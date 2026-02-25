package com.takaotech.navigation.routing.dto.response.road

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalizedString(
    @SerialName("value") val value: String,
    @SerialName("language") val language: String? = null
)
