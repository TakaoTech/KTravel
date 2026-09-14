package com.takaotech.gunzou.api.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The extent a result covers on the map, in WGS84 degrees: what to fit the camera to when a pin is
 * not enough, such as a city or a long street.
 *
 * @property west Longitude of the western side.
 * @property south Latitude of the southern side.
 * @property east Longitude of the eastern side.
 * @property north Latitude of the northern side.
 */
@Serializable
data class GeoBounds(
    @SerialName("west") val west: Double,
    @SerialName("south") val south: Double,
    @SerialName("east") val east: Double,
    @SerialName("north") val north: Double,
)

/**
 * Which parts of a result matched what the user typed, for the provider that reports it.
 *
 * Absent altogether when the provider does not highlight, see
 * [SearchProfileDescriptor.supportsHighlights]; an empty list means nothing in that text matched.
 *
 * @property title Matched spans of the result title.
 * @property addressLabel Matched spans of [SearchAddress.label].
 */
@Serializable
data class SearchHighlights(
    @SerialName("title") val title: List<TextRange> = emptyList(),
    @SerialName("addressLabel") val addressLabel: List<TextRange> = emptyList(),
)

/**
 * A span of a text, to be highlighted.
 *
 * The indexes are the provider's own, which count characters of the text they refer to. A client
 * should clamp them to its length rather than trust them blindly.
 *
 * @property start Index of the first matched character, 0-based and inclusive.
 * @property end Index one past the last matched character, 0-based and exclusive.
 */
@Serializable
data class TextRange(@SerialName("start") val start: Int, @SerialName("end") val end: Int)
