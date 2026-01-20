package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * A section of a route traveled by vehicle.
 *
 * @property id Unique identifier of the section
 * @property type Section type (always "vehicle" for this type)
 * @property departure Departure information
 * @property arrival Arrival information
 * @property transport Transport mode information
 * @property polyline Encoded polyline of the route geometry (flexible polyline encoding)
 * @property summary Summary of key attributes (duration, length, etc.)
 * @property notices List of notices/warnings for this section
 */
@Serializable
data class VehicleSection(
    val id: String,
    val type: String,
    val departure: VehicleDeparture,
    val arrival: VehicleDeparture,
    val transport: VehicleTransport,
    val polyline: String? = null,
    val summary: VehicleSummary? = null,
    val notices: List<Notice>? = null
)
