package com.takaotech.gunzou.here.common.model

/**
 * A point on the Earth, in the `lat,lng` form every HERE API expects in a query string.
 *
 * @property lat Latitude in WGS84 format
 * @property lng Longitude in WGS84 format
 */
data class Coordinate(val lat: Double, val lng: Double) {
    /**
     * Converts the coordinate to the HERE API string format: `{lat},{lng}`.
     */
    fun toQueryString(): String = "$lat,$lng"

    /** Parsing helpers. */
    companion object {
        /**
         * Parses a coordinate string in the `lat,lng` form.
         *
         * @throws IllegalArgumentException if the format is invalid
         */
        fun fromString(coordinate: String): Coordinate {
            val parts = coordinate.split(",")
            require(parts.size >= 2) { "Invalid coordinate format. Expected 'lat,lng'" }

            return Coordinate(
                lat = parts[0].toDouble(),
                lng = parts[1].toDouble(),
            )
        }
    }
}
