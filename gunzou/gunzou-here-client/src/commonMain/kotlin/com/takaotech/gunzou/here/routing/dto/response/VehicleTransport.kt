package com.takaotech.gunzou.here.routing.dto.response

import com.takaotech.gunzou.here.routing.model.TransportMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Transport mode of the vehicle.
 *
 * @property mode Transport mode of the vehicle.
 */
@Serializable
data class VehicleTransport(@SerialName("mode") val mode: TransportMode)
