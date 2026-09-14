package com.takaotech.gunzou.here.search.browser.dto.request

import com.takaotech.gunzou.here.common.model.Coordinate
import com.takaotech.gunzou.here.search.browser.model.BrowseShowOption
import com.takaotech.gunzou.here.search.common.category.PlaceCategory
import com.takaotech.gunzou.here.search.common.model.SearchArea
import com.takaotech.gunzou.here.search.common.model.SearchFilter
import com.vanniktech.locale.Locale

/**
 * Request parameters for the Browse endpoint: places around a point, filtered and ranked by
 * distance.
 *
 * @property at Centre of the search, results are ranked by their distance from it
 * @property categories Place categories to include or exclude, at any level of the category tree
 * @property foodTypes Cuisine ids of the HERE Places Cuisine System to include or exclude. Kept as
 *   raw ids: this client has no table of cuisines.
 * @property area Area the results must lie in
 * @property name Text a result's name must partially match
 * @property lang Preferred languages for the response, most preferred first
 * @property limit Maximum number of results, 1 to 100. HERE defaults to 20.
 * @property offset Turns pagination on and selects the page to start from, 0 to 99. With it,
 *   [limit] is the page size and the response carries the pagination fields.
 * @property politicalView ISO 3166-1 alpha-3 code of the political view to render disputed areas
 *   in, such as `ARG`
 * @property show Optional sections to add to each result
 * @property requestId Identifier echoed in error responses, sent as the `X-Request-ID` header, to
 *   correlate a request with its response in logs. A UUID is recommended.
 */
data class BrowseRequest(
    val at: Coordinate,
    val categories: SearchFilter<PlaceCategory>? = null,
    val foodTypes: SearchFilter<String>? = null,
    val area: SearchArea? = null,
    val name: String? = null,
    val lang: List<Locale>,
    val limit: Int? = null,
    val offset: Int? = null,
    val politicalView: String? = null,
    val show: List<BrowseShowOption>? = null,
    val requestId: String? = null,
) {
    init {
        require(lang.isNotEmpty()) { "Language list cannot be empty" }
        require(limit == null || limit in LIMIT_RANGE) { "Limit must be in $LIMIT_RANGE: $limit" }
        require(offset == null || offset in OFFSET_RANGE) {
            "Offset must be in $OFFSET_RANGE: $offset"
        }
    }

    private companion object {
        val LIMIT_RANGE = 1..100
        val OFFSET_RANGE = 0..99
    }
}
