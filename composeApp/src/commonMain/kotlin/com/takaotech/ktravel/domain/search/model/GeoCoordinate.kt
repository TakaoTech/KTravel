package com.takaotech.ktravel.domain.search.model

/**
 * A point on the map, in degrees.
 *
 * Its own type rather than the contract's `GeoPoint`, so nothing above the data layer learns the
 * navigator's wire shapes.
 *
 * @property lat Latitude, from -90 (south) to 90 (north).
 * @property lng Longitude, from -180 (west) to 180 (east).
 */
// TODO Centralize latLng Object
data class GeoCoordinate(val lat: Double, val lng: Double)
