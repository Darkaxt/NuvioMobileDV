package com.nuvio.app.features.playback.classification

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Last observed classification facts for a title, so other launch routes keep its category. */
@Serializable
data class ObservedTitleFacts(
    val facts: TitleFacts,
    /** Suggestion when the title was first observed; reported as `initialSuggestedValue`. */
    val initialSuggestedValue: Boolean,
)

interface ObservedTitleFactsStore {
    fun load(profileId: String, title: CanonicalTitle): ObservedTitleFacts?
    fun save(profileId: String, title: CanonicalTitle, observed: ObservedTitleFacts)
}

data class AnimationToggleState(
    val explicitValue: Boolean? = null,
    val classification: ContentClassification = ContentClassification(null, false, emptyList(), emptyList()),
    val loaded: Boolean = false,
    /** Ledger corruption or last write failure, for display. */
    val error: String? = null,
) {
    val suggestion: Boolean get() = classification.suggestedAnimationToggle
    val effectiveAnimation: Boolean get() = explicitValue ?: suggestion
    val eligibility: EnhancementEligibility get() = effectiveEnhancementEligibility(explicitValue, classification)
}

/**
 * Per profile + canonical title Animation toggle (spec P2/P3). The ledger is the only store of
 * explicit choices; suggestions are derived from the current rule set and never persisted as choices.
 */
class AnimationToggleRepository(
    private val ledger: AnimationDecisionLedger,
    private val factsStore: ObservedTitleFactsStore,
    private val ruleSet: () -> ValidatedRuleSet,
    private val now: () -> String = { Clock.System.now().toString() },
    @OptIn(ExperimentalUuidApi::class)
    private val newEventId: () -> String = { Uuid.random().toString() },
) {
    private val writeMutex = Mutex()
    private val loadMutex = Mutex()
    private val explicitByProfile = mutableMapOf<String, Map<String, Boolean>>()
    private val ledgerErrors = mutableMapOf<String, String?>()
    private val flows = mutableMapOf<String, MutableStateFlow<AnimationToggleState>>()
    private val lock = SynchronizedObject()

    private fun flowKey(profileId: String, title: CanonicalTitle) = "$profileId\u0000${title.key}"

    private fun flowFor(profileId: String, title: CanonicalTitle) = synchronized(lock) {
        flows.getOrPut(flowKey(profileId, title)) { MutableStateFlow(AnimationToggleState()) }
    }

    /** Replays the profile ledger once; safe to call repeatedly. */
    suspend fun load(profileId: String) = loadMutex.withLock {
        if (synchronized(lock) { profileId in explicitByProfile }) return@withLock
        val replay = ledger.replay(profileId)
        synchronized(lock) {
            explicitByProfile[profileId] = replay.latest.mapValues { it.value.animation }
            ledgerErrors[profileId] = replay.error
        }
        refreshProfile(profileId)
    }

    /**
     * Observes the toggle for [title]. [facts] (when provided) refine the suggestion until an explicit
     * choice exists and are remembered so later launches without metadata keep the classification.
     */
    fun state(profileId: String, title: CanonicalTitle, facts: TitleFacts? = null): StateFlow<AnimationToggleState> {
        if (facts != null) observeFacts(profileId, title, facts)
        val flow = flowFor(profileId, title)
        flow.value = computeState(profileId, title)
        return flow.asStateFlow()
    }

    fun observeFacts(profileId: String, title: CanonicalTitle, facts: TitleFacts) {
        val previous = factsStore.load(profileId, title)
        val merged = facts.mergedWithPrevious(previous?.facts)
        if (previous == null || previous.facts != merged) {
            val initial = previous?.initialSuggestedValue
                ?: ContentClassifier.evaluate(merged, ruleSet().rules).suggestedAnimationToggle
            factsStore.save(profileId, title, ObservedTitleFacts(merged, initial))
        }
        synchronized(lock) { flows[flowKey(profileId, title)] }?.let { it.value = computeState(profileId, title) }
    }

    /**
     * Records an explicit choice (also when equal to the suggestion). State changes only after the
     * ledger append is durable; on failure the previous value is kept and the error surfaced.
     */
    suspend fun setExplicit(
        profileId: String,
        title: CanonicalTitle,
        displayTitle: String,
        value: Boolean,
        surface: AnimationToggleSurface,
        facts: TitleFacts? = null,
    ): Result<Unit> {
        load(profileId)
        if (facts != null) observeFacts(profileId, title, facts)
        return writeMutex.withLock {
            val previous = synchronized(lock) { explicitByProfile[profileId]?.get(title.key) }
            val observed = factsStore.load(profileId, title)
            val validated = ruleSet()
            val classification = ContentClassifier.evaluate(observed?.facts, validated.rules)
            val normalized = observed?.facts?.let { ContentClassifier.normalize(it, validated.rules) }
            val event = AnimationDecisionEvent(
                eventId = newEventId(),
                timestampUtc = now(),
                profileId = profileId,
                titleId = title.titleId,
                titleType = title.titleType,
                title = displayTitle,
                animation = value,
                previousExplicitValue = previous,
                initialSuggestedValue = observed?.initialSuggestedValue ?: classification.suggestedAnimationToggle,
                currentSuggestedValue = classification.suggestedAnimationToggle,
                ruleSetVersion = validated.rules.ruleSetVersion,
                ruleSetSha256 = validated.sha256,
                matchedRuleIds = classification.matchedRuleIds,
                matchedExclusionIds = classification.matchedExclusionIds,
                surface = surface.wireName,
                evidence = DecisionEvidence(
                    categories = normalized?.categories.orEmpty().sorted(),
                    genres = normalized?.genres.orEmpty().sorted(),
                    services = normalized?.services.orEmpty().sorted(),
                    networks = normalized?.networks.orEmpty().sorted(),
                    originCountries = normalized?.originCountries.orEmpty().map { it.uppercase() }.sorted(),
                    studios = normalized?.studios.orEmpty().sorted(),
                ),
            )
            val outcome = runCatching { ledger.append(event) }
            outcome.onSuccess {
                synchronized(lock) {
                    explicitByProfile[profileId] = explicitByProfile[profileId].orEmpty() + (title.key to value)
                }
            }
            val writeError = outcome.exceptionOrNull()?.let { "Could not save Animation choice: ${it.message}" }
            flowFor(profileId, title).update { computeState(profileId, title).let { state -> state.copy(error = writeError ?: state.error) } }
            outcome
        }
    }


    /** Re-evaluates suggestions (e.g. after a rule-set reload); explicit choices are untouched. */
    fun refreshAll() {
        val keys = synchronized(lock) { flows.keys.toList() }
        keys.forEach { key ->
            val profileId = key.substringBefore('\u0000')
            val titleKey = key.substringAfter('\u0000')
            val title = CanonicalTitle(titleKey.substringAfter('|'), titleKey.substringBefore('|'))
            synchronized(lock) { flows[key] }?.value = computeState(profileId, title)
        }
    }

    private fun refreshProfile(profileId: String) {
        val keys = synchronized(lock) { flows.keys.filter { it.startsWith("$profileId\u0000") } }
        keys.forEach { key ->
            val titleKey = key.substringAfter('\u0000')
            val title = CanonicalTitle(titleKey.substringAfter('|'), titleKey.substringBefore('|'))
            synchronized(lock) { flows[key] }?.value = computeState(profileId, title)
        }
    }

    private fun computeState(profileId: String, title: CanonicalTitle): AnimationToggleState {
        val (loaded, explicit, error) = synchronized(lock) {
            Triple(
                profileId in explicitByProfile,
                explicitByProfile[profileId]?.get(title.key),
                ledgerErrors[profileId],
            )
        }
        val classification = ContentClassifier.evaluate(factsStore.load(profileId, title)?.facts, ruleSet().rules)
        return AnimationToggleState(explicit, classification, loaded, error)
    }
}
