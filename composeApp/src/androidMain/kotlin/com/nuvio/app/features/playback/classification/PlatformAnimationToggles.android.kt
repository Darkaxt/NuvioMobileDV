package com.nuvio.app.features.playback.classification

/** Animation titles are upscaled with Anime4K in libmpv, so Android shows the toggle. */
internal actual fun platformAnimationToggleRepository(): AnimationToggleRepository? = AndroidAnimationToggles.repository
