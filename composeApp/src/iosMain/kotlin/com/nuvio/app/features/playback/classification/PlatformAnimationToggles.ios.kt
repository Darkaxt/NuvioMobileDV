package com.nuvio.app.features.playback.classification

/** iOS has no Anime4K path, so it shows no Animation toggle. */
internal actual fun platformAnimationToggleRepository(): AnimationToggleRepository? = null
