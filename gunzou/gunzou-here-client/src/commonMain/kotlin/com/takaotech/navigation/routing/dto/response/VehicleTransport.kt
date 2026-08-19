package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Transport mode of the vehicle.
 *
 * @property mode Transport mode of the vehicle.
 */
@Serializable
data class VehicleTransport(@SerialName("mode") val mode: com.takaotech.navigation.routing.model.TransportMode)
