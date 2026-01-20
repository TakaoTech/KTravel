package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * Describes a departure or arrival location and time.
 *
 * @property place The location of departure/arrival
 * @property time Expected time in RFC 3339 format (optional)
 * @property charge Vehicle battery charge in kWh (optional, for EVs)
 */
@Serializable
data class VehicleDeparture(
    val place: Place,
    val time: String? = null,
    val charge: Double? = null
)
