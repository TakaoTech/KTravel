package com.takaotech.navigation.routing.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mode of transport to be used for route calculation.
 * Maps to HERE API RouterMode enum.
 */
@Serializable
enum class TransportMode {
    @SerialName("car")
    CAR,

    @SerialName("truck")
    TRUCK,

    @SerialName("pedestrian")
    PEDESTRIAN,

    @SerialName("bicycle")
    BICYCLE,

    @SerialName("scooter")
    SCOOTER,

    @SerialName("taxi")
    TAXI,

    @SerialName("bus")
    BUS,

    @SerialName("privateBus")
    PRIVATE_BUS
}
