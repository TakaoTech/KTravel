package com.takaotech.gunzou.here.search.browser.dto.response

import com.takaotech.gunzou.here.common.dto.Location
import com.takaotech.gunzou.here.search.common.dto.AccessPoint
import com.takaotech.gunzou.here.search.common.dto.AccessRestriction
import com.takaotech.gunzou.here.search.common.dto.ContactInformation
import com.takaotech.gunzou.here.search.common.dto.MapView
import com.takaotech.gunzou.here.search.common.dto.OpeningHours
import com.takaotech.gunzou.here.search.common.dto.PhonemesSection
import com.takaotech.gunzou.here.search.common.dto.ResultCategory
import com.takaotech.gunzou.here.search.common.dto.SearchAddress
import com.takaotech.gunzou.here.search.common.dto.StreetInfo
import com.takaotech.gunzou.here.search.common.dto.SupplierReference
import com.takaotech.gunzou.here.search.common.dto.TimeZoneInfo
import com.takaotech.gunzou.here.search.common.dto.media.Media
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single Browse result: a place, or an address, street, locality or administrative area.
 *
 * Only [id], [title] and [address] are guaranteed; everything else depends on the data HERE holds
 * for the result and on the sections requested. The type fields are kept as text in HERE's own
 * vocabulary, because HERE documents that new values can appear without notice.
 *
 * The `extended` and `chains` sections of the response are deliberately not read.
 *
 * @property title Localized display name
 * @property id HERE identifier, also accepted by the Lookup endpoint
 * @property address Postal address
 * @property politicalView Political view the result is rendered in, when the request set one
 * @property resultType `place`, `street`, `locality`, `administrativeArea` or `addressBlock`
 * @property addressBlockType `block` or `subblock`, when [resultType] is `addressBlock`
 * @property localityType `city`, `district`, `subdistrict` or `postalCode`, when [resultType] is
 *   `locality`
 * @property administrativeAreaType `country`, `state` or `county`, when [resultType] is
 *   `administrativeArea`
 * @property position Where to put the pin on a map
 * @property access Positions on a navigable link from which the result is reached, primary first
 * @property distance Straight-line distance from the search centre, in meters
 * @property mapView Bounding box of the result. Places have none.
 * @property categories Place categories of the result
 * @property foodTypes Cuisines served
 * @property references Identifiers of the place in third-party systems
 * @property contacts Phone numbers, web sites, email addresses
 * @property openingHours Opening hours. Absent means unknown, not always open.
 * @property timeZone Time zone, only with `show=tz`
 * @property phonemes Pronunciations of the names, only with `show=phonemes`
 * @property streetInfo Street name split into its parts, only with `show=streetInfo`
 * @property accessRestrictions Known access restrictions. Absent means none are known.
 * @property media Tripadvisor images, editorials and ratings, only with `show=tripadvisor`
 */
@Serializable
data class BrowseResultItem(
    @SerialName("title") val title: String,
    @SerialName("id") val id: String,
    @SerialName("address") val address: SearchAddress,
    @SerialName("politicalView") val politicalView: String? = null,
    @SerialName("resultType") val resultType: String? = null,
    @SerialName("addressBlockType") val addressBlockType: String? = null,
    @SerialName("localityType") val localityType: String? = null,
    @SerialName("administrativeAreaType") val administrativeAreaType: String? = null,
    @SerialName("position") val position: Location? = null,
    @SerialName("access") val access: List<AccessPoint>? = null,
    @SerialName("distance") val distance: Long? = null,
    @SerialName("mapView") val mapView: MapView? = null,
    @SerialName("categories") val categories: List<ResultCategory>? = null,
    @SerialName("foodTypes") val foodTypes: List<ResultCategory>? = null,
    @SerialName("references") val references: List<SupplierReference>? = null,
    @SerialName("contacts") val contacts: List<ContactInformation>? = null,
    @SerialName("openingHours") val openingHours: List<OpeningHours>? = null,
    @SerialName("timeZone") val timeZone: TimeZoneInfo? = null,
    @SerialName("phonemes") val phonemes: PhonemesSection? = null,
    @SerialName("streetInfo") val streetInfo: List<StreetInfo>? = null,
    @SerialName("accessRestrictions") val accessRestrictions: List<AccessRestriction>? = null,
    @SerialName("media") val media: Media? = null,
)
