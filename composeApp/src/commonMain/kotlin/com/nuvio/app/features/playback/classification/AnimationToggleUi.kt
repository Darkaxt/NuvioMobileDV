package com.nuvio.app.features.playback.classification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.profiles.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.animation_toggle_off
import nuvio.composeapp.generated.resources.animation_toggle_on
import nuvio.composeapp.generated.resources.animation_toggle_suggested_off
import nuvio.composeapp.generated.resources.animation_toggle_suggested_on
import org.jetbrains.compose.resources.stringResource

/** Current Animation toggle of one title plus its writer; null where the platform has no toggle. */
internal class AnimationToggleBinding(
    val state: AnimationToggleState,
    private val onSet: (Boolean) -> Unit,
) {
    val enabled: Boolean get() = state.effectiveAnimation
    val isExplicit: Boolean get() = state.explicitValue != null

    fun set(value: Boolean) = onSet(value)
    fun toggle() = onSet(!enabled)
}

// Writes outlive the surface that started them (sheets close right after selection).
private val toggleWriteScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
private val log = Logger.withTag("AnimationToggle")

/**
 * Observes the active profile's Animation toggle for a movie or series. Episodes must pass their
 * series id/type. [facts] only refine the suggestion; pass null when no metadata is available.
 */
@Composable
internal fun rememberAnimationToggle(
    metaId: String?,
    metaType: String?,
    displayTitle: String,
    facts: TitleFacts?,
    surface: AnimationToggleSurface,
): AnimationToggleBinding? {
    if (metaId.isNullOrBlank() || metaType == null || !isAnimationToggleTitleType(metaType)) return null
    val repository = remember { platformAnimationToggleRepository() } ?: return null
    val profileId = ProfileRepository.activeProfileId.toString()
    val title = remember(metaId, metaType) { CanonicalTitle.of(metaId, metaType) }
    val flow = remember(repository, profileId, title) { repository.state(profileId, title) }
    LaunchedEffect(repository, profileId) {
        runCatching { repository.load(profileId) }.onFailure { log.w(it) { "Ledger load failed" } }
    }
    LaunchedEffect(repository, profileId, title, facts) {
        if (facts != null) {
            withContext(Dispatchers.Default) {
                runCatching { repository.observeFacts(profileId, title, facts) }
                    .onFailure { log.w(it) { "Could not record title facts" } }
            }
        }
    }
    val state by flow.collectAsState()
    return AnimationToggleBinding(state) { value ->
        toggleWriteScope.launch {
            // The repository keeps the previous value and surfaces the error on failure.
            runCatching { repository.setExplicit(profileId, title, displayTitle, value, surface, facts) }
                .onFailure { log.w(it) { "Animation choice not saved" } }
        }
    }
}

/**
 * Replays the profile's ledger ahead of any toggle surface. Long-press sheets freeze their action
 * labels when they open, so a ledger loaded only by the sheet itself would show the suggestion
 * instead of the explicit choice.
 */
@Composable
internal fun PreloadAnimationToggles(profileId: Int) {
    val repository = remember { platformAnimationToggleRepository() } ?: return
    LaunchedEffect(repository, profileId) {
        runCatching { repository.load(profileId.toString()) }.onFailure { log.w(it) { "Ledger preload failed" } }
    }
}

/** "Animation: On" / "Animation: On (suggested)" etc., used as visible and accessible label. */
@Composable
internal fun AnimationToggleBinding.label(): String = stringResource(
    when {
        isExplicit && enabled -> Res.string.animation_toggle_on
        isExplicit -> Res.string.animation_toggle_off
        enabled -> Res.string.animation_toggle_suggested_on
        else -> Res.string.animation_toggle_suggested_off
    },
)

/** Only movies and series (anime included) carry an Animation choice; other meta types do not. */
internal fun isAnimationToggleTitleType(metaType: String?): Boolean =
    metaType?.trim()?.lowercase() in setOf("movie", "series", "anime")

internal fun MetaPreview.toTitleFacts(): TitleFacts =
    TitleFacts(
        categories = if (isNuvioAnimeCategory(id, type)) setOf("anime") else emptySet(),
        genres = genres.toSet(),
        contentType = canonicalTitleType(type),
    )

/** Continue Watching only knows the series/movie identity; anime namespace ids are still a fact. */
internal fun continueWatchingTitleFacts(parentMetaId: String, parentMetaType: String): TitleFacts? =
    if (isNuvioAnimeCategory(parentMetaId, parentMetaType)) {
        TitleFacts(categories = setOf("anime"), contentType = canonicalTitleType(parentMetaType))
    } else {
        null
    }
