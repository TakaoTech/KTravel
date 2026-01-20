package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.Serializable

/**
 * Summary of travel attributes for a section.
 */
@Serializable
data class TravelSummary(
    val duration: Int? = null,
    val length: Int? = null
)
