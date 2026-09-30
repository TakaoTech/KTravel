package com.takaotech.gunzou.api.search.place

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.search.GeoBounds
import com.takaotech.gunzou.api.search.PlaceCategoryGroup
import com.takaotech.gunzou.api.search.SearchAddress
import com.takaotech.gunzou.api.search.SearchResultType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Body answered by every `GET /v1/{provider}/search/id/{id}`: what is known about one place,
 * whatever the provider behind it.
 *
 * The first half is what an autocomplete place already carries, so a client can show the details
 * of a suggestion without reconciling two shapes; the second half is what only a lookup by
 * identifier answers. Every field of that second half is optional: most addresses have no contacts
 * and no opening hours, and an empty list means the provider knows none, not that there are none.
 *
 * @property id Provider identifier of the place, the one it was asked for.
 * @property title Display name, in the language of the request.
 * @property resultType What it is: a place, a house number, a street, a locality...
 * @property position Where to put the pin.
 * @property address Postal address.
 * @property category The broad kind of place, when [resultType] is [SearchResultType.PLACE] and the
 *   provider categorizes it.
 * @property bounds The extent it covers, for results larger than a point. Places have none.
 * @property contacts Ways to reach the place, in the order the provider lists them.
 * @property openingHours When the place is open, one entry per recurring period. Empty when the
 *   provider does not know; a day that appears in no period is a day the place is closed.
 * @property timeZone The time zone the place lies in, which [openingHours] are expressed in.
 * @property permanentClosure Whether the place has closed for good, absent when it is not known to
 *   have.
 */
@Serializable
data class PlaceDetails(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("resultType") val resultType: SearchResultType,
    @SerialName("position") val position: GeoPoint,
    @SerialName("address") val address: SearchAddress = SearchAddress(),
    @SerialName("category") val category: PlaceCategoryGroup? = null,
    @SerialName("bounds") val bounds: GeoBounds? = null,
    @SerialName("contacts") val contacts: List<PlaceContact> = emptyList(),
    @SerialName("openingHours") val openingHours: List<OpeningPeriod> = emptyList(),
    @SerialName("timeZone") val timeZone: PlaceTimeZone? = null,
    @SerialName("permanentClosure") val permanentClosure: PermanentClosure? = null,
)

/**
 * The time zone a place lies in.
 *
 * @property name IANA time zone identifier, such as `Europe/Rome`.
 * @property utcOffset Offset from UTC at the time of the request, such as `+02:00`. It changes with
 *   daylight saving time, so it is only a hint: compute offsets for other dates from [name].
 */
@Serializable
data class PlaceTimeZone(@SerialName("name") val name: String, @SerialName("utcOffset") val utcOffset: String)

/**
 * How certain the provider is that a place has closed for good.
 *
 * A string rather than an enum, so a degree of certainty added by a newer server still decodes.
 *
 * @property value The certainty as it travels on the wire.
 */
@Serializable
@JvmInline
value class PermanentClosure(val value: String) {
    override fun toString(): String = value

    /** The degrees of certainty this contract knows. */
    companion object {
        /** The place is known to have closed. */
        val CONFIRMED = PermanentClosure("confirmed")

        /** The place is suspected to have closed, without a confirmation. */
        val POSSIBLE = PermanentClosure("possible")
    }
}
