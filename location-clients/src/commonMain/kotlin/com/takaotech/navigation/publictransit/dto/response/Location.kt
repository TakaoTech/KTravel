package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.Serializable

/**
 * Location on the Earth with latitude and longitude coordinates.
 */
@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)
