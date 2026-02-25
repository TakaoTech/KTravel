package com.takaotech.navigation.routing.dto.response.notice

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleRestrictionMaxWeight(
    @SerialName("value") val value: Int,
    @SerialName("type") val type: String? = null
)
