package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Summary of travel attributes for a section.
 */
@Serializable
data class TravelSummary(
    @SerialName("duration") val duration: Int? = null,
    @SerialName("length") val length: Int? = null,
)
