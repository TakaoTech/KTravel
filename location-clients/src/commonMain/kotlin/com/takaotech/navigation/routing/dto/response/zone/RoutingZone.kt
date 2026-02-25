package com.takaotech.navigation.routing.dto.response.zone

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoutingZone(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    @SerialName("type") val type: String? = null
)
