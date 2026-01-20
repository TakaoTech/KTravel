package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * Total value of key attributes for a route section.
 *
 * @property duration Duration in seconds
 * @property length Length in meters
 * @property baseDuration Duration without traffic information (seconds)
 * @property typicalDuration Duration under typical traffic conditions (seconds)
 * @property consumption Estimated energy/fuel consumption
 */
@Serializable
data class VehicleSummary(
    val duration: Int,
    val length: Int,
    val baseDuration: Int? = null,
    val typicalDuration: Int? = null,
    val consumption: Double? = null
)
