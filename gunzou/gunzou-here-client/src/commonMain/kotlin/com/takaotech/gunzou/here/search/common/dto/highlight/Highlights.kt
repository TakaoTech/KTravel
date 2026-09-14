package com.takaotech.gunzou.here.search.common.dto.highlight

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Which parts of a result matched the query, for the endpoints that complete what the user types.
 *
 * @property title Matched spans of the result title
 * @property address Matched spans of each address component
 */
@Serializable
data class Highlights(
    @SerialName("title") val title: List<HighlightRange>? = null,
    @SerialName("address") val address: AddressHighlights? = null,
)

/**
 * Matched spans of each component of a [com.takaotech.gunzou.here.search.common.dto.SearchAddress].
 *
 * A component is absent when nothing in it matched.
 *
 * @property label Matched spans of the address label
 * @property country Matched spans of the country name
 * @property countryCode Matched spans of the country code
 * @property state Matched spans of the state
 * @property stateCode Matched spans of the state code
 * @property county Matched spans of the county
 * @property countyCode Matched spans of the county code
 * @property city Matched spans of the city
 * @property district Matched spans of the district
 * @property subdistrict Matched spans of the subdistrict
 * @property block Matched spans of the block
 * @property subblock Matched spans of the sub-block
 * @property street Matched spans of the street
 * @property streets Matched spans of each crossing street of an intersection, in the order of
 *   `address.streets`
 * @property postalCode Matched spans of the postal code
 * @property houseNumber Matched spans of the house number
 * @property building Matched spans of the building
 * @property unit Matched spans of the unit
 */
@Serializable
data class AddressHighlights(
    @SerialName("label") val label: List<HighlightRange>? = null,
    @SerialName("country") val country: List<HighlightRange>? = null,
    @SerialName("countryCode") val countryCode: List<HighlightRange>? = null,
    @SerialName("state") val state: List<HighlightRange>? = null,
    @SerialName("stateCode") val stateCode: List<HighlightRange>? = null,
    @SerialName("county") val county: List<HighlightRange>? = null,
    @SerialName("countyCode") val countyCode: List<HighlightRange>? = null,
    @SerialName("city") val city: List<HighlightRange>? = null,
    @SerialName("district") val district: List<HighlightRange>? = null,
    @SerialName("subdistrict") val subdistrict: List<HighlightRange>? = null,
    @SerialName("block") val block: List<HighlightRange>? = null,
    @SerialName("subblock") val subblock: List<HighlightRange>? = null,
    @SerialName("street") val street: List<HighlightRange>? = null,
    @SerialName("streets") val streets: List<List<HighlightRange>>? = null,
    @SerialName("postalCode") val postalCode: List<HighlightRange>? = null,
    @SerialName("houseNumber") val houseNumber: List<HighlightRange>? = null,
    @SerialName("building") val building: List<HighlightRange>? = null,
    @SerialName("unit") val unit: List<HighlightRange>? = null,
)
