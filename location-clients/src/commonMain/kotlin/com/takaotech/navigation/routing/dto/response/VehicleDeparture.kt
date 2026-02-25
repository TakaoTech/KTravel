package com.takaotech.navigation.routing.dto.response

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
    val place: Place,
    val time: String? = null,
    val charge: Double? = null
)
