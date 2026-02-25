package com.takaotech.navigation.routing.dto.response.road

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignpostInfo(
    @SerialName("labels") val labels: List<SignpostLabel>
)
