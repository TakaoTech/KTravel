package com.takaotech.navigation.routing.dto.response.summary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleTravelSummary(
    @SerialName("duration") val duration: Int,
    @SerialName("length") val length: Int,
    @SerialName("baseDuration") val baseDuration: Int? = null,
    @SerialName("consumption") val consumption: Double? = null
)
