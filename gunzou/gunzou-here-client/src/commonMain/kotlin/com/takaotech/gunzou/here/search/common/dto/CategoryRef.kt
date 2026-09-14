package com.takaotech.gunzou.here.search.common.dto

import com.takaotech.gunzou.here.search.common.category.PlaceCategories
import com.takaotech.gunzou.here.search.common.category.PlaceCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A reference to a place category, used where a piece of information only applies to some of the
 * categories of a result: a contact, a set of opening hours, an access restriction.
 *
 * @property id Category id, such as `600-6600-0082`
 */
@Serializable
data class CategoryRef(@SerialName("id") val id: String) {
    /** The place category behind [id], or null when it is newer than this client. */
    fun toPlaceCategory(): PlaceCategory? = PlaceCategories.fromId(id)
}
