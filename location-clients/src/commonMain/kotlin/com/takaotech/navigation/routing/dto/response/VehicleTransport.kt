package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * Information about the transport mode used in a vehicle section.
 *
 * @property mode The transport mode
 */
@Serializable
data class VehicleTransport(
    val mode: com.takaotech.navigation.routing.model.TransportMode
)
