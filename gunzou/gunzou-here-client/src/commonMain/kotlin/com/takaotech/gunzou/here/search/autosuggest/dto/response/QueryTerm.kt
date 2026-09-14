package com.takaotech.gunzou.here.search.autosuggest.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A suggested completion of one term of the query, such as `restaurant` for `restau`.
 *
 * The indexes count Unicode code points, not UTF-16 chars: they match `String` indexes only while
 * the query has no character outside the Basic Multilingual Plane, such as an emoji.
 *
 * @property term Term to offer the user
 * @property replaces Part of the query the term replaces
 * @property start Index of the first replaced code point, inclusive
 * @property end Index one past the last replaced code point, exclusive
 */
@Serializable
data class QueryTerm(
    @SerialName("term") val term: String,
    @SerialName("replaces") val replaces: String,
    @SerialName("start") val start: Int,
    @SerialName("end") val end: Int,
) {
    /** The replaced indexes of the query as a range. */
    fun toIntRange(): IntRange = start until end
}
