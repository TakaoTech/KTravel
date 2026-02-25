package com.takaotech.navigation.routing.dto.response.road

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RoadInfoType {
    @SerialName("rural")
    RURAL,

    @SerialName("urban")
    URBAN,

    @SerialName("highway")
    HIGHWAY
}
