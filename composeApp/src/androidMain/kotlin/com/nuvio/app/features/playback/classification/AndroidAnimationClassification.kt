package com.nuvio.app.features.playback.classification

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

/**
 * JSONL ledger at `<filesDir>/playback-decisions/profile-<id>.jsonl` (spec P3). Writers are
 * serialized; each record is one line, synced to storage before [append] returns.
 */
class AndroidAnimationDecisionLedger(private val directory: File) : AnimationDecisionLedger {
    private val mutex = Mutex()

    fun fileFor(profileId: String): File {
        require(profileId.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid profile id '$profileId'" }
        return File(directory, "profile-$profileId.jsonl")
    }

    override suspend fun append(event: AnimationDecisionEvent) = mutex.withLock {
        withContext(Dispatchers.IO) {
            val file = fileFor(event.profileId)
            if (!directory.isDirectory && !directory.mkdirs()) error("Cannot create $directory")
            RandomAccessFile(file, "rw").use { raf ->
                // An unterminated tail is an interrupted, never-acknowledged write: drop it so the new
                // record starts on its own line and earlier history stays intact.
                raf.setLength(lastNewlineEnd(raf))
                raf.seek(raf.length())
                raf.write((AnimationDecisionLedgerCodec.encode(event) + "\n").toByteArray(Charsets.UTF_8))
                raf.fd.sync()
            }
        }
    }

    override suspend fun replay(profileId: String): LedgerReplay = mutex.withLock {
        withContext(Dispatchers.IO) {
            val file = fileFor(profileId)
            if (!file.exists()) LedgerReplay(emptyMap())
            else AnimationDecisionLedgerCodec.replay(file.readText(Charsets.UTF_8), profileId)
        }
    }

    private fun lastNewlineEnd(raf: RandomAccessFile): Long {
        var position = raf.length()
        while (position > 0) {
            raf.seek(position - 1)
            if (raf.read() == '\n'.code) return position
            position--
        }
        return 0
    }
}

/** Observed facts per profile/title (not a choice store). */
internal class AndroidObservedTitleFactsStore(private val preferences: SharedPreferences) : ObservedTitleFactsStore {
    private val json = Json { ignoreUnknownKeys = true }

    private fun key(profileId: String, title: CanonicalTitle) = "$profileId|${title.key}"

    override fun load(profileId: String, title: CanonicalTitle): ObservedTitleFacts? =
        preferences.getString(key(profileId, title), null)?.let {
            runCatching { json.decodeFromString(ObservedTitleFacts.serializer(), it) }.getOrNull()
        }

    override fun save(profileId: String, title: CanonicalTitle, observed: ObservedTitleFacts) {
        preferences.edit().putString(key(profileId, title), json.encodeToString(ObservedTitleFacts.serializer(), observed)).apply()
    }
}

/** The packaged rule artifact (`assets/playback/playback-content-indicators.json`), validated once. */
internal fun loadPackagedRuleSet(context: Context): ValidatedRuleSet {
    val bytes = context.assets.open(RULES_ASSET).use { it.readBytes() }
    val sha256 = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    return ContentIndicatorRuleParser.parse(bytes.toString(Charsets.UTF_8))
        .map { ValidatedRuleSet(it, sha256) }
        .getOrThrow()
}

private const val RULES_ASSET = "playback/playback-content-indicators.json"

/** Android wiring; [initialize] runs from MainActivity before any toggle surface is composed. */
object AndroidAnimationToggles {
    @Volatile
    var repository: AnimationToggleRepository? = null
        private set

    fun initialize(context: Context) {
        if (repository != null) return
        val appContext = context.applicationContext
        val rules by lazy { loadPackagedRuleSet(appContext) }
        repository = AnimationToggleRepository(
            ledger = AndroidAnimationDecisionLedger(File(appContext.filesDir, "playback-decisions")),
            factsStore = AndroidObservedTitleFactsStore(
                appContext.getSharedPreferences("playback_title_facts", Context.MODE_PRIVATE),
            ),
            ruleSet = { rules },
        )
    }
}
