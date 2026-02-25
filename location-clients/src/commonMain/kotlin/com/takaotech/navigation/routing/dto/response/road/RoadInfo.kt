package com.takaotech.navigation.routing.dto.response.road

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoadInfo(
    @SerialName("type") val type: RoadInfoType? = null,
    @SerialName("name") val name: List<LocalizedString>? = null,
    @SerialName("number") val number: List<LocalizedRouteNumber>? = null,
    @SerialName("toward") val toward: List<LocalizedString>? = null
)
