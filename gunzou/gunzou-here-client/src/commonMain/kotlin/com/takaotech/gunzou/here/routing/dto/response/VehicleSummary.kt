package com.takaotech.gunzou.here.routing.dto.response

import kotlinx.serialization.SerialName
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
    @SerialName("duration") val duration: Int,
    @SerialName("length") val length: Int,
    @SerialName("baseDuration") val baseDuration: Int? = null,
    @SerialName("typicalDuration") val typicalDuration: Int? = null,
    @SerialName("consumption") val consumption: Double? = null,
)
