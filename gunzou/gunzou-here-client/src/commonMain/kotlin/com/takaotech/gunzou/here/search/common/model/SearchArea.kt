package com.takaotech.gunzou.here.search.common.model

import com.takaotech.gunzou.here.common.model.Coordinate

/**
 * A geographic area a HERE search is restricted to, sent as the `in` query parameter.
 *
 * It is a hard filter: results outside the area are not returned.
 */
sealed class SearchArea {
    /** Converts the area to the form the `in` parameter expects. */
    abstract fun toQueryString(): String

    /**
     * One or more countries.
     *
     * @property codes ISO 3166-1 alpha-3 country codes, in uppercase, such as `ITA`
     */
    data class Countries(val codes: List<String>) : SearchArea() {
        init {
            require(codes.isNotEmpty()) { "At least one country code is required" }
            require(codes.all { it.matches(COUNTRY_CODE) }) {
                "Country codes must be ISO 3166-1 alpha-3, in uppercase: $codes"
            }
        }

        override fun toQueryString(): String = "countryCode:${codes.joinToString(",")}"
    }

    /**
     * A circle around a point.
     *
     * @property center Centre of the circle
     * @property radiusMeters Radius of the circle, in meters
     */
    data class Circle(val center: Coordinate, val radiusMeters: Int) : SearchArea() {
        init {
            require(radiusMeters > 0) { "The radius must be positive: $radiusMeters" }
        }

        override fun toQueryString(): String = "circle:${center.toQueryString()};r=$radiusMeters"
    }

    /**
     * A bounding box. Note the order HERE expects, longitude before latitude.
     *
     * @property west Longitude of the western side
     * @property south Latitude of the southern side
     * @property east Longitude of the eastern side
     * @property north Latitude of the northern side
     */
    data class BoundingBox(val west: Double, val south: Double, val east: Double, val north: Double) :
        SearchArea() {
        init {
            require(south <= north) { "The southern side must not be north of the northern side" }
        }

        override fun toQueryString(): String = "bbox:$west,$south,$east,$north"
    }

    private companion object {
        val COUNTRY_CODE = Regex("[A-Z]{3}")
    }
}
