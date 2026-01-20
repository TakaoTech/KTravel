package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.Serializable

/**
 * A transit route containing multiple sections.
 */
@Serializable
data class TransitRoute(
    val id: String,
    val sections: List<TransitRouteSection>,
    val notices: List<Notice>? = null
)
