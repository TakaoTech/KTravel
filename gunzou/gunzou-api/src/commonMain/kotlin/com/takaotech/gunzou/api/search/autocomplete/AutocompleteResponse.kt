package com.takaotech.gunzou.api.search.autocomplete

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body answered by every autocomplete path, whatever the provider behind it.
 *
 * An empty [suggestions] list is a valid answer, not a failure: nothing matching what was typed so
 * far is an ordinary moment of typing, and the next keystroke will ask again.
 *
 * @property suggestions What to offer the user, most relevant first.
 * @property termSuggestions Completions of single words of the query, to offer while the user is
 *   still typing. Always empty for a provider that does not
 *   [suggest queries][com.takaotech.gunzou.api.search.SearchProfileDescriptor.supportsQuerySuggestions].
 */
@Serializable
data class AutocompleteResponse(
    @SerialName("suggestions") val suggestions: List<AutocompleteSuggestion> = emptyList(),
    @SerialName("termSuggestions") val termSuggestions: List<TermSuggestion> = emptyList(),
)

/**
 * A suggested completion of one word of the query, such as `restaurant` for `restau`.
 *
 * @property term The word to offer the user.
 * @property replaces The part of the query it replaces.
 * @property start Index of the first replaced character of the query, 0-based and inclusive.
 * @property end Index one past the last replaced character of the query, 0-based and exclusive.
 */
@Serializable
data class TermSuggestion(
    @SerialName("term") val term: String,
    @SerialName("replaces") val replaces: String,
    @SerialName("start") val start: Int,
    @SerialName("end") val end: Int,
)
