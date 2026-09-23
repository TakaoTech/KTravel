package com.takaotech.ktravel.domain.search.model

/**
 * The rectangle of the map the traveller is looking at, in degrees.
 *
 * [west] can be greater than [east] when the rectangle crosses the antimeridian.
 *
 * @property west Longitude of the left edge.
 * @property south Latitude of the bottom edge.
 * @property east Longitude of the right edge.
 * @property north Latitude of the top edge.
 */
data class GeoArea(val west: Double, val south: Double, val east: Double, val north: Double) {

    /** The point halfway across the rectangle, across the antimeridian too. */
    val center: GeoCoordinate
        get() {
            val span = if (west <= east) east - west else east + FULL_TURN - west
            val lng = west + span / 2
            return GeoCoordinate(
                lat = (south + north) / 2,
                lng = if (lng > HALF_TURN) lng - FULL_TURN else lng,
            )
        }

    private companion object {
        const val HALF_TURN = 180.0
        const val FULL_TURN = 360.0
    }
}
