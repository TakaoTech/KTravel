package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.Serializable

/**
 * Transport information for a transit section.
 */
@Serializable
data class TransitTransport(
    val mode: String? = null,
    val name: String? = null,
    val category: String? = null,
    val color: String? = null,
    val textColor: String? = null,
    val headsign: String? = null,
    val shortName: String? = null,
    val longName: String? = null
)
