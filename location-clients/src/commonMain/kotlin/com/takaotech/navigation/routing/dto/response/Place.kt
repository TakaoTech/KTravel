package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * A place represents a generic location relevant for the route.
 *
 * @property type Place type identifier (always "place" for this type)
 * @property location The position used in route calculation
 * @property originalLocation Original position provided in the request (if different)
 * @property displayLocation Display position for POI visualization
 * @property name Location name
 * @property waypoint Index of the corresponding via waypoint in the request
 * @property sideOfStreet Location relative to driving direction ("left" or "right")
 */
@Serializable
data class Place(
    val type: String,
    val location: Location,
    val originalLocation: Location? = null,
    val displayLocation: Location? = null,
    val name: String? = null,
    val waypoint: Int? = null,
    val sideOfStreet: String? = null
)
