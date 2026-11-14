package com.nuvio.app.features.player

import android.content.Context
import android.util.Log
import com.nuvio.app.features.playback.upscale.Anime4kProfile
import `is`.xyz.mpv.MPVNode
import java.io.File

/**
 * Anime4K GLSL shaders bundled under `assets/anime4k/` (MIT licence in the same folder). mpv loads
 * user shaders from files, so they are mirrored into app-internal storage once per process.
 */
internal object Anime4kShaderFiles {
    private const val ASSET_DIR = "anime4k"

    @Volatile
    private var installedDir: File? = null

    /** Path of [profile]'s shader, installing the bundled copies first if needed. */
    @Synchronized
    fun pathFor(context: Context, profile: Anime4kProfile): String {
        val dir = installedDir ?: install(context.applicationContext).also { installedDir = it }
        return File(dir, profile.shaderFile).path
    }

    private fun install(context: Context): File {
        val dir = File(context.filesDir, ASSET_DIR)
        if (!dir.isDirectory && !dir.mkdirs()) error("Cannot create $dir")
        Anime4kProfile.entries.forEach { profile ->
            val bytes = context.assets.open("$ASSET_DIR/${profile.shaderFile}").use { it.readBytes() }
            val target = File(dir, profile.shaderFile)
            if (!target.isFile || !target.readBytes().contentEquals(bytes)) {
                val partial = File(dir, "${profile.shaderFile}.partial")
                partial.writeBytes(bytes)
                if (!partial.renameTo(target)) error("Cannot install ${target.name}")
            }
        }
        return dir
    }
}

/**
 * GPU render cost from mpv's `vo-passes` (fresh frames): the sum of every pass's average and peak,
 * in milliseconds. Null when the VO reports no timings.
 */
internal fun voPassTotalsMs(passes: MPVNode?): Pair<Double, Double>? {
    val fresh = ((passes as? MPVNode.MapNode)?.value?.get("fresh") as? MPVNode.ArrayNode)?.value ?: return null
    if (fresh.isEmpty()) return null
    var avgNs = 0L
    var peakNs = 0L
    fresh.forEach { pass ->
        val fields = (pass as? MPVNode.MapNode)?.value ?: return@forEach
        avgNs += (fields["avg"] as? MPVNode.IntNode)?.value ?: 0L
        peakNs += (fields["peak"] as? MPVNode.IntNode)?.value ?: 0L
    }
    return avgNs / 1e6 to peakNs / 1e6
}

internal fun logAnime4k(message: String) {
    Log.i(PLAYER_DIAGNOSTIC_TAG, message)
}
