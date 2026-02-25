package com.takaotech.navigation.routing.dto.response.toll

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TollSystem(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null
)
