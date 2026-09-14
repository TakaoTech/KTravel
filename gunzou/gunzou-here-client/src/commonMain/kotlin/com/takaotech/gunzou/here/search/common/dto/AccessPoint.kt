package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A position on a navigable link from which a result can be reached, by car or on foot.
 *
 * A `place` result can have several; any other result has at most one.
 *
 * @property lat Latitude in decimal degrees
 * @property lng Longitude in decimal degrees
 * @property primary True on the main access of a place with more than one. Only the first access
 *   of the list can carry it.
 * @property type Kind of access in HERE's own vocabulary, for places only: `delivery`, `emergency`,
 *   `entrance`, `loading`, `other`, `parking` or `taxi`. Kept as text because HERE may add values.
 * @property label Short description of the access, such as `North Entrance`, for places only
 */
@Serializable
data class AccessPoint(
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double,
    @SerialName("primary") val primary: Boolean? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("label") val label: String? = null,
)
