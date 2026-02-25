package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * Total value of key attributes for a route section.
 *
 * @property duration Estimated duration (in seconds).
 * @property length Estimated length (in meters).
 * @property baseDuration Estimated duration (in seconds) without traffic information.
 * @property typicalDuration Estimated duration (in seconds) under typical traffic conditions.
 * @property consumption Estimated energy or fuel consumption.
 */
@Serializable
data class VehicleSummary(
    val duration: Int,
    val length: Int,
    val baseDuration: Int? = null,
    val typicalDuration: Int? = null,
    val consumption: Double? = null
)
