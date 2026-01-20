package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * Response from the HERE Routing API /routes endpoint.
 *
 * @property routes List of calculated routes
 * @property notices List of notices/warnings for the entire response
 */
@Serializable
data class RouterRouteResponse(
    val routes: List<RouterRoute>,
    val notices: List<Notice>? = null
)
