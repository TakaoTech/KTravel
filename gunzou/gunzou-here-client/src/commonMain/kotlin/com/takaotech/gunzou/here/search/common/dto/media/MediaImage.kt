package com.takaotech.gunzou.here.search.common.dto.media

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An image of a place.
 *
 * @property href URL of the default image, usually sized for Tripadvisor's medium format. It can
 *   occasionally be larger, so bound its size when drawing it.
 * @property supplier Who supplied the image
 * @property variants The same image in other sizes, only with `show=tripadvisorImageVariants`
 */
@Serializable
data class MediaImage(
    @SerialName("href") val href: String,
    @SerialName("supplier") val supplier: MediaSupplier,
    @SerialName("variants") val variants: ImageVariants? = null,
)

/**
 * The sizes an image is available in.
 *
 * @property thumbnail 50×50 px, cropped
 * @property small 150×150 px, cropped
 * @property medium At most 250 px on the longer side, aspect ratio kept
 * @property large At most 550 px on the longer side, aspect ratio kept
 * @property original Original resolution and aspect ratio
 */
@Serializable
data class ImageVariants(
    @SerialName("thumbnail") val thumbnail: ImageVariant? = null,
    @SerialName("small") val small: ImageVariant? = null,
    @SerialName("medium") val medium: ImageVariant? = null,
    @SerialName("large") val large: ImageVariant? = null,
    @SerialName("original") val original: ImageVariant? = null,
)

/**
 * One size of an image.
 *
 * @property href URL of the image in this size
 */
@Serializable
data class ImageVariant(@SerialName("href") val href: String)
