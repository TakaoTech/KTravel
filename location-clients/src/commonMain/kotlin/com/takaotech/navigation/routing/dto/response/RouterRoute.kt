package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * A route connects a given `origin` with a given `destination` (possibly via one or more `via` waypoints) and is made up of one or more sections.
 *
 * @property id Unique identifier of the route
 * @property sections A route is made up of one or more sections.
 * @property notices Contains a list of issues related to this route. Notices must be carefully evaluated and, if deemed necessary, the route should be discarded accordingly.
 * @property routeHandle Route handle of the route. Can be used to reconstruct a route using the `importRoute` parameter.
 */
@Serializable
data class RouterRoute(
    val id: String,
    val sections: List<RouterSection>,
    val notices: List<Notice>? = null,
    val routeHandle: String? = null
)
