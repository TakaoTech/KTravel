package com.takaotech.navigation.routing.dto.response.road

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalizedRouteNumber(
    @SerialName("value") val value: String,
    @SerialName("language") val language: String? = null,
    @SerialName("direction") val direction: String? = null,
    @SerialName("routeType") val routeType: Int? = null
)
