package com.takaotech.gunzou.here.search.autosuggest.dto.request

import com.takaotech.gunzou.here.common.model.Coordinate
import com.takaotech.gunzou.here.search.autosuggest.model.AutosuggestShowOption
import com.takaotech.gunzou.here.search.common.model.SearchArea
import com.vanniktech.locale.Locale

/**
 * Request parameters for the Autosuggest endpoint: suggests places, addresses and category or chain
 * queries for an incomplete or misspelled query, for the user to pick from.
 *
 * The search context is required: either [at], or an [area] that is a [SearchArea.Circle] or a
 * [SearchArea.BoundingBox]. [SearchArea.Countries] narrows the results but does not locate them, so
 * it needs [at] as well.
 *
 * @property query Text typed so far, such as `restau` or `berlin bran`. Whitespace alone, URLs and
 *   email addresses yield no results.
 * @property at Centre of the search context, used to rank the results. Cannot be combined with a
 *   circle or a bounding box.
 * @property area Area the results must lie in
 * @property lang Preferred languages for the response, most preferred first
 * @property limit Maximum number of results, 1 to 100. HERE defaults to 20.
 * @property offset Turns pagination on and selects the page to start from, 0 to 99. With it,
 *   [limit] is the page size and the response carries the pagination fields. Alpha in HERE.
 * @property termsLimit Maximum number of query term suggestions, 0 to 10
 * @property show Optional sections to add to each result
 * @property requestId Identifier echoed in error responses, sent as the `X-Request-ID` header, to
 *   correlate a request with its response in logs. A UUID is recommended.
 */
data class AutosuggestRequest(
    val query: String,
    val at: Coordinate? = null,
    val area: SearchArea? = null,
    val lang: List<Locale>,
    val limit: Int? = null,
    val offset: Int? = null,
    val termsLimit: Int? = null,
    val show: List<AutosuggestShowOption>? = null,
    val requestId: String? = null,
) {
    init {
        require(query.isNotBlank()) { "Query cannot be blank" }
        val locatingArea = area is SearchArea.Circle || area is SearchArea.BoundingBox
        require(at == null || !locatingArea) {
            "A circle or a bounding box cannot be combined with a search centre"
        }
        require(at != null || locatingArea) {
            "A search centre, a circle or a bounding box is required"
        }
        require(lang.isNotEmpty()) { "Language list cannot be empty" }
        require(limit == null || limit in LIMIT_RANGE) { "Limit must be in $LIMIT_RANGE: $limit" }
        require(offset == null || offset in OFFSET_RANGE) {
            "Offset must be in $OFFSET_RANGE: $offset"
        }
        require(termsLimit == null || termsLimit in TERMS_LIMIT_RANGE) {
            "Terms limit must be in $TERMS_LIMIT_RANGE: $termsLimit"
        }
    }

    private companion object {
        val LIMIT_RANGE = 1..100
        val OFFSET_RANGE = 0..99
        val TERMS_LIMIT_RANGE = 0..10
    }
}
