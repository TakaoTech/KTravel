package com.takaotech.gunzou.here.search.common.dto.media

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rich media about a place, aggregated from third parties: currently Tripadvisor only.
 *
 * Restricted to HERE customers with a specific contract. HERE omits it when the place has none of
 * the three.
 *
 * @property images Images of the place, with `show=tripadvisor` or `show=tripadvisorImageVariants`
 * @property editorials Editorial descriptions of the place, with `show=tripadvisor`
 * @property ratings User ratings of the place, with `show=tripadvisor`
 */
@Serializable
data class Media(
    @SerialName("images") val images: MediaCollection<MediaImage>? = null,
    @SerialName("editorials") val editorials: MediaCollection<Editorial>? = null,
    @SerialName("ratings") val ratings: MediaCollection<Rating>? = null,
)

/**
 * The wrapper HERE puts around every list of media. HERE omits it when the list would be empty.
 *
 * @param T Kind of media listed
 * @property items The media
 */
@Serializable
data class MediaCollection<T>(@SerialName("items") val items: List<T> = emptyList())

/**
 * The third party a piece of media comes from.
 *
 * @property id Supplier identifier, currently always `tripadvisor`. Kept as text because HERE may
 *   add suppliers.
 */
@Serializable
data class MediaSupplier(@SerialName("id") val id: String)
