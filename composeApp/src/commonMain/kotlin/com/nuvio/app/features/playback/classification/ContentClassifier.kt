package com.nuvio.app.features.playback.classification

import com.nuvio.app.features.details.MetaDetails
import com.nuvio.app.features.player.isAnimeNamespaceId
import kotlinx.serialization.Serializable

/** Title-level normalized facts (spec P1 fields). Empty sets mean "not provided". */
@Serializable
data class TitleFacts(
    val categories: Set<String> = emptySet(),
    val genres: Set<String> = emptySet(),
    val services: Set<String> = emptySet(),
    val networks: Set<String> = emptySet(),
    val originCountries: Set<String> = emptySet(),
    val studios: Set<String> = emptySet(),
    val contentType: String? = null,
) {
    /** Metadata is unknown when no classification-bearing field was provided. */
    val hasClassificationMetadata: Boolean
        get() = categories.isNotEmpty() || genres.isNotEmpty() || services.isNotEmpty() ||
            networks.isNotEmpty() || originCountries.isNotEmpty() || studios.isNotEmpty()

    fun field(name: String): Set<String> = when (name) {
        "categories" -> categories
        "genres" -> genres
        "services" -> services
        "networks" -> networks
        "originCountries" -> originCountries
        "studios" -> studios
        "contentType" -> setOfNotNull(contentType)
        else -> emptySet()
    }

    /** Keeps known facts when a later launch route provides less (e.g. Continue Watching). */
    fun mergedWithPrevious(previous: TitleFacts?): TitleFacts {
        if (previous == null) return this
        fun pick(current: Set<String>, old: Set<String>) = current.ifEmpty { old }
        return TitleFacts(
            categories = categories + previous.categories,
            genres = pick(genres, previous.genres),
            services = pick(services, previous.services),
            networks = pick(networks, previous.networks),
            originCountries = pick(originCountries, previous.originCountries),
            studios = pick(studios, previous.studios),
            contentType = contentType ?: previous.contentType,
        )
    }
}

/** Separate rule outputs (spec P1 last paragraph). */
data class ContentClassification(
    /** true = animation evidence, false = known metadata without evidence, null = unknown. */
    val animationEvidence: Boolean?,
    val suggestedAnimationToggle: Boolean,
    val matchedRuleIds: List<String>,
    val matchedExclusionIds: List<String>,
)

fun normalizeFactString(value: String): String =
    value.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ")

object ContentClassifier {
    fun normalize(facts: TitleFacts, rules: ContentIndicatorRuleSet): TitleFacts {
        val aliasLookup: Map<String, Map<String, String>> = rules.normalization.aliases.mapValues { (_, canon) ->
            buildMap {
                canon.forEach { (canonical, aliases) ->
                    val target = normalizeFactString(canonical)
                    put(target, target)
                    aliases.forEach { put(normalizeFactString(it), target) }
                }
            }
        }
        fun norm(field: String, values: Set<String>): Set<String> {
            val aliases = aliasLookup[field].orEmpty()
            return values.map(::normalizeFactString).filter { it.isNotEmpty() }
                .map { aliases[it] ?: it }.toSet()
        }
        return TitleFacts(
            categories = norm("categories", facts.categories),
            genres = norm("genres", facts.genres),
            services = norm("services", facts.services),
            networks = norm("networks", facts.networks),
            originCountries = norm("originCountries", facts.originCountries),
            studios = norm("studios", facts.studios),
            contentType = facts.contentType?.let(::normalizeFactString)?.ifEmpty { null },
        )
    }

    fun evaluate(facts: TitleFacts?, rules: ContentIndicatorRuleSet): ContentClassification {
        val normalized = facts?.let { normalize(it, rules) }
        if (normalized == null || !normalized.hasClassificationMetadata) {
            return ContentClassification(null, false, emptyList(), emptyList())
        }
        fun matches(conditions: List<RuleCondition>) = conditions.all { condition ->
            val wanted = condition.anyOf.map(::normalizeFactString).toSet()
            normalized.field(condition.field).any { it in wanted }
        }
        val matchedIndicators = rules.animationIndicators.filter { it.enabled && matches(it.matchAll) }
        val evidence = matchedIndicators.any { it.animation }
        val matchedExclusions = rules.automaticAnimeJaNaiExclusions.filter { rule ->
            rule.enabled && (!rule.requiresAnimationEvidence || evidence) && matches(rule.matchAll)
        }
        val suggestion = when {
            matchedExclusions.isNotEmpty() -> matchedExclusions.first().suggestedAnimationToggle
            else -> evidence
        }
        return ContentClassification(
            animationEvidence = evidence,
            suggestedAnimationToggle = suggestion,
            matchedRuleIds = matchedIndicators.map { it.id },
            matchedExclusionIds = matchedExclusions.map { it.id },
        )
    }
}

/** Effective eligibility per spec P2 (before global switches / technical feasibility). */
data class EnhancementEligibility(val animeJaNai: Boolean, val svp: Boolean)

fun effectiveEnhancementEligibility(
    explicitValue: Boolean?,
    classification: ContentClassification,
): EnhancementEligibility = when (explicitValue) {
    true -> EnhancementEligibility(animeJaNai = true, svp = false)
    false -> EnhancementEligibility(animeJaNai = false, svp = true)
    null -> when (classification.animationEvidence) {
        // Evidence excludes SVP even when an exclusion turned the suggestion Off.
        true -> EnhancementEligibility(animeJaNai = classification.suggestedAnimationToggle, svp = false)
        false -> EnhancementEligibility(animeJaNai = false, svp = true)
        null -> EnhancementEligibility(animeJaNai = false, svp = false)
    }
}

/** Canonical movie/series identity; episodes must pass their parent series id/type. */
data class CanonicalTitle(val titleId: String, val titleType: String) {
    val key: String get() = "$titleType|$titleId"

    companion object {
        fun of(parentMetaId: String, parentMetaType: String): CanonicalTitle =
            CanonicalTitle(parentMetaId.trim(), canonicalTitleType(parentMetaType))
    }
}

fun canonicalTitleType(metaType: String?): String =
    if (metaType?.trim()?.equals("movie", ignoreCase = true) == true) "movie" else "series"

/** "anime" category: anime id namespace, meta type "anime", or known library media category. */
fun isNuvioAnimeCategory(id: String?, metaType: String?, libraryMediaCategory: String? = null): Boolean =
    isAnimeNamespaceId(id) ||
        metaType?.trim()?.equals("anime", ignoreCase = true) == true ||
        libraryMediaCategory?.trim()?.equals("anime", ignoreCase = true) == true

fun countryTokensToIso(country: String?): Set<String> =
    country?.split(',')?.mapNotNull { token ->
        val value = token.trim()
        when {
            value.isEmpty() -> null
            value.equals("japan", ignoreCase = true) -> "jp"
            else -> value.lowercase()
        }
    }?.toSet().orEmpty()

fun MetaDetails.toTitleFacts(libraryMediaCategory: String? = null, services: Set<String> = emptySet()): TitleFacts =
    TitleFacts(
        categories = if (isNuvioAnimeCategory(id, type, libraryMediaCategory)) setOf("anime") else emptySet(),
        genres = genres.toSet(),
        services = services,
        networks = networks.map { it.name }.toSet(),
        originCountries = countryTokensToIso(country),
        studios = productionCompanies.map { it.name }.toSet(),
        contentType = canonicalTitleType(type),
    )
