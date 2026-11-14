package com.nuvio.app.features.playback.classification

import com.nuvio.app.features.details.MetaCompany
import com.nuvio.app.features.details.MetaDetails
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnimationClassificationTest {
    private val tempDir: Path = Files.createTempDirectory("nuvio-animation-classification")
    private val packagedText = String(Files.readAllBytes(packagedAsset()), Charsets.UTF_8)
    private val rules = ContentIndicatorRuleParser.parse(packagedText).getOrThrow()
    private val packaged = ValidatedRuleSet(rules, sha256 = "packaged")

    /** The rule artifact the APK ships (`assets/playback/...`), resolved from the module directory. */
    private fun packagedAsset(): Path =
        listOf(
            Paths.get("src/androidMain/assets/playback/playback-content-indicators.json"),
            Paths.get("composeApp/src/androidMain/assets/playback/playback-content-indicators.json"),
        ).firstOrNull(Files::exists) ?: error("packaged rule asset not found from ${Paths.get("").toAbsolutePath()}")

    @AfterTest
    fun cleanup() {
        tempDir.toFile().deleteRecursively()
    }

    private fun classify(facts: TitleFacts?) = ContentClassifier.evaluate(facts, rules)

    @Test
    fun packaged_artifact_is_valid() {
        assertTrue(rules.ruleSetVersion >= 1)
    }

    @Test
    fun anime_category_without_animation_genre() {
        val meta = MetaDetails(id = "kitsu:123", type = "series", name = "X", genres = listOf("Drama"))
        val result = classify(meta.toTitleFacts())
        assertEquals(true, result.animationEvidence)
        assertTrue(result.suggestedAnimationToggle)
        assertEquals(listOf("nuvio-anime-category"), result.matchedRuleIds)
        assertTrue(classify(TitleFacts(genres = setOf("Drama")).copy(categories = if (isNuvioAnimeCategory("tt1", "series", "anime")) setOf("anime") else emptySet())).suggestedAnimationToggle)
    }

    @Test
    fun animation_genre_outside_anime_category() {
        val result = classify(TitleFacts(genres = setOf("  ANIMATION "), originCountries = setOf("us")))
        assertEquals(true, result.animationEvidence)
        assertEquals(listOf("animation-genre"), result.matchedRuleIds)
    }

    @Test
    fun crunchyroll_title_reference() {
        val result = classify(TitleFacts(services = setOf("Crunchyroll"), genres = setOf("Comedy")))
        assertEquals(listOf("crunchyroll-title-reference"), result.matchedRuleIds)
        assertTrue(result.suggestedAnimationToggle)
    }

    @Test
    fun tokyo_mx_with_japanese_origin_uses_alias() {
        val meta = MetaDetails(
            id = "tmdb:1", type = "series", name = "X", genres = listOf("Drama"),
            networks = listOf(MetaCompany("Tokyo   Metropolitan Television")), country = "Japan",
        )
        assertEquals(listOf("tokyo-mx-japanese-origin"), classify(meta.toTitleFacts()).matchedRuleIds)
        assertEquals(false, classify(TitleFacts(networks = setOf("TOKYO MX"), originCountries = setOf("US"))).animationEvidence)
    }

    @Test
    fun japanese_live_action_alone_is_not_positive() {
        val result = classify(TitleFacts(genres = setOf("Drama"), originCountries = setOf("JP")))
        assertEquals(false, result.animationEvidence)
        assertFalse(result.suggestedAnimationToggle)
        assertEquals(EnhancementEligibility(animeJaNai = false, svp = true), effectiveEnhancementEligibility(null, result))
    }

    @Test
    fun disney_animated_movie_suggests_off_keeps_evidence_and_excludes_svp() {
        val meta = MetaDetails(
            id = "tmdb:2", type = "movie", name = "Y", genres = listOf("Animation", "Family"),
            productionCompanies = listOf(MetaCompany("Pixar Animation Studios")),
        )
        val result = classify(meta.toTitleFacts())
        assertEquals(true, result.animationEvidence)
        assertFalse(result.suggestedAnimationToggle)
        assertEquals(listOf("disney-produced-animated-movie"), result.matchedExclusionIds)
        assertEquals(EnhancementEligibility(animeJaNai = false, svp = false), effectiveEnhancementEligibility(null, result))
        // Disney live action: exclusion requires animation evidence.
        val live = classify(TitleFacts(genres = setOf("Adventure"), studios = setOf("Walt Disney Pictures"), contentType = "movie"))
        assertEquals(emptyList(), live.matchedExclusionIds)
        assertEquals(false, live.animationEvidence)
    }

    @Test
    fun unrelated_anime_streamed_on_disney_plus_is_not_excluded() {
        // A Japanese studio's anime series that Disney+ only distributes (no Disney studio).
        val result = classify(
            TitleFacts(
                categories = setOf("anime"),
                genres = setOf("Animation", "Action"),
                services = setOf("Disney+"),
                networks = setOf("Disney+"),
                originCountries = setOf("JP"),
                studios = setOf("MAPPA"),
                contentType = "series",
            ),
        )
        assertEquals(true, result.animationEvidence)
        assertTrue(result.suggestedAnimationToggle)
        assertEquals(emptyList(), result.matchedExclusionIds)
        assertEquals(EnhancementEligibility(animeJaNai = true, svp = false), effectiveEnhancementEligibility(null, result))
    }

    @Test
    fun unknown_metadata_is_unknown_and_starts_neither() {
        val result = classify(TitleFacts(contentType = "movie"))
        assertNull(result.animationEvidence)
        assertNull(classify(null).animationEvidence)
        assertEquals(EnhancementEligibility(false, false), effectiveEnhancementEligibility(null, result))
    }

    @Test
    fun explicit_values_override_classification() {
        val disney = classify(TitleFacts(genres = setOf("Animation"), studios = setOf("Disney"), contentType = "movie"))
        assertEquals(EnhancementEligibility(animeJaNai = true, svp = false), effectiveEnhancementEligibility(true, disney))
        val anime = classify(TitleFacts(categories = setOf("anime")))
        assertEquals(EnhancementEligibility(animeJaNai = false, svp = true), effectiveEnhancementEligibility(false, anime))
    }

    // --- repository / ledger ---

    private class MemoryFacts : ObservedTitleFactsStore {
        val map = mutableMapOf<String, ObservedTitleFacts>()
        override fun load(profileId: String, title: CanonicalTitle) = map["$profileId|${title.key}"]
        override fun save(profileId: String, title: CanonicalTitle, observed: ObservedTitleFacts) {
            map["$profileId|${title.key}"] = observed
        }
    }

    private var currentRules: ValidatedRuleSet = packaged

    private fun repo(ledger: AnimationDecisionLedger = AndroidAnimationDecisionLedger(tempDir.toFile()), facts: MemoryFacts = MemoryFacts()) =
        AnimationToggleRepository(ledger, facts, { currentRules }, now = { "2026-11-12T00:00:00Z" })

    private val disneyMovie = CanonicalTitle.of("tmdb:2", "movie")
    private val disneyFacts = TitleFacts(genres = setOf("Animation"), studios = setOf("Pixar"), contentType = "movie")

    @Test
    fun explicit_on_overrides_disney_and_records_ledger_event() = runTest {
        val repository = repo()
        repository.load("1")
        val state = repository.state("1", disneyMovie, disneyFacts)
        assertFalse(state.value.effectiveAnimation)
        assertTrue(repository.setExplicit("1", disneyMovie, "Y", true, AnimationToggleSurface.Details).isSuccess)
        assertEquals(true, state.value.explicitValue)
        assertTrue(state.value.effectiveAnimation)
        assertEquals(EnhancementEligibility(true, false), state.value.eligibility)
        val line = Files.readAllLines(tempDir.resolve("profile-1.jsonl")).single()
        val event = AnimationDecisionLedgerCodec.replay(line + "\n", "1").latest.values.single()
        assertEquals(listOf("disney-produced-animated-movie"), event.matchedExclusionIds)
        assertEquals(false, event.currentSuggestedValue)
        assertNull(event.previousExplicitValue)
        assertEquals(packaged.sha256, event.ruleSetSha256)
        assertEquals("details", event.surface)
        assertTrue(line.contains("\"previousExplicitValue\":null"))
    }

    @Test
    fun explicit_off_overrides_anime_and_selecting_suggestion_is_recorded() = runTest {
        val repository = repo()
        val anime = CanonicalTitle.of("kitsu:9", "series")
        repository.setExplicit("1", anime, "A", true, AnimationToggleSurface.PosterSheet, TitleFacts(categories = setOf("anime")))
        repository.setExplicit("1", anime, "A", false, AnimationToggleSurface.Player)
        val replay = AndroidAnimationDecisionLedger(tempDir.toFile()).replay("1")
        val event = replay.latest.getValue(anime.key)
        assertFalse(event.animation)
        assertEquals(true, event.previousExplicitValue)
        assertEquals(2, Files.readAllLines(tempDir.resolve("profile-1.jsonl")).size)
        assertEquals(EnhancementEligibility(false, true), repository.state("1", anime).value.eligibility)
    }

    @Test
    fun rule_update_preserves_explicit_choice_and_profiles_are_isolated() = runTest {
        val repository = repo()
        val title = CanonicalTitle.of("tmdb:5", "movie")
        val facts = TitleFacts(genres = setOf("Drama"))
        repository.setExplicit("1", title, "Z", true, AnimationToggleSurface.Details, facts)
        currentRules = packaged.copy(rules = rules.copy(ruleSetVersion = 3, animationIndicators = emptyList()), sha256 = "x")
        repository.refreshAll()
        assertEquals(true, repository.state("1", title).value.explicitValue)
        val fresh = repo()
        fresh.load("1"); fresh.load("2")
        assertEquals(true, fresh.state("1", title).value.explicitValue)
        assertNull(fresh.state("2", title).value.explicitValue)
    }

    @Test
    fun a_surface_observed_before_the_ledger_loads_receives_the_explicit_choice() = runTest {
        val title = CanonicalTitle.of("tt2948356", "movie")
        repo().setExplicit("1", title, "Zootopia", true, AnimationToggleSurface.Details, null)
        val fresh = repo()
        val observed = fresh.state("1", title)
        assertFalse(observed.value.loaded)
        assertNull(observed.value.explicitValue)
        fresh.load("1")
        assertEquals(true, observed.value.explicitValue)
    }

    @Test
    fun episodes_key_to_series_and_category_preserved_across_launch_routes() = runTest {
        val facts = MemoryFacts()
        val repository = repo(facts = facts)
        val series = CanonicalTitle.of("kitsu:7", "series")
        repository.state("1", series, TitleFacts(categories = setOf("anime"), genres = setOf("Drama")))
        // Continue Watching later: only partial metadata, keyed to the parent series.
        val cw = repository.state("1", CanonicalTitle.of("kitsu:7", "series"), TitleFacts(genres = setOf("Drama")))
        assertTrue(cw.value.suggestion)
        repository.setExplicit("1", series, "S", false, AnimationToggleSurface.ContinueWatchingSheet)
        assertEquals(false, cw.value.explicitValue)
        assertEquals(CanonicalTitle("kitsu:7", "series"), CanonicalTitle.of(" kitsu:7", "anime"))
    }

    @Test
    fun interrupted_trailing_record_is_ignored_and_next_append_keeps_history() = runTest {
        val ledger = AndroidAnimationDecisionLedger(tempDir.toFile())
        val repository = repo(ledger)
        val title = CanonicalTitle.of("tmdb:5", "movie")
        repository.setExplicit("1", title, "Z", true, AnimationToggleSurface.Details)
        val file = tempDir.resolve("profile-1.jsonl")
        Files.writeString(file, "{\"schemaVersion\":1,\"eventId\":\"par", java.nio.file.StandardOpenOption.APPEND)
        val replay = ledger.replay("1")
        assertTrue(replay.truncatedTrailingRecord)
        assertNull(replay.error)
        assertEquals(true, replay.latest.getValue(title.key).animation)
        repository.setExplicit("1", title, "Z", false, AnimationToggleSurface.Details)
        val after = ledger.replay("1")
        assertNull(after.error)
        assertEquals(false, after.latest.getValue(title.key).animation)
        assertEquals(2, Files.readAllLines(file).size)
    }

    @Test
    fun malformed_earlier_record_is_surfaced() = runTest {
        val ledger = AndroidAnimationDecisionLedger(tempDir.toFile())
        val title = CanonicalTitle.of("tmdb:5", "movie")
        repo(ledger).setExplicit("1", title, "Z", true, AnimationToggleSurface.Details)
        val file = tempDir.resolve("profile-1.jsonl")
        val good = Files.readString(file)
        Files.writeString(file, "garbage\n$good")
        val replay = ledger.replay("1")
        assertEquals(listOf(1), replay.corruptLines)
        assertNotNull(replay.error)
        val repository = repo(ledger)
        repository.load("1")
        assertNotNull(repository.state("1", title).value.error)
    }

    @Test
    fun concurrent_writes_are_serialized() = runTest {
        val repository = repo()
        (0 until 40).map { index ->
            async {
                repository.setExplicit("1", CanonicalTitle.of("tmdb:$index", "movie"), "T$index", index % 2 == 0, AnimationToggleSurface.Player)
            }
        }.awaitAll()
        val lines = Files.readAllLines(tempDir.resolve("profile-1.jsonl"))
        assertEquals(40, lines.size)
        val replay = AndroidAnimationDecisionLedger(tempDir.toFile()).replay("1")
        assertNull(replay.error)
        assertEquals(40, replay.latest.size)
    }

    @Test
    fun write_failure_keeps_previous_value_and_surfaces_error() = runTest {
        val failing = object : AnimationDecisionLedger {
            override suspend fun append(event: AnimationDecisionEvent) = throw java.io.IOException("disk full")
            override suspend fun replay(profileId: String) = LedgerReplay(emptyMap())
        }
        val repository = repo(failing)
        val title = CanonicalTitle.of("tmdb:5", "movie")
        assertTrue(repository.setExplicit("1", title, "Z", true, AnimationToggleSurface.Details).isFailure)
        val state = repository.state("1", title).value
        assertNull(state.explicitValue)
    }

    @Test
    fun invalid_rules_are_rejected_with_precise_error() {
        val badField = ContentIndicatorRuleParser.parse(packagedText.replace("\"field\": \"genres\"", "\"field\": \"genre\""))
        assertTrue(badField.exceptionOrNull()!!.message!!.contains("field 'genre' is unknown"), badField.exceptionOrNull()?.message)
        val notJson = ContentIndicatorRuleParser.parse("{ not json")
        assertTrue(notJson.exceptionOrNull()!!.message!!.contains("Invalid rule JSON"))
    }
}
