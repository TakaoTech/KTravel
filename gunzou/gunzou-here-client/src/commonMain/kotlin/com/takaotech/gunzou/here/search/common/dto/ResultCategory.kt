package com.takaotech.gunzou.here.search.common.dto

import com.takaotech.gunzou.here.search.common.category.PlaceCategories
import com.takaotech.gunzou.here.search.common.category.PlaceCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A category a search result is tagged with: a place category, or a cuisine in `foodTypes`.
 *
 * @property id Category id, such as `600-6600-0082`
 * @property name Name of the category in the language of the result
 * @property primary True on the primary category of the result. HERE omits the field otherwise.
 */
@Serializable
data class ResultCategory(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    @SerialName("primary") val primary: Boolean? = null,
) {
    /**
     * The place category behind [id], or null when it is a cuisine or a category newer than this
     * client. The raw id stays available either way.
     */
    fun toPlaceCategory(): PlaceCategory? = PlaceCategories.fromId(id)
}
