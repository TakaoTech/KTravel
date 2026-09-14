package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A street name split into its parts, only with `show=streetInfo`: useful to abbreviate or sort
 * addresses by their base name rather than by the street type in front of it.
 *
 * @property baseName Base name of the street
 * @property streetType Street type, such as `Via` or `Street`
 * @property streetTypePrecedes Whether the street type comes before the base name
 * @property streetTypeAttached Whether the street type is written attached to the base name
 * @property prefix Directional identifier before the base name and not part of it, such as `N`
 * @property suffix Directional identifier after the base name and not part of it, such as `SW`
 * @property direction Official direction of a highway, such as `North/South`
 * @property language BCP 47 code of the language of the name
 */
@Serializable
data class StreetInfo(
    @SerialName("baseName") val baseName: String? = null,
    @SerialName("streetType") val streetType: String? = null,
    @SerialName("streetTypePrecedes") val streetTypePrecedes: Boolean? = null,
    @SerialName("streetTypeAttached") val streetTypeAttached: Boolean? = null,
    @SerialName("prefix") val prefix: String? = null,
    @SerialName("suffix") val suffix: String? = null,
    @SerialName("direction") val direction: String? = null,
    @SerialName("language") val language: String? = null,
)
