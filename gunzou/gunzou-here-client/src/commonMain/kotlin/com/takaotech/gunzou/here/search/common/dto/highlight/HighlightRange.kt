package com.takaotech.gunzou.here.search.common.dto.highlight

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A span of a response field that matched the text the user typed, to be highlighted.
 *
 * @property start Index of the first matched character, 0-based and inclusive
 * @property end Index one past the last matched character, 0-based and exclusive
 */
@Serializable
data class HighlightRange(@SerialName("start") val start: Int, @SerialName("end") val end: Int) {
    /** The matched indexes as a range, ready for `String.substring` or a styled text span. */
    fun toIntRange(): IntRange = start until end
}
