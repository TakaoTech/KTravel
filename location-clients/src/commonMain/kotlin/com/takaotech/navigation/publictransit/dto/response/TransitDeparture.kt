package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.Serializable

/**
 * Represents a departure or arrival point in a transit section.
 */
@Serializable
data class TransitDeparture(
    val time: String? = null,
    val place: TransitPlace? = null,
    val delay: Int? = null,
    val platform: String? = null
)
