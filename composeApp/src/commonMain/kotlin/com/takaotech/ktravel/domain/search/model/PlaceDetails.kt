package com.takaotech.ktravel.domain.search.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.time.Duration

/**
 * What is known about one place, whoever knows it.
 *
 * The one shape every source of place details is turned into, through a
 * [com.takaotech.ktravel.domain.search.adapter.PlaceDetailsAdapter]: the screen reads this and never
 * a provider's own type, so a new provider is a new adapter and nothing else.
 *
 * The richer half — contacts, opening hours — is read on demand, for a place the traveller is
 * looking at, rather than for every result of a search: a provider bills the details apart, and most
 * results are never opened. Until then, or for a place no provider knows, those lists are empty.
 *
 * @property title The name of the place.
 * @property coordinate Where the place is.
 * @property addressLabel The full address as the provider writes it.
 * @property locality The city or town, when the provider knows it.
 * @property category The kind of place, when it is one the trip filters by.
 * @property contacts Ways to reach the place, in the order the provider lists them.
 * @property openingHours When the place is open, in its own time zone. Empty when the provider does
 *   not know; a day no period covers is a day the place is closed.
 * @property timeZoneId IANA identifier of the time zone the opening hours are in, such as
 *   `Europe/Rome`.
 * @property closure Whether the place has closed for good, `null` when it is not known to have.
 * @property reference How the provider that knows this place identifies it, or `null` for a place no
 *   provider knows, such as typed coordinates.
 */
data class PlaceDetails(
    val title: String,
    val coordinate: GeoCoordinate,
    val addressLabel: String? = null,
    val locality: String? = null,
    val category: PlaceCategory? = null,
    val contacts: ImmutableList<PlaceContact> = persistentListOf(),
    val openingHours: ImmutableList<OpeningPeriod> = persistentListOf(),
    val timeZoneId: String? = null,
    val closure: PlaceClosure? = null,
    val reference: PlaceReference? = null,
)

/**
 * One way to reach a place.
 *
 * @property kind What [value] is.
 * @property value The number, the address or the URL, as the provider writes it.
 * @property label What the provider says it is for, when it says.
 */
data class PlaceContact(val kind: PlaceContactKind, val value: String, val label: String? = null)

/** What a [PlaceContact] is, and so what tapping it should open. */
enum class PlaceContactKind {
    /** A landline number, to dial. */
    PHONE,

    /** A mobile number, to dial or to message. */
    MOBILE,

    /** A web page, to open in the browser. */
    WEBSITE,

    /** An email address, to write to. */
    EMAIL,
}

/**
 * A recurring span of time a place is open.
 *
 * It may cross midnight: a bar opening at 18:00 for eight hours closes at 02:00 of the following
 * day, which is still the period of the day it opened on.
 *
 * @property days The days it opens on, never empty.
 * @property opensAt Local opening time.
 * @property duration How long it stays open.
 */
data class OpeningPeriod(val days: ImmutableSet<DayOfWeek>, val opensAt: LocalTime, val duration: Duration)

/** How certain the provider is that a place has closed for good. */
enum class PlaceClosure {
    /** The place is known to have closed. */
    CONFIRMED,

    /** The place is suspected to have closed. */
    POSSIBLE,
}
