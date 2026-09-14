package com.takaotech.gunzou.here.search.autocomplete.dto.request

import com.takaotech.gunzou.here.common.model.Coordinate
import com.takaotech.gunzou.here.search.autocomplete.model.AutocompleteShowOption
import com.takaotech.gunzou.here.search.autocomplete.model.AutocompleteType
import com.takaotech.gunzou.here.search.autocomplete.model.PostalCodeMode
import com.takaotech.gunzou.here.search.common.model.SearchArea
import com.vanniktech.locale.Locale

/**
 * Request parameters for the Autocomplete endpoint: completes the keystrokes typed so far into
 * street addresses and administrative areas. Places are not returned.
 *
 * @property query Text typed so far, such as `Berlin Pariser 20`. Whitespace alone, URLs and email
 *   addresses yield no results.
 * @property at Centre of the search context, used to rank the results
 * @property area Area the results must lie in. A [SearchArea.Circle] or a [SearchArea.BoundingBox]
 *   cannot be combined with [at]; [SearchArea.Countries] can.
 * @property postalCodeMode How to complete a postal code shared by several cities or districts
 * @property types Kinds of result to limit the completion to
 * @property lang Preferred languages for the response, most preferred first. HERE answers in the
 *   language it detects in the query, and falls back on these only for the parts that did not
 *   match or are ambiguous.
 * @property limit Maximum number of results, 1 to 20. HERE defaults to 5.
 * @property politicalView ISO 3166-1 alpha-3 code of the political view to render disputed areas
 *   in, such as `ARG`
 * @property show Optional sections to add to each result
 * @property requestId Identifier echoed in error responses, sent as the `X-Request-ID` header, to
 *   correlate a request with its response in logs. A UUID is recommended.
 */
data class AutocompleteRequest(
    val query: String,
    val at: Coordinate? = null,
    val area: SearchArea? = null,
    val postalCodeMode: PostalCodeMode? = null,
    val types: List<AutocompleteType>? = null,
    val lang: List<Locale>,
    val limit: Int? = null,
    val politicalView: String? = null,
    val show: List<AutocompleteShowOption>? = null,
    val requestId: String? = null,
) {
    init {
        require(query.isNotBlank()) { "Query cannot be blank" }
        require(at == null || area == null || area is SearchArea.Countries) {
            "A circle or a bounding box cannot be combined with a search centre"
        }
        require(lang.isNotEmpty()) { "Language list cannot be empty" }
        require(limit == null || limit in LIMIT_RANGE) { "Limit must be in $LIMIT_RANGE: $limit" }
    }

    private companion object {
        val LIMIT_RANGE = 1..20
    }
}
