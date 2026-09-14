package com.takaotech.gunzou.here.search.autocomplete.dto.response

import com.takaotech.gunzou.here.search.common.dto.SearchAddress
import com.takaotech.gunzou.here.search.common.dto.StreetInfo
import com.takaotech.gunzou.here.search.common.dto.highlight.Highlights
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single Autocomplete result: an address, street, intersection, postal code, locality or
 * administrative area.
 *
 * A completion carries no position: resolve [id] through the Lookup endpoint to place it on a map.
 * The type fields are kept as text in HERE's own vocabulary, because HERE documents that new values
 * can appear without notice.
 *
 * The restricted `hasRelatedMPA` field is deliberately not read.
 *
 * @property title Display name, built so that [highlights] can mark the typed parts in it, such as
 *   `Germany, 32547, Bad Oeynhausen, Schulstraße 4`
 * @property id HERE identifier, also accepted by the Lookup endpoint
 * @property address Postal address
 * @property language BCP 47 code of the preferred language of the address components
 * @property politicalView Political view the result is rendered in, when the request set one
 * @property resultType `houseNumber`, `street`, `intersection`, `postalCodePoint`, `locality` or
 *   `administrativeArea`
 * @property houseNumberType `PA` for a point address, `interpolated` for a position interpolated
 *   along the street, when [resultType] is `houseNumber`
 * @property estimatedPointAddress Whether the coordinates of a point address are estimated. Only
 *   present, as `true`, for such results.
 * @property localityType `city`, `district`, `subdistrict` or `postalCode`, when [resultType] is
 *   `locality`
 * @property administrativeAreaType `country`, `state` or `county`, when [resultType] is
 *   `administrativeArea`
 * @property distance Straight-line distance from the search centre, in meters
 * @property highlights Parts of the title and of the address that matched the query
 * @property streetInfo Street name split into its parts, only with `show=streetInfo`
 */
@Serializable
data class AutocompleteResultItem(
    @SerialName("title") val title: String,
    @SerialName("id") val id: String,
    @SerialName("address") val address: SearchAddress,
    @SerialName("language") val language: String? = null,
    @SerialName("politicalView") val politicalView: String? = null,
    @SerialName("resultType") val resultType: String? = null,
    @SerialName("houseNumberType") val houseNumberType: String? = null,
    @SerialName("estimatedPointAddress") val estimatedPointAddress: Boolean? = null,
    @SerialName("localityType") val localityType: String? = null,
    @SerialName("administrativeAreaType") val administrativeAreaType: String? = null,
    @SerialName("distance") val distance: Long? = null,
    @SerialName("highlights") val highlights: Highlights? = null,
    @SerialName("streetInfo") val streetInfo: List<StreetInfo>? = null,
)
