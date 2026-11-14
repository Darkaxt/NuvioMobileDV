package com.nuvio.app.features.playback.upscale

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Anime4K v4.0 single-shader profiles (MIT, bloc97/Anime4K), measured on the owner's Galaxy Tab
 * S9 Ultra (Adreno 740, 2960x1848): CNN_x2_S costs about 25 ms per 1080p frame (sustained over
 * 10 minutes without throttling) and CNN_x2_M about 22 ms per 720p frame, both within the 33 ms
 * budget of 30 fps content. M does not fit 1080p (p95 42 ms), so each profile has a source limit.
 */
enum class Anime4kProfile(val shaderFile: String, val maxWidth: Int, val maxHeight: Int) {
    Quality("Anime4K_Upscale_CNN_x2_M.glsl", maxWidth = 1280, maxHeight = 736),
    Fast("Anime4K_Upscale_CNN_x2_S.glsl", maxWidth = 1920, maxHeight = 1088),
    ;

    fun fits(width: Int, height: Int): Boolean = width <= maxWidth && height <= maxHeight
}

enum class UpscaleSkipReason { Disabled, ExoPlayer, HighDynamicRange, SourceAboveLimit }

/** Player panel status of the upscaler for the title that is playing. */
data class UpscaleStatus(
    val kind: Kind,
    val profile: Anime4kProfile? = null,
    val skipReason: UpscaleSkipReason? = null,
    val sourceWidth: Int? = null,
    val sourceHeight: Int? = null,
    val error: String? = null,
) {
    enum class Kind { Off, Pending, Active, Skipped, Failed }

    companion object {
        val Off = UpscaleStatus(Kind.Off)
    }
}

/**
 * Decides the upscaler for the current stream. [requested] is the title's effective Animation
 * eligibility; source size is the decoded video size (null until the first frame is known).
 */
fun decideAnime4k(
    requested: Boolean,
    settingEnabled: Boolean,
    libmpv: Boolean,
    sourceWidth: Int?,
    sourceHeight: Int?,
    highDynamicRange: Boolean,
): UpscaleStatus {
    if (!requested) return UpscaleStatus.Off
    fun skipped(reason: UpscaleSkipReason) =
        UpscaleStatus(UpscaleStatus.Kind.Skipped, skipReason = reason, sourceWidth = sourceWidth, sourceHeight = sourceHeight)
    if (!settingEnabled) return skipped(UpscaleSkipReason.Disabled)
    if (!libmpv) return skipped(UpscaleSkipReason.ExoPlayer)
    if (sourceWidth == null || sourceHeight == null || sourceWidth <= 0 || sourceHeight <= 0) {
        return UpscaleStatus(UpscaleStatus.Kind.Pending)
    }
    if (highDynamicRange) return skipped(UpscaleSkipReason.HighDynamicRange)
    val profile = Anime4kProfile.entries.firstOrNull { it.fits(sourceWidth, sourceHeight) }
        ?: return skipped(UpscaleSkipReason.SourceAboveLimit)
    return UpscaleStatus(UpscaleStatus.Kind.Active, profile = profile, sourceWidth = sourceWidth, sourceHeight = sourceHeight)
}

/** Status published by the attached player surface; null when no player is open. */
object ActivePlaybackUpscale {
    private val lock = SynchronizedObject()
    private var owner: Any? = null
    private val mutableStatus = MutableStateFlow<UpscaleStatus?>(null)
    val status: StateFlow<UpscaleStatus?> = mutableStatus.asStateFlow()

    fun publish(owner: Any, status: UpscaleStatus) = synchronized(lock) {
        this.owner = owner
        mutableStatus.value = status
    }

    /** Clears the status only when [owner] published it last (a replacing surface may already have). */
    fun clear(owner: Any) = synchronized(lock) {
        if (this.owner === owner) {
            this.owner = null
            mutableStatus.value = null
        }
    }
}
