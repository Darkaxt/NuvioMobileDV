package com.nuvio.app.features.playback.upscale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Anime4kUpscaleTest {
    private fun decide(
        width: Int?,
        height: Int?,
        requested: Boolean = true,
        setting: Boolean = true,
        libmpv: Boolean = true,
        hdr: Boolean = false,
    ) = decideAnime4k(requested, setting, libmpv, width, height, hdr)

    @Test
    fun not_requested_is_off_regardless_of_source() {
        assertEquals(UpscaleStatus.Off, decide(1280, 720, requested = false))
        assertEquals(UpscaleStatus.Off, decide(null, null, requested = false, libmpv = false))
    }

    @Test
    fun sd_and_720p_use_quality() {
        assertEquals(Anime4kProfile.Quality, decide(1280, 720).profile)
        assertEquals(Anime4kProfile.Quality, decide(960, 720).profile)
        assertEquals(Anime4kProfile.Quality, decide(1280, 736).profile)
        assertEquals(UpscaleStatus.Kind.Active, decide(720, 480).kind)
    }

    @Test
    fun above_the_720p_box_up_to_1080p_uses_fast() {
        assertEquals(Anime4kProfile.Fast, decide(1920, 1080).profile)
        assertEquals(Anime4kProfile.Fast, decide(1920, 1088).profile)
        assertEquals(Anime4kProfile.Fast, decide(1440, 1080).profile)
        assertEquals(Anime4kProfile.Fast, decide(1281, 720).profile)
        assertEquals(Anime4kProfile.Fast, decide(1280, 800).profile)
    }

    @Test
    fun larger_than_1080p_is_skipped() {
        val uhd = decide(3840, 2160)
        assertEquals(UpscaleStatus.Kind.Skipped, uhd.kind)
        assertEquals(UpscaleSkipReason.SourceAboveLimit, uhd.skipReason)
        assertNull(uhd.profile)
        assertEquals(UpscaleSkipReason.SourceAboveLimit, decide(2048, 1080).skipReason)
    }

    @Test
    fun setting_engine_and_hdr_skips_have_reasons() {
        assertEquals(UpscaleSkipReason.Disabled, decide(1280, 720, setting = false).skipReason)
        assertEquals(UpscaleSkipReason.ExoPlayer, decide(null, null, libmpv = false).skipReason)
        assertEquals(UpscaleSkipReason.HighDynamicRange, decide(1920, 1080, hdr = true).skipReason)
    }

    @Test
    fun unknown_size_is_pending() {
        assertEquals(UpscaleStatus.Kind.Pending, decide(null, null).kind)
        assertEquals(UpscaleStatus.Kind.Pending, decide(0, 0).kind)
    }

    @Test
    fun clear_only_removes_own_status() {
        val first = Any()
        val second = Any()
        ActivePlaybackUpscale.publish(first, decide(1280, 720))
        ActivePlaybackUpscale.publish(second, decide(1920, 1080))
        ActivePlaybackUpscale.clear(first)
        assertEquals(Anime4kProfile.Fast, ActivePlaybackUpscale.status.value?.profile)
        ActivePlaybackUpscale.clear(second)
        assertNull(ActivePlaybackUpscale.status.value)
    }
}
