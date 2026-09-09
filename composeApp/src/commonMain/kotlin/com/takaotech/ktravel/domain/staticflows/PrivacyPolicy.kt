package com.takaotech.ktravel.domain.staticflows

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** What separates a section from the one it lives in, in a path such as `collection.automatic`. */
const val PRIVACY_PATH_SEPARATOR: Char = '.'

/**
 * The privacy policy, as it is shipped: a versioned tree of sections.
 *
 * Packaged as `composeResources/files/privacy/privacy_policy_<language>.json`, for the same reasons
 * [IntroFlow] is: the version travels with the text, because an answer about telemetry is given to a
 * version of the policy, and a newer one has to be shown again.
 *
 * Every section is addressable: [section] takes the path of ids joined by [PRIVACY_PATH_SEPARATOR],
 * which is what lets the introduction point at one paragraph and what lets the document scroll to it.
 *
 * @property version The version of the policy. Raising it shows the privacy step again and asks the
 *   telemetry question again.
 * @property language The language this file is written in, for diagnostics and tests.
 * @property sections The top level sections, in reading order.
 */
@Serializable
data class PrivacyPolicy(
    @SerialName("version")
    val version: Int,
    @SerialName("language")
    val language: String,
    @SerialName("sections")
    val sections: List<PrivacyPolicySection>,
)

/**
 * One section of the policy, and the sections under it.
 *
 * One type at every depth rather than a section and a subsection type: the two would carry the same
 * three fields, and a path such as `collection.automatic.limits` stops being expressible the moment the
 * depth is fixed by the model.
 *
 * @property id Names the section among its siblings. Stable across translations, and what the path
 *   is built from.
 * @property title The heading, in the language of the file.
 * @property body The text, in **Markdown**, rendered the way trip notes are.
 * @property subsections What is nested under it, in reading order; empty when it is a leaf.
 */
@Serializable
data class PrivacyPolicySection(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("body")
    val body: String,
    @SerialName("subsections")
    val subsections: List<PrivacyPolicySection> = emptyList(),
)

/**
 * The section at [path], or null when nothing sits there.
 *
 * @param path Ids joined by [PRIVACY_PATH_SEPARATOR], e.g. `collection.automatic`. A blank path, an empty
 *   segment or an id that does not exist all resolve to null: the path comes from content, and
 *   content that has fallen out of step must not be able to fail anything.
 */
fun PrivacyPolicy.section(path: String): PrivacyPolicySection? {
    val segments = path.split(PRIVACY_PATH_SEPARATOR)
    if (segments.any { it.isBlank() }) return null

    return segments.fold(null as PrivacyPolicySection?) { parent, id ->
        val candidates = parent?.subsections ?: sections

        candidates.firstOrNull { it.id == id } ?: return null
    }
}

/**
 * Every path in the policy, parents before their children, in reading order.
 *
 * What the document lays out, and what a test walks to check that the introduction points at
 * sections that exist.
 */
fun PrivacyPolicy.paths(): List<String> = sections.flatMap { it.paths(prefix = "") }

private fun PrivacyPolicySection.paths(prefix: String): List<String> {
    val path = if (prefix.isEmpty()) id else "$prefix$PRIVACY_PATH_SEPARATOR$id"

    return listOf(path) + subsections.flatMap { it.paths(prefix = path) }
}
