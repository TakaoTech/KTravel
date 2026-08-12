package com.takaotech.navigation.common.dto

import kotlinx.serialization.Serializable

/**
 * Geographic location on Earth.
 *
 * @property lat Latitude in decimal degrees (-90 to 90)
 * @property lng Longitude in decimal degrees (-180 to 180)
 * @property elv Ellipsoid (geodetic) height in meters, returned by the Routing API only
 */
@Serializable
data class Location(val lat: Double, val lng: Double, val elv: Double? = null)
