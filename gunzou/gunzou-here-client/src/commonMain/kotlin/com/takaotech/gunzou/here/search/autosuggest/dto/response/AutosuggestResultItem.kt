package com.takaotech.gunzou.here.search.autosuggest.dto.response

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
import com.takaotech.gunzou.here.search.common.dto.highlight.Highlights
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

/**
 * A single Autosuggest suggestion: an [Entity] the user can pick, or a [Query] to run as a new
 * search.
 *
 * The type fields are kept as text in HERE's own vocabulary, because HERE documents that new values
 * can appear without notice.
 */
@Serializable(with = AutosuggestResultItemSerializer::class)
sealed interface AutosuggestResultItem {
    /** Localized display name. */
    val title: String

    /** HERE identifier of the suggestion. */
    val id: String

    /** Kind of suggestion, such as `place`, `street` or `categoryQuery`. */
    val resultType: String?

    /** Which parts of the suggestion matched the query. */
    val highlights: Highlights?

    /**
     * A place, or an address, street, locality or administrative area.
     *
     * Only [id], [title] and [address] are guaranteed; everything else depends on the data HERE
     * holds for the result and on the sections requested.
     *
     * The `chains`, `extended` and `mapReferences` sections of the response are deliberately not
     * read, nor is `excursionDistance`, which only a search along a route returns.
     *
     * @property title Localized display name
     * @property id HERE identifier, also accepted by the Lookup endpoint
     * @property address Postal address
     * @property politicalView Political view the result is rendered in, when the request set one
     * @property ontologyId Id of the related entry in the HERE ontology
     * @property resultType `place`, `houseNumber`, `street`, `intersection`, `postalCodePoint`,
     *   `locality`, `administrativeArea` or `addressBlock`
     * @property houseNumberType `PA` for a point address or `interpolated`, when [resultType] is
     *   `houseNumber`
     * @property addressBlockType `block` or `subblock`, when [resultType] is `addressBlock`
     * @property localityType `city`, `district`, `subdistrict` or `postalCode`, when [resultType]
     *   is `locality`
     * @property administrativeAreaType `country`, `state` or `county`, when [resultType] is
     *   `administrativeArea`
     * @property position Where to put the pin on a map
     * @property access Positions on a navigable link from which the result is reached, primary
     *   first
     * @property driveThrough True when the business serves customers without leaving their car
     * @property distance Straight-line distance from the search centre, in meters
     * @property mapView Bounding box of the result. Places have none.
     * @property categories Place categories of the result
     * @property foodTypes Cuisines served
     * @property references Identifiers of the place in third-party systems
     * @property contacts Phone numbers, web sites, email addresses, only with `show=details`
     * @property openingHours Opening hours, only with `show=details`. Absent means unknown, not
     *   always open.
     * @property timeZone Time zone, only with `show=tz`
     * @property highlights Which parts of the title and address matched the query
     * @property phonemes Pronunciations of the names, only with `show=phonemes`
     * @property streetInfo Street name split into its parts, only with `show=streetInfo`
     * @property accessRestrictions Known access restrictions. Absent means none are known.
     */
    @Serializable
    data class Entity(
        @SerialName("title") override val title: String,
        @SerialName("id") override val id: String,
        @SerialName("address") val address: SearchAddress,
        @SerialName("politicalView") val politicalView: String? = null,
        @SerialName("ontologyId") val ontologyId: String? = null,
        @SerialName("resultType") override val resultType: String? = null,
        @SerialName("houseNumberType") val houseNumberType: String? = null,
        @SerialName("addressBlockType") val addressBlockType: String? = null,
        @SerialName("localityType") val localityType: String? = null,
        @SerialName("administrativeAreaType") val administrativeAreaType: String? = null,
        @SerialName("position") val position: Location? = null,
        @SerialName("access") val access: List<AccessPoint>? = null,
        @SerialName("driveThrough") val driveThrough: Boolean? = null,
        @SerialName("distance") val distance: Long? = null,
        @SerialName("mapView") val mapView: MapView? = null,
        @SerialName("categories") val categories: List<ResultCategory>? = null,
        @SerialName("foodTypes") val foodTypes: List<ResultCategory>? = null,
        @SerialName("references") val references: List<SupplierReference>? = null,
        @SerialName("contacts") val contacts: List<ContactInformation>? = null,
        @SerialName("openingHours") val openingHours: List<OpeningHours>? = null,
        @SerialName("timeZone") val timeZone: TimeZoneInfo? = null,
        @SerialName("highlights") override val highlights: Highlights? = null,
        @SerialName("phonemes") val phonemes: PhonemesSection? = null,
        @SerialName("streetInfo") val streetInfo: List<StreetInfo>? = null,
        @SerialName("accessRestrictions") val accessRestrictions: List<AccessRestriction>? = null,
    ) : AutosuggestResultItem

    /**
     * A category or chain query to offer the user, such as "Restaurants near Berlin".
     *
     * Its [id] cannot be passed to the Lookup endpoint: the suggestion is followed by calling
     * [href].
     *
     * @property title Localized display name
     * @property id HERE identifier of the suggestion, not accepted by the Lookup endpoint
     * @property resultType `categoryQuery` or `chainQuery`
     * @property href URL of the follow-up search
     * @property noResultOnFollowUp True when [href] is unlikely to return any result, for example
     *   because no place of the chain is near the search centre. HERE omits the field otherwise.
     * @property highlights Which parts of the title matched the query. The address part is always
     *   absent.
     */
    @Serializable
    data class Query(
        @SerialName("title") override val title: String,
        @SerialName("id") override val id: String,
        @SerialName("resultType") override val resultType: String? = null,
        @SerialName("href") val href: String? = null,
        @SerialName("noResultOnFollowUp") val noResultOnFollowUp: Boolean? = null,
        @SerialName("highlights") override val highlights: Highlights? = null,
    ) : AutosuggestResultItem
}

/**
 * Tells the two kinds of suggestion apart, which HERE does not label with a discriminator: only an
 * [AutosuggestResultItem.Entity] carries an address. Checking the address rather than the result
 * type keeps a result type HERE adds later readable.
 */
internal object AutosuggestResultItemSerializer :
    JsonContentPolymorphicSerializer<AutosuggestResultItem>(AutosuggestResultItem::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AutosuggestResultItem> =
        if (ADDRESS_KEY in element.jsonObject) {
            AutosuggestResultItem.Entity.serializer()
        } else {
            AutosuggestResultItem.Query.serializer()
        }

    private const val ADDRESS_KEY = "address"
}
