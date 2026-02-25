package com.takaotech.navigation.routing.dto.response.action

import com.takaotech.navigation.routing.dto.response.Location
import com.takaotech.navigation.routing.dto.response.Place
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Describes a location and time the section is passing through.
 *
 * @property id Unique identifier.
 * @property place Place details.
 * @property time Expected time in RFC 3339 format.
 * @property matchedLocation Matched location coordinates.
 */
@Serializable
data class Passthrough(
    @SerialName("id") val id: String? = null,
    @SerialName("place") val place: Place? = null,
    @SerialName("time") val time: String? = null,
    @SerialName("matchedLocation") val matchedLocation: Location? = null
)
