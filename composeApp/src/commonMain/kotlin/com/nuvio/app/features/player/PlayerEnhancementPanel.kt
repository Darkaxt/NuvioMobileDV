package com.nuvio.app.features.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.details.MetaDetails
import com.nuvio.app.features.playback.classification.AnimationToggleBinding
import com.nuvio.app.features.playback.classification.AnimationToggleSurface
import com.nuvio.app.features.playback.classification.platformAnimationToggleRepository
import com.nuvio.app.features.playback.classification.rememberAnimationToggle
import com.nuvio.app.features.playback.classification.toTitleFacts
import com.nuvio.app.features.playback.upscale.ActivePlaybackUpscale
import com.nuvio.app.features.playback.upscale.Anime4kProfile
import com.nuvio.app.features.playback.upscale.UpscaleSkipReason
import com.nuvio.app.features.playback.upscale.UpscaleStatus
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.action_close
import nuvio.composeapp.generated.resources.animation_toggle_explicit_desc
import nuvio.composeapp.generated.resources.animation_toggle_suggested_desc
import nuvio.composeapp.generated.resources.animation_toggle_title
import nuvio.composeapp.generated.resources.player_enhancements_title
import nuvio.composeapp.generated.resources.player_upscale_active_fast
import nuvio.composeapp.generated.resources.player_upscale_active_quality
import nuvio.composeapp.generated.resources.player_upscale_failed
import nuvio.composeapp.generated.resources.player_upscale_off
import nuvio.composeapp.generated.resources.player_upscale_pending
import nuvio.composeapp.generated.resources.player_upscale_reason_disabled
import nuvio.composeapp.generated.resources.player_upscale_reason_exoplayer
import nuvio.composeapp.generated.resources.player_upscale_reason_hdr
import nuvio.composeapp.generated.resources.player_upscale_reason_large
import nuvio.composeapp.generated.resources.player_upscale_skipped
import org.jetbrains.compose.resources.stringResource

/** True where the per-title Animation toggle and upscaler exist (Android). */
internal val enhancementPanelSupported: Boolean
    get() = platformAnimationToggleRepository() != null

/** The playing title's Animation toggle (series value for episodes); null without a toggle. */
@Composable
internal fun rememberPlayerAnimationToggle(
    parentMetaId: String,
    parentMetaType: String,
    title: String,
    meta: MetaDetails?,
): AnimationToggleBinding? {
    val ownMeta = meta?.takeIf { it.id == parentMetaId }
    val facts = remember(ownMeta) { ownMeta?.toTitleFacts() }
    return rememberAnimationToggle(
        metaId = parentMetaId,
        metaType = parentMetaType,
        displayTitle = ownMeta?.name ?: title,
        facts = facts,
        surface = AnimationToggleSurface.Player,
    )
}

/**
 * Player side panel with the upscaler status and the title's Animation toggle (spec P2). A change
 * is saved immediately and applied to the playing stream without reloading it.
 */
@Composable
internal fun PlayerEnhancementPanel(
    visible: Boolean,
    toggle: AnimationToggleBinding?,
    onDismiss: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val status by ActivePlaybackUpscale.status.collectAsState()

    PlayerSidePanel(visible = visible, onDismiss = onDismiss, width = 420.dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlayerPanelHeader(title = stringResource(Res.string.player_enhancements_title)) {
                PlayerDialogButton(label = stringResource(Res.string.action_close), onClick = onDismiss)
            }

            status?.let { current ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = current.headline(),
                        color = tokens.colors.textPrimary,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    current.reason()?.let { reason ->
                        Text(text = reason, color = tokens.colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (toggle != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = toggle.enabled,
                            role = Role.Switch,
                            onValueChange = toggle::set,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = stringResource(Res.string.animation_toggle_title),
                            color = tokens.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(
                                if (toggle.isExplicit) {
                                    Res.string.animation_toggle_explicit_desc
                                } else {
                                    Res.string.animation_toggle_suggested_desc
                                },
                            ),
                            color = tokens.colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        toggle.state.error?.let { error ->
                            Spacer(Modifier.height(4.dp))
                            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    // The row owns the toggle semantics and focus; the switch is visual only.
                    Switch(checked = toggle.enabled, onCheckedChange = null)
                }
            }
        }
    }
}

@Composable
private fun UpscaleStatus.headline(): String {
    val base = stringResource(
        when (kind) {
            UpscaleStatus.Kind.Off -> Res.string.player_upscale_off
            UpscaleStatus.Kind.Pending -> Res.string.player_upscale_pending
            UpscaleStatus.Kind.Active -> when (profile) {
                Anime4kProfile.Quality -> Res.string.player_upscale_active_quality
                else -> Res.string.player_upscale_active_fast
            }
            UpscaleStatus.Kind.Skipped -> Res.string.player_upscale_skipped
            UpscaleStatus.Kind.Failed -> Res.string.player_upscale_failed
        },
    )
    val size = if (sourceWidth != null && sourceHeight != null) " · ${sourceWidth}x$sourceHeight" else ""
    return base + size
}

@Composable
private fun UpscaleStatus.reason(): String? = when {
    error != null -> error
    skipReason != null -> stringResource(
        when (skipReason) {
            UpscaleSkipReason.Disabled -> Res.string.player_upscale_reason_disabled
            UpscaleSkipReason.ExoPlayer -> Res.string.player_upscale_reason_exoplayer
            UpscaleSkipReason.HighDynamicRange -> Res.string.player_upscale_reason_hdr
            UpscaleSkipReason.SourceAboveLimit -> Res.string.player_upscale_reason_large
        },
    )
    else -> null
}
