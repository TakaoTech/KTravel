package com.takaotech.gunzou.here.routing.dto.response.road

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignpostLabel(
    @SerialName("name") val name: LocalizedString? = null,
    @SerialName("routeNumber") val routeNumber: LocalizedRouteNumber? = null,
)
