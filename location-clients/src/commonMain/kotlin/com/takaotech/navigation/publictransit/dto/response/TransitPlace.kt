package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.Serializable

/**
 * Represents a place (station, stop, or location) in a transit route.
 */
@Serializable
data class TransitPlace(
    val name: String? = null,
    val type: String? = null,
    val location: Location? = null,
    val id: String? = null
)
