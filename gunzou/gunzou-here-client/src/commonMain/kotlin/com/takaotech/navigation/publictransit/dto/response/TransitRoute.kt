package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A transit route containing multiple sections.
 */
@Serializable
data class TransitRoute(
    @SerialName("id") val id: String,
    @SerialName("sections") val sections: List<TransitRouteSection>,
    @SerialName("notices") val notices: List<Notice>? = null,
)
