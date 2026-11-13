package com.nuvio.app.features.player

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@androidx.annotation.OptIn(UnstableApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UnsupportedVideoTrackTest {

    private val dolbyVisionProfile5 = Format.Builder()
        .setSampleMimeType(MimeTypes.VIDEO_DOLBY_VISION)
        .setCodecs("dvh1.05.06")
        .build()
    private val hevc = Format.Builder().setSampleMimeType(MimeTypes.VIDEO_H265).build()
    private val aac = Format.Builder().setSampleMimeType(MimeTypes.AUDIO_AAC).build()

    private fun group(format: Format, support: Int, selected: Boolean) = Tracks.Group(
        TrackGroup(format),
        false,
        intArrayOf(support),
        booleanArrayOf(selected),
    )

    private val audio = group(aac, C.FORMAT_HANDLED, selected = true)

    @Test
    fun unsupportedDolbyVisionProfile5IsReported() {
        val tracks = Tracks(listOf(group(dolbyVisionProfile5, C.FORMAT_UNSUPPORTED_SUBTYPE, false), audio))
        assertEquals("video/dolby-vision dvh1.05.06", tracks.unplayableVideoFormatDescription())
    }

    @Test
    fun selectedVideoIsNotReported() {
        val tracks = Tracks(listOf(group(hevc, C.FORMAT_HANDLED, true), audio))
        assertNull(tracks.unplayableVideoFormatDescription())
    }

    @Test
    fun videoExceedingCapabilitiesIsNotReported() {
        val tracks = Tracks(listOf(group(hevc, C.FORMAT_EXCEEDS_CAPABILITIES, false), audio))
        assertNull(tracks.unplayableVideoFormatDescription())
    }

    @Test
    fun supportedButDeselectedVideoIsNotReported() {
        val tracks = Tracks(listOf(group(hevc, C.FORMAT_HANDLED, false), audio))
        assertNull(tracks.unplayableVideoFormatDescription())
    }

    @Test
    fun audioOnlyAndEmptyTracksAreNotReported() {
        assertNull(Tracks(listOf(audio)).unplayableVideoFormatDescription())
        assertNull(Tracks.EMPTY.unplayableVideoFormatDescription())
    }
}
