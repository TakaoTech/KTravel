package com.takaotech.gunzou.here.search.revgeocode.dto.request

import com.takaotech.gunzou.here.common.model.Coordinate
import com.takaotech.gunzou.here.search.common.model.SearchArea
import com.takaotech.gunzou.here.search.revgeocode.model.RevgeocodeFeature
import com.takaotech.gunzou.here.search.revgeocode.model.RevgeocodeShowOption
import com.takaotech.gunzou.here.search.revgeocode.model.RevgeocodeType
import com.vanniktech.locale.Locale

/**
 * Request parameters for the Reverse Geocode endpoint: the addresses nearest to a point.
 *
 * Exactly one of [at] and [area] is set: HERE requires one and rejects both.
 *
 * @property at Point to find the nearest addresses to
 * @property area Circle the results must lie in, used instead of [at]. It is a hard filter.
 * @property bearing Direction the caller is heading in, in degrees clockwise from true north, 0 to
 *   359. With it every result is a street, and [types] can only be [RevgeocodeType.STREET].
 * @property types Kinds of result to limit the search to
 * @property features Features that are off by default, sent as the `with` query parameter
 * @property lang Preferred languages for the response, most preferred first
 * @property limit Maximum number of results, 1 to 100. HERE defaults to 1.
 * @property politicalView ISO 3166-1 alpha-3 code of the political view to render disputed areas
 *   in, such as `ARG`
 * @property show Optional sections to add to each result
 * @property requestId Identifier echoed in error responses, sent as the `X-Request-ID` header, to
 *   correlate a request with its response in logs. A UUID is recommended.
 */
data class RevgeocodeRequest(
    val at: Coordinate? = null,
    val area: SearchArea.Circle? = null,
    val bearing: Int? = null,
    val types: List<RevgeocodeType>? = null,
    val features: List<RevgeocodeFeature>? = null,
    val lang: List<Locale>,
    val limit: Int? = null,
    val politicalView: String? = null,
    val show: List<RevgeocodeShowOption>? = null,
    val requestId: String? = null,
) {
    init {
        require((at == null) != (area == null)) { "Exactly one of a point and a circle is required" }
        require(bearing == null || bearing in BEARING_RANGE) { "Bearing must be in $BEARING_RANGE: $bearing" }
        require(bearing == null || types.orEmpty().all { it == RevgeocodeType.STREET }) {
            "A bearing only accepts the street type: $types"
        }
        require(lang.isNotEmpty()) { "Language list cannot be empty" }
        require(limit == null || limit in LIMIT_RANGE) { "Limit must be in $LIMIT_RANGE: $limit" }
    }

    private companion object {
        val BEARING_RANGE = 0..359
        val LIMIT_RANGE = 1..100
    }
}
