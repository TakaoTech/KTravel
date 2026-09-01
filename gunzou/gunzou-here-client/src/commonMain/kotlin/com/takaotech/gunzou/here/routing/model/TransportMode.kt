package com.takaotech.gunzou.here.routing.model

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
    PRIVATE_BUS,
    ;

    /**
     * The value the API expects in a query string.
     *
     * Not `name.lowercase()`: that turns `PRIVATE_BUS` into `private_bus`, which the API rejects.
     * The mapping is the one the [SerialName] annotations already declare for the response body,
     * spelled out here because a query parameter is not built by the serializer.
     */
    fun toQueryString(): String = when (this) {
        PRIVATE_BUS -> "privateBus"
        else -> name.lowercase()
    }
}
