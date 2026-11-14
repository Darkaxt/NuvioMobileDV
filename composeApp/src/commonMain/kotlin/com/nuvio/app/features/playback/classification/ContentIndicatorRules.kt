package com.nuvio.app.features.playback.classification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Spec P1: `playback-content-indicators.json` rule artifact (schemaVersion 2). */
@Serializable
data class ContentIndicatorRuleSet(
    val schemaVersion: Int,
    val ruleSetVersion: Int,
    val status: String? = null,
    val normalization: RuleNormalization = RuleNormalization(),
    val precedence: List<String> = emptyList(),
    val animationIndicators: List<AnimationIndicatorRule> = emptyList(),
    @SerialName("automaticAnimeJaNaiExclusions")
    val automaticAnimeJaNaiExclusions: List<AnimeJaNaiExclusionRule> = emptyList(),
    val supportingEvidenceOnly: List<SupportingEvidenceRule> = emptyList(),
    val review: kotlinx.serialization.json.JsonObject? = null,
)

@Serializable
data class RuleNormalization(
    val strings: String? = null,
    val fields: Map<String, String> = emptyMap(),
    /** field -> canonical value -> aliases. */
    val aliases: Map<String, Map<String, List<String>>> = emptyMap(),
)

@Serializable
data class RuleCondition(
    val field: String,
    val anyOf: List<String>,
)

@Serializable
data class AnimationIndicatorRule(
    val id: String,
    val enabled: Boolean = true,
    val matchAll: List<RuleCondition>,
    val animation: Boolean = true,
)

@Serializable
data class AnimeJaNaiExclusionRule(
    val id: String,
    val enabled: Boolean = true,
    val requiresAnimationEvidence: Boolean = true,
    val matchAll: List<RuleCondition>,
    val suggestedAnimationToggle: Boolean = false,
    val preserveAnimationEvidenceForSvpExclusion: Boolean = true,
    val explicitUserChoiceOverrides: Boolean = true,
)

@Serializable
data class SupportingEvidenceRule(
    val id: String,
    val field: String,
    val anyOf: List<String>,
)

/** A rule set that passed parsing and validation, with the SHA-256 of its exact bytes. */
data class ValidatedRuleSet(
    val rules: ContentIndicatorRuleSet,
    val sha256: String,
)

object ContentIndicatorRuleParser {
    const val SUPPORTED_SCHEMA_VERSION = 2

    val FIELDS = setOf(
        "categories", "genres", "services", "networks", "originCountries", "studios", "contentType",
    )

    private val json = Json { ignoreUnknownKeys = false }

    /** Parses and validates; failure message is precise enough to show the user. */
    fun parse(text: String): Result<ContentIndicatorRuleSet> {
        val rules = try {
            json.decodeFromString(ContentIndicatorRuleSet.serializer(), text)
        } catch (error: Exception) {
            return Result.failure(IllegalArgumentException("Invalid rule JSON: ${error.message}", error))
        }
        val problems = validate(rules)
        return if (problems.isEmpty()) {
            Result.success(rules)
        } else {
            Result.failure(IllegalArgumentException("Invalid rule set: ${problems.joinToString("; ")}"))
        }
    }

    fun validate(rules: ContentIndicatorRuleSet): List<String> {
        val problems = mutableListOf<String>()
        if (rules.schemaVersion != SUPPORTED_SCHEMA_VERSION) {
            problems += "schemaVersion ${rules.schemaVersion} is not supported (expected $SUPPORTED_SCHEMA_VERSION)"
        }
        if (rules.ruleSetVersion < 1) problems += "ruleSetVersion must be >= 1"
        rules.normalization.aliases.keys.filter { it !in FIELDS }.forEach {
            problems += "normalization.aliases has unknown field '$it'"
        }
        val ids = mutableSetOf<String>()
        fun checkId(path: String, id: String) {
            if (id.isBlank()) problems += "$path.id is blank"
            else if (!ids.add(id)) problems += "$path.id '$id' is duplicated"
        }
        fun checkConditions(path: String, conditions: List<RuleCondition>) {
            if (conditions.isEmpty()) problems += "$path.matchAll is empty"
            conditions.forEachIndexed { index, condition ->
                if (condition.field !in FIELDS) problems += "$path.matchAll[$index].field '${condition.field}' is unknown"
                if (condition.anyOf.isEmpty() || condition.anyOf.any { it.isBlank() }) {
                    problems += "$path.matchAll[$index].anyOf must contain non-blank values"
                }
            }
        }
        rules.animationIndicators.forEachIndexed { index, rule ->
            val path = "animationIndicators[$index]"
            checkId(path, rule.id)
            checkConditions(path, rule.matchAll)
        }
        rules.automaticAnimeJaNaiExclusions.forEachIndexed { index, rule ->
            val path = "automaticAnimeJaNaiExclusions[$index]"
            checkId(path, rule.id)
            checkConditions(path, rule.matchAll)
            if (!rule.explicitUserChoiceOverrides) {
                problems += "$path.explicitUserChoiceOverrides must be true (explicit choice is final)"
            }
            if (!rule.preserveAnimationEvidenceForSvpExclusion) {
                problems += "$path.preserveAnimationEvidenceForSvpExclusion must be true"
            }
        }
        rules.supportingEvidenceOnly.forEachIndexed { index, rule ->
            val path = "supportingEvidenceOnly[$index]"
            checkId(path, rule.id)
            if (rule.field !in FIELDS) problems += "$path.field '${rule.field}' is unknown"
        }
        return problems
    }
}
