package com.takaotech.gunzou.here.search.common.dto.media

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An editorial description of a place.
 *
 * @property description The editorial text
 * @property language Language of the text, when known
 * @property href Link to the source of the editorial
 * @property supplier Who supplied the editorial
 */
@Serializable
data class Editorial(
    @SerialName("description") val description: String,
    @SerialName("language") val language: String? = null,
    @SerialName("href") val href: String? = null,
    @SerialName("supplier") val supplier: MediaSupplier,
)
