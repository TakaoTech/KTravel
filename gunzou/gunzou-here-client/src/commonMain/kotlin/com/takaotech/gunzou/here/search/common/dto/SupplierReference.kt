package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The identifier a third-party supplier knows a place by.
 *
 * @property supplier Who supplied the reference
 * @property id Identifier of the place in the supplier's system
 * @property categories Place categories the reference relates to, only with
 *   `show=referenceCategories`
 */
@Serializable
data class SupplierReference(
    @SerialName("supplier") val supplier: ReferenceSupplier,
    @SerialName("id") val id: String,
    @SerialName("categories") val categories: List<CategoryRef>? = null,
)

/**
 * A supplier of place references.
 *
 * @property id Supplier identifier in HERE's own vocabulary, such as `core`, `tripadvisor`, `yelp`
 *   or `booking.com`. Kept as text because HERE adds suppliers over time.
 */
@Serializable
data class ReferenceSupplier(@SerialName("id") val id: String)
