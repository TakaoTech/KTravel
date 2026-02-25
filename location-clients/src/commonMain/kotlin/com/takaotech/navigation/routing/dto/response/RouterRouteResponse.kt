package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * A response containing zero or more routes.
 *
 * @property routes An array of routes.
 * @property notices Contains a list of issues related to this request. Notices must be carefully evaluated and, if deemed necessary, the response should be discarded accordingly.
 */
@Serializable
data class RouterRouteResponse(
    val routes: List<RouterRoute>,
    val notices: List<Notice>? = null
)
