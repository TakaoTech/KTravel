package com.takaotech.gunzou.api.search

import com.takaotech.gunzou.api.common.GeoPoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * An area a search is restricted to. It is a hard filter: results outside it are not returned.
 *
 * Discriminated on `type`. Deliberately unvalidated, like [GeoPoint]: a radius that is not positive
 * or a box whose south is north of its north must reach the caller as an
 * [com.takaotech.gunzou.api.error.ErrorResponse] naming the field, which is what the server's request
 * validation produces, rather than as a decoding failure.
 *
 * Not every provider accepts every shape — see [SearchProfileDescriptor.supportedAreas] — and a
 * shape the profile does not list is refused with
 * [com.takaotech.gunzou.api.error.ErrorCode.UNSUPPORTED_OPTION].
 */
@Serializable
sealed interface SearchArea {

    /** Which of the shapes this is, as a descriptor lists it. */
    val kind: SearchAreaKind

    /**
     * One or more countries.
     *
     * @property codes ISO 3166-1 alpha-2 codes, in uppercase, such as `IT`. Alpha-2 because it is
     *   the code most providers and every locale already speak; a provider that wants alpha-3 is
     *   converted to on the server.
     */
    @Serializable
    @SerialName("countries")
    data class Countries(@SerialName("codes") val codes: List<String>) : SearchArea {
        override val kind: SearchAreaKind get() = SearchAreaKind.COUNTRIES
    }

    /**
     * A circle around a point.
     *
     * @property center Centre of the circle.
     * @property radiusMeters Radius of the circle, in meters, expected to be positive.
     */
    @Serializable
    @SerialName("circle")
    data class Circle(@SerialName("center") val center: GeoPoint, @SerialName("radiusMeters") val radiusMeters: Int) :
        SearchArea {
        override val kind: SearchAreaKind get() = SearchAreaKind.CIRCLE
    }

    /**
     * A box bounded by two meridians and two parallels, in WGS84 degrees.
     *
     * @property west Longitude of the western side.
     * @property south Latitude of the southern side, expected not to be north of [north].
     * @property east Longitude of the eastern side.
     * @property north Latitude of the northern side.
     */
    @Serializable
    @SerialName("boundingBox")
    data class BoundingBox(
        @SerialName("west") val west: Double,
        @SerialName("south") val south: Double,
        @SerialName("east") val east: Double,
        @SerialName("north") val north: Double,
    ) : SearchArea {
        override val kind: SearchAreaKind get() = SearchAreaKind.BOUNDING_BOX
    }
}

/**
 * Names a shape of [SearchArea], so a descriptor can say which ones a profile accepts.
 *
 * A string rather than an enum because it travels in a catalog response: a server that learns a new
 * shape must not make the whole catalog undecodable for an older client, which would only skip it.
 *
 * @property value The shape as it travels on the wire, the same spelling as the `type` of the area.
 */
@Serializable
@JvmInline
value class SearchAreaKind(val value: String) {
    override fun toString(): String = value

    /** The shapes this contract knows. */
    companion object {
        /** [SearchArea.Countries]. */
        val COUNTRIES = SearchAreaKind("countries")

        /** [SearchArea.Circle]. */
        val CIRCLE = SearchAreaKind("circle")

        /** [SearchArea.BoundingBox]. */
        val BOUNDING_BOX = SearchAreaKind("boundingBox")
    }
}
