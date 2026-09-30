package com.takaotech.ktravel.gunzou.server.endpoint.here.search

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.search.place.ContactKind
import com.takaotech.gunzou.api.search.place.OpeningPeriod
import com.takaotech.gunzou.api.search.place.PermanentClosure
import com.takaotech.gunzou.api.search.place.PlaceContact
import com.takaotech.gunzou.api.search.place.PlaceDetails
import com.takaotech.gunzou.api.search.place.PlaceTimeZone
import com.takaotech.gunzou.api.search.place.Weekday
import com.takaotech.gunzou.here.search.common.dto.Contact
import com.takaotech.gunzou.here.search.common.dto.ContactInformation
import com.takaotech.gunzou.here.search.common.dto.StructuredOpeningHours
import com.takaotech.gunzou.here.search.lookup.dto.response.LookupResponse
import com.takaotech.ktravel.gunzou.server.endpoint.NavigatorException

// What this server makes of a HERE Lookup answer.

private const val CLOSED_PERMANENTLY = "yes"
private const val MAYBE_CLOSED_PERMANENTLY = "maybe"

private const val MINUTES_PER_HOUR = 60
private const val RULE_SEPARATOR = ';'
private const val RULE_ASSIGNMENT = ':'
private const val LIST_SEPARATOR = ','
private const val FREQUENCY = "FREQ"
private const val BY_DAY = "BYDAY"

/** The frequencies a period repeats with that the contract can express as a set of weekdays. */
private val WEEKLY_FREQUENCIES = setOf("DAILY", "WEEKLY")

/** `T083000`: the time part of an iCalendar DATE-TIME, seconds optional. */
private val START_TIME = Regex("""T(\d{2})(\d{2})(\d{2})?""")

/** `PT10H30M`: an iCalendar DURATION of hours and minutes, either part optional. */
private val DURATION = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?""")

/** The iCalendar two letter day codes, in the order of [Weekday.ALL]. */
private val DAY_CODES: Map<String, Weekday> =
    listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU").zip(Weekday.ALL).toMap()

/**
 * Translates a HERE Lookup answer into the provider neutral place details.
 *
 * @throws NavigatorException [ErrorCode.PLACE_NOT_FOUND] when HERE answered without a position: the
 *   contract promises a pin, and a place that cannot be put on the map is, for a client, not found.
 */
internal fun LookupResponse.toPlaceDetails(): PlaceDetails {
    val position = position ?: throw NavigatorException(
        code = ErrorCode.PLACE_NOT_FOUND,
        message = "HERE knows '$id' but not where it is",
    )

    return PlaceDetails(
        id = id,
        title = title,
        resultType = hereResultType(resultType, localityType),
        position = GeoPoint(lat = position.lat, lng = position.lng),
        address = address.toSearchAddress(),
        category = categories?.toCategoryGroup(),
        bounds = mapView?.toGeoBounds(),
        contacts = contacts.orEmpty().flatMap { it.toPlaceContacts() },
        // The first entry is the place's own hours; the others, when present, belong to one of its
        // categories, such as the kitchen of a bar, and have no place in a single schedule.
        openingHours = openingHours?.firstOrNull()?.structured.orEmpty().mapNotNull { it.toOpeningPeriod() },
        timeZone = timeZone?.let { PlaceTimeZone(name = it.name, utcOffset = it.utcOffset) },
        permanentClosure = when (closedPermanently) {
            CLOSED_PERMANENTLY -> PermanentClosure.CONFIRMED
            MAYBE_CLOSED_PERMANENTLY -> PermanentClosure.POSSIBLE
            else -> null
        },
    )
}

/**
 * Every contact of one group, in the order HERE lists them.
 *
 * Fax and toll free numbers are left out: neither is something a traveller reaches a place by.
 */
private fun ContactInformation.toPlaceContacts(): List<PlaceContact> = buildList {
    addAll(phone.toPlaceContacts(ContactKind.PHONE))
    addAll(mobile.toPlaceContacts(ContactKind.MOBILE))
    addAll(www.toPlaceContacts(ContactKind.WEBSITE))
    addAll(email.toPlaceContacts(ContactKind.EMAIL))
}

private fun List<Contact>?.toPlaceContacts(kind: ContactKind): List<PlaceContact> =
    orEmpty().map { PlaceContact(kind = kind, value = it.value, label = it.label) }

/**
 * The same period in the contract's terms, or nothing when it cannot be read or opens for no time.
 *
 * HERE marks a closed day with a zero duration, which the contract expresses by the day being absent
 * from every period; a rule it cannot read is dropped rather than guessed, since a wrong opening time
 * is worse than none.
 */
internal fun StructuredOpeningHours.toOpeningPeriod(): OpeningPeriod? {
    val time = START_TIME.matchEntire(start)?.destructured
    val durationMinutes = duration.toMinutes()?.takeIf { it > 0 }
    val days = recurrence.toWeekdays()?.takeIf { it.isNotEmpty() }

    if (time == null || durationMinutes == null || days == null) {
        return null
    }
    val (hours, minutes) = time

    return OpeningPeriod(days = days, opensAt = "$hours:$minutes", durationMinutes = durationMinutes)
}

private fun String.toMinutes(): Int? {
    val match = DURATION.matchEntire(this) ?: return null
    val (hours, minutes) = match.destructured

    return (hours.toIntOrNull() ?: 0) * MINUTES_PER_HOUR + (minutes.toIntOrNull() ?: 0)
}

/**
 * The days a HERE recurrence rule opens on, or `null` when it is not a daily or weekly rule.
 *
 * HERE writes the rule with `:` where the RFC has `=`, such as `FREQ:DAILY;BYDAY:MO,TU`. Without a
 * `BYDAY` it applies to every day. A day code outside the seven is skipped.
 */
private fun String.toWeekdays(): List<Weekday>? {
    val parts = split(RULE_SEPARATOR).associate { part ->
        part.substringBefore(RULE_ASSIGNMENT).trim().uppercase() to part.substringAfter(RULE_ASSIGNMENT, "").trim()
    }

    val byDay = parts[BY_DAY]

    return when {
        parts[FREQUENCY]?.uppercase() !in WEEKLY_FREQUENCIES -> null
        byDay == null -> Weekday.ALL
        else -> byDay.split(LIST_SEPARATOR).mapNotNull { DAY_CODES[it.trim().uppercase()] }.distinct()
    }
}
