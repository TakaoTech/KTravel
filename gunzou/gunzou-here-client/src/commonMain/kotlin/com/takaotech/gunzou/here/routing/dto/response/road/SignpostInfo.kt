package com.takaotech.gunzou.here.routing.dto.response.road

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignpostInfo(@SerialName("labels") val labels: List<SignpostLabel>)
