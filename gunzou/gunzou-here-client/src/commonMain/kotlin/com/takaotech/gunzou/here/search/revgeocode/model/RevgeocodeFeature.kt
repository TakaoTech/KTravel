package com.takaotech.gunzou.here.search.revgeocode.model

/**
 * A feature of the Reverse Geocode endpoint that is off by default, turned on through the `with`
 * query parameter.
 *
 * @property value Representation the API expects in the query string
 */
enum class RevgeocodeFeature(val value: String) {
    /**
     * Micro point addresses: building, floor and suite details. Restricted to HERE contracts, and
     * only available in AUS, AUT, BRA, CAN, NZL and USA.
     */
    MICRO_POINT_ADDRESS("MPA"),

    /** Streets that have no name. */
    UNNAMED_STREETS("unnamedStreets"),

    /**
     * District and city names estimated from the closest street where the area data has none,
     * flagged by `estimatedAreaFallback` in the result. Alpha: near a border the estimate can name
     * the neighbouring area.
     */
    ESTIMATED_AREA_FALLBACK("estimatedAreaFallback"),
    ;

    /** Converts the feature to its query string representation. */
    fun toQueryString(): String = value
}
