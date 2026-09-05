package com.takaotech.gunzou.here.publictransit.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the Public Transit Routes API.
 */
@Serializable
data class TransitRouteResponse(
    @SerialName("routes") val routes: List<TransitRoute>,
    @SerialName("notices") val notices: List<Notice>? = null,
)
