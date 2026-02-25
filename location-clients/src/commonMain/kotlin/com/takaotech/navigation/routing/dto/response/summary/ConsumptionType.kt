package com.takaotech.navigation.routing.dto.response.summary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ConsumptionType {
    @SerialName("electric")
    ELECTRIC,

    @SerialName("diesel")
    DIESEL,

    @SerialName("petrol")
    PETROL,

    @SerialName("lpg")
    LPG,

    @SerialName("cng")
    CNG,

    @SerialName("lng")
    LNG,

    @SerialName("ethanol")
    ETHANOL,

    @SerialName("propane")
    PROPANE,

    @SerialName("hydrogen")
    HYDROGEN
}
