package com.takaotech.navigation.publictransit.dto.response

import com.takaotech.navigation.common.dto.Location
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a place (station, stop, or location) in a transit route.
 *
 * @property url Where the operator publishes the state of this station.
 * @property wheelchairAccessible Accessibility in HERE's own vocabulary: `yes`, `no`, `limited` or
 *   `unknown`. Kept as text, like [type] and the transport mode, because translating a provider's
 *   vocabulary is the navigator's job and not this client's.
 */
@Serializable
data class TransitPlace(
    @SerialName("name") val name: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("location") val location: Location? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("wheelchairAccessible") val wheelchairAccessible: String? = null,
)
