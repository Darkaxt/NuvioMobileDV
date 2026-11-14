package com.nuvio.app.features.playback.classification

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Spec P3 originating surfaces. */
enum class AnimationToggleSurface(val wireName: String) {
    Details("details"),
    PosterSheet("posterSheet"),
    ContinueWatchingSheet("continueWatchingSheet"),
    Player("player"),
}

@Serializable
data class DecisionEvidence(
    val categories: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val services: List<String> = emptyList(),
    val networks: List<String> = emptyList(),
    val originCountries: List<String> = emptyList(),
    val studios: List<String> = emptyList(),
)

/** One JSONL ledger record (spec P3 example schema). */
@Serializable
data class AnimationDecisionEvent(
    val schemaVersion: Int = SCHEMA_VERSION,
    val eventId: String,
    val timestampUtc: String,
    val profileId: String,
    val titleId: String,
    val titleType: String,
    val title: String,
    val animation: Boolean,
    val previousExplicitValue: Boolean?,
    val initialSuggestedValue: Boolean,
    val currentSuggestedValue: Boolean,
    val ruleSetVersion: Int,
    val ruleSetSha256: String,
    val matchedRuleIds: List<String>,
    val matchedExclusionIds: List<String>,
    val surface: String,
    val evidence: DecisionEvidence,
) {
    val canonicalTitle: CanonicalTitle get() = CanonicalTitle(titleId, titleType)

    companion object {
        const val SCHEMA_VERSION = 1
    }
}

data class LedgerReplay(
    /** Latest valid record per [CanonicalTitle.key]. */
    val latest: Map<String, AnimationDecisionEvent>,
    /** Malformed records before the final line (1-based line numbers); surfaced, never skipped silently. */
    val corruptLines: List<Int> = emptyList(),
    val truncatedTrailingRecord: Boolean = false,
) {
    val error: String?
        get() = corruptLines.takeIf { it.isNotEmpty() }
            ?.let { "Animation decision ledger has malformed record(s) at line(s) ${it.joinToString()}" }
}

/** Profile-scoped append-only decision ledger. */
interface AnimationDecisionLedger {
    /** Durably commits [event] (flushed + synced) before returning; throws on failure. */
    suspend fun append(event: AnimationDecisionEvent)

    suspend fun replay(profileId: String): LedgerReplay
}

object AnimationDecisionLedgerCodec {
    private val json = Json { ignoreUnknownKeys = false; explicitNulls = true; encodeDefaults = true }
    private val surfaces = AnimationToggleSurface.entries.map { it.wireName }.toSet()

    fun encode(event: AnimationDecisionEvent): String =
        json.encodeToString(AnimationDecisionEvent.serializer(), event)

    private fun decodeValid(line: String, profileId: String): AnimationDecisionEvent? = runCatching {
        json.decodeFromString(AnimationDecisionEvent.serializer(), line)
    }.getOrNull()?.takeIf {
        it.schemaVersion == AnimationDecisionEvent.SCHEMA_VERSION &&
            it.profileId == profileId &&
            it.surface in surfaces &&
            it.titleId.isNotBlank() &&
            it.titleType in setOf("movie", "series")
    }

    /**
     * Replays ledger text. A final line without a terminating newline that does not parse is an
     * interrupted (never acknowledged) write and is ignored; earlier invalid lines are reported.
     */
    fun replay(text: String, profileId: String): LedgerReplay {
        val endsWithNewline = text.endsWith("\n")
        val lines = text.split("\n").let { if (endsWithNewline) it.dropLast(1) else it }
        val latest = linkedMapOf<String, AnimationDecisionEvent>()
        val corrupt = mutableListOf<Int>()
        var truncated = false
        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trimEnd('\r')
            if (line.isBlank()) return@forEachIndexed
            val event = decodeValid(line, profileId)
            when {
                event != null -> latest[event.canonicalTitle.key] = event
                index == lines.lastIndex && !endsWithNewline -> truncated = true
                else -> corrupt += index + 1
            }
        }
        return LedgerReplay(latest, corrupt, truncated)
    }
}
