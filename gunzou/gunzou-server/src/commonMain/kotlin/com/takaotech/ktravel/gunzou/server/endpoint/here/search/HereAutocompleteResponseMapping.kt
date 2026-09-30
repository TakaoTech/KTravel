package com.takaotech.ktravel.gunzou.server.endpoint.here.search

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.search.SearchHighlights
import com.takaotech.gunzou.api.search.TextRange
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteResponse
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteSuggestion
import com.takaotech.gunzou.api.search.autocomplete.QuerySuggestionKind
import com.takaotech.gunzou.api.search.autocomplete.TermSuggestion
import com.takaotech.gunzou.here.search.autosuggest.dto.response.AutosuggestResponse
import com.takaotech.gunzou.here.search.autosuggest.dto.response.AutosuggestResultItem
import com.takaotech.gunzou.here.search.common.dto.highlight.HighlightRange
import com.takaotech.gunzou.here.search.common.dto.highlight.Highlights
import io.ktor.http.parseUrl

// What this server makes of a HERE Autosuggest answer.

private const val CATEGORY_QUERY = "categoryQuery"
private const val CHAIN_QUERY = "chainQuery"
private const val QUERY_SUFFIX = "Query"
private const val FOLLOW_UP_QUERY_PARAMETER = "q"

/** Translates a HERE Autosuggest answer into the provider neutral autocomplete answer. */
internal fun AutosuggestResponse.toAutocompleteResponse(): AutocompleteResponse = AutocompleteResponse(
    suggestions = items.mapNotNull { it.toSuggestion() },
    termSuggestions = queryTerms.map { term ->
        TermSuggestion(term = term.term, replaces = term.replaces, start = term.start, end = term.end)
    },
)

private fun AutosuggestResultItem.toSuggestion(): AutocompleteSuggestion? = when (this) {
    is AutosuggestResultItem.Entity -> toPlace()
    is AutosuggestResultItem.Query -> toQuery()
}

/**
 * A place, or nothing when HERE sent one without a position.
 *
 * The contract promises a pin on every place, and a suggestion that cannot be put on the map is not
 * one worth offering: dropping it is better than a position a client would have to special case.
 */
private fun AutosuggestResultItem.Entity.toPlace(): AutocompleteSuggestion.Place? {
    val position = position ?: return null

    return AutocompleteSuggestion.Place(
        id = id,
        title = title,
        resultType = hereResultType(resultType, localityType),
        position = GeoPoint(lat = position.lat, lng = position.lng),
        address = address.toSearchAddress(),
        category = categories?.toCategoryGroup(),
        distanceMeters = distance,
        bounds = mapView?.toGeoBounds(),
        highlights = highlights?.toSearchHighlights(),
    )
}

private fun Highlights.toSearchHighlights(): SearchHighlights = SearchHighlights(
    title = title.orEmpty().map { it.toTextRange() },
    addressLabel = address?.label.orEmpty().map { it.toTextRange() },
)

private fun HighlightRange.toTextRange(): TextRange = TextRange(start = start, end = end)

/**
 * A category or chain search, or nothing when HERE sent a kind of query it does not name.
 *
 * The follow-up URL is read for its search text and then dropped: it names HERE's host and
 * parameters, which the contract keeps away from a client.
 */
private fun AutosuggestResultItem.Query.toQuery(): AutocompleteSuggestion.Query? {
    val kind = when (resultType) {
        CATEGORY_QUERY -> QuerySuggestionKind.CATEGORY

        CHAIN_QUERY -> QuerySuggestionKind.CHAIN

        null -> return null

        // A kind added after this was written, passed through in the same spelling as the others.
        else -> QuerySuggestionKind(resultType.orEmpty().removeSuffix(QUERY_SUFFIX))
    }

    return AutocompleteSuggestion.Query(
        id = id,
        title = title,
        kind = kind,
        searchText = href?.let(::parseUrl)?.parameters?.get(FOLLOW_UP_QUERY_PARAMETER),
        highlights = highlights?.toSearchHighlights(),
    )
}
