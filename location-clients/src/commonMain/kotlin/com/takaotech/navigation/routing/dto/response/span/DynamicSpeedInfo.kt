package com.takaotech.navigation.routing.dto.response.span

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DynamicSpeedInfo(
    @SerialName("trafficSpeed") val trafficSpeed: Double,
    @SerialName("baseSpeed") val baseSpeed: Double,
    @SerialName("turnTime") val turnTime: Int
)
