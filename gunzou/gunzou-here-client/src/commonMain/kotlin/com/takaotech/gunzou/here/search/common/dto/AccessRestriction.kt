package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A known restriction on who may enter a place: customers or visitors only, members only.
 *
 * A place with no known restriction has no such entry at all, which is why [restricted] is never
 * `false` in practice.
 *
 * @property categories Place categories the restriction applies to, such as a parking lot of the
 *   place
 * @property restricted True when access is restricted
 */
@Serializable
data class AccessRestriction(
    @SerialName("categories") val categories: List<CategoryRef>? = null,
    @SerialName("restricted") val restricted: Boolean? = null,
)
