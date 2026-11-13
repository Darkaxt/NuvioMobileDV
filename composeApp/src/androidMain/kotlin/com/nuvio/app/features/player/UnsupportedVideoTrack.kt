package com.nuvio.app.features.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi

/**
 * Describes the video format ExoPlayer cannot play, or returns null when playback has a
 * usable video track (or the stream has no video at all).
 *
 * ExoPlayer does not raise a playback error when no renderer supports any video track; it
 * plays the audio over a black picture. Dolby Vision Profile 5 on devices whose only Dolby
 * Vision decoder is the secure one is the known case. The player reports this like a decoder
 * error so the engine failover can switch to libmpv.
 *
 * Only fires when video tracks exist, none is selected and none is supported even when
 * exceeding device capabilities is allowed, so audio-only streams and deliberately disabled
 * video are left alone.
 */
@OptIn(UnstableApi::class)
internal fun Tracks.unplayableVideoFormatDescription(): String? {
    val videoGroups = groups.filter { it.type == C.TRACK_TYPE_VIDEO }
    if (videoGroups.isEmpty()) return null
    if (videoGroups.any { it.isSelected || it.isSupported(/* allowExceedsCapabilities = */ true) }) {
        return null
    }
    return videoGroups
        .flatMap { group -> (0 until group.length).map(group::getTrackFormat) }
        .map { format ->
            listOfNotNull(format.sampleMimeType, format.codecs).joinToString(" ").ifBlank { "video" }
        }
        .distinct()
        .joinToString(", ")
}
