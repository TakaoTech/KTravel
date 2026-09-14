package com.takaotech.gunzou.here.search.common.dto.media

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The user rating of a place in a supplier's database.
 *
 * @property count Number of user reviews
 * @property average Average user rating
 * @property href Link to the ratings at the source
 * @property supplier Who supplied the rating
 */
@Serializable
data class Rating(
    @SerialName("count") val count: Int,
    @SerialName("average") val average: Double,
    @SerialName("href") val href: String? = null,
    @SerialName("supplier") val supplier: MediaSupplier,
)
