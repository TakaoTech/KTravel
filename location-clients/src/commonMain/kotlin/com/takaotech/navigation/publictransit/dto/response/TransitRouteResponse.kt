package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.Serializable

/**
 * Response from the Public Transit Routes API.
 */
@Serializable
data class TransitRouteResponse(
    val routes: List<TransitRoute>,
    val notices: List<Notice>? = null
)
