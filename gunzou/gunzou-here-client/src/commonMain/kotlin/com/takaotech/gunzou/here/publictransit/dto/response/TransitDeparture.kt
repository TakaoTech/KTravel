package com.takaotech.gunzou.here.publictransit.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a departure or arrival point in a transit section.
 */
@Serializable
data class TransitDeparture(
    @SerialName("time") val time: String? = null,
    @SerialName("place") val place: TransitPlace? = null,
    @SerialName("delay") val delay: Int? = null,
    @SerialName("platform") val platform: String? = null,
)
