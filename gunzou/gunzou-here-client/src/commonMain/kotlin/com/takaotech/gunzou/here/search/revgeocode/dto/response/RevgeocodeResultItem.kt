package com.takaotech.gunzou.here.search.revgeocode.dto.response

import com.takaotech.gunzou.here.common.dto.Location
import com.takaotech.gunzou.here.search.common.dto.AccessPoint
import com.takaotech.gunzou.here.search.common.dto.CountryInfo
import com.takaotech.gunzou.here.search.common.dto.MapView
import com.takaotech.gunzou.here.search.common.dto.ResultCategory
import com.takaotech.gunzou.here.search.common.dto.SearchAddress
import com.takaotech.gunzou.here.search.common.dto.StreetInfo
import com.takaotech.gunzou.here.search.common.dto.TimeZoneInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single Reverse Geocode result: an address, street, place, locality or administrative area.
 *
 * Only [id], [title] and [address] are guaranteed; everything else depends on the data HERE holds
 * for the result and on the sections requested. The type fields are kept as text in HERE's own
 * vocabulary, because HERE documents that new values can appear without notice.
 *
 * The `mapReferences`, `related`, `navigationAttributes`, `postalCodeDetails` and `addressUsage`
 * sections of the response are deliberately not read.
 *
 * @property title Localized display name
 * @property id HERE identifier, also accepted by the Lookup endpoint
 * @property address Postal address
 * @property politicalView Political view the result is rendered in, when the request set one
 * @property resultType `houseNumber`, `street`, `place`, `locality`, `administrativeArea`,
 *   `postalCodePoint` or `addressBlock`
 * @property houseNumberType `PA`, `MPA` or `interpolated`, when [resultType] is `houseNumber`: how
 *   precise the address and its coordinates are
 * @property addressBlockType `block` or `subblock`, when [resultType] is `addressBlock`
 * @property localityType `city`, `district`, `subdistrict` or `postalCode`, when [resultType] is
 *   `locality`
 * @property administrativeAreaType `country`, `state` or `county`, when [resultType] is
 *   `administrativeArea`
 * @property position Where to put the pin on a map
 * @property access Positions on a navigable link from which the result is reached
 * @property distance Straight-line distance from the requested point, in meters
 * @property mapView Bounding box of the result. Places have none.
 * @property categories Place categories of the result
 * @property foodTypes Cuisines served
 * @property houseNumberFallback True when the house number was corrected to the nearest known one.
 *   HERE omits the field otherwise.
 * @property estimatedPointAddress True when the coordinates of a point address are estimated. HERE
 *   omits the field otherwise.
 * @property estimatedAreaFallback True when the area names are estimated from the closest street,
 *   only with [com.takaotech.gunzou.here.search.revgeocode.model.RevgeocodeFeature.ESTIMATED_AREA_FALLBACK]
 * @property timeZone Time zone, only with `show=tz`
 * @property streetInfo Street name split into its parts, only with `show=streetInfo`
 * @property countryInfo Country codes, only with `show=countryInfo`
 */
@Serializable
data class RevgeocodeResultItem(
    @SerialName("title") val title: String,
    @SerialName("id") val id: String,
    @SerialName("address") val address: SearchAddress,
    @SerialName("politicalView") val politicalView: String? = null,
    @SerialName("resultType") val resultType: String? = null,
    @SerialName("houseNumberType") val houseNumberType: String? = null,
    @SerialName("addressBlockType") val addressBlockType: String? = null,
    @SerialName("localityType") val localityType: String? = null,
    @SerialName("administrativeAreaType") val administrativeAreaType: String? = null,
    @SerialName("position") val position: Location? = null,
    @SerialName("access") val access: List<AccessPoint>? = null,
    @SerialName("distance") val distance: Long? = null,
    @SerialName("mapView") val mapView: MapView? = null,
    @SerialName("categories") val categories: List<ResultCategory>? = null,
    @SerialName("foodTypes") val foodTypes: List<ResultCategory>? = null,
    @SerialName("houseNumberFallback") val houseNumberFallback: Boolean? = null,
    @SerialName("estimatedPointAddress") val estimatedPointAddress: Boolean? = null,
    @SerialName("estimatedAreaFallback") val estimatedAreaFallback: Boolean? = null,
    @SerialName("timeZone") val timeZone: TimeZoneInfo? = null,
    @SerialName("streetInfo") val streetInfo: List<StreetInfo>? = null,
    @SerialName("countryInfo") val countryInfo: CountryInfo? = null,
)
