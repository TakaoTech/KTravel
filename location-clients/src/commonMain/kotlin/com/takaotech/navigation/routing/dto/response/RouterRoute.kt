package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * A complete route from origin to destination.
 *
 * @property id Unique identifier of the route
 * @property sections Ordered list of sections making up the route
 * @property notices List of notices/warnings for this route
 * @property routeHandle Opaque handle for route reconstruction
 */
@Serializable
data class RouterRoute(
    val id: String,
    val sections: List<VehicleSection>,
    val notices: List<Notice>? = null,
    val routeHandle: String? = null
)
