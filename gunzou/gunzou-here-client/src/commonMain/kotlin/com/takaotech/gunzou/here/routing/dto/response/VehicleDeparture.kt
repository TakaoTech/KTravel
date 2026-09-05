package com.takaotech.gunzou.here.routing.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Describes a departure or arrival location and time.
 *
 * @property place The location of departure or arrival.
 * @property time Expected time of departure or arrival in RFC 3339 format.
 * @property charge Estimated vehicle battery charge in kWh.
 */
@Serializable
data class VehicleDeparture(
    @SerialName("place") val place: Place,
    @SerialName("time") val time: String? = null,
    @SerialName("charge") val charge: Double? = null,
)
