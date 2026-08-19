package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The operator running the service of a transit section.
 *
 * Reported per section rather than per line, because the same line can be run by different
 * agencies on different journeys.
 *
 * @property id The operator's identifier in HERE's namespace.
 * @property name Display name, and the only field a traveller ever sees.
 * @property website Where a disruption on this service is announced.
 */
@Serializable
data class TransitAgency(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("website") val website: String? = null,
)
