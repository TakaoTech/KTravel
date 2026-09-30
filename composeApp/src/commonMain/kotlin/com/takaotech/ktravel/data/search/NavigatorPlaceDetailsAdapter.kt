package com.takaotech.ktravel.data.search

import com.takaotech.gunzou.api.common.SearchProviderId
import com.takaotech.gunzou.api.search.place.ContactKind
import com.takaotech.gunzou.api.search.place.PermanentClosure
import com.takaotech.gunzou.api.search.place.Weekday
import com.takaotech.ktravel.domain.search.adapter.PlaceDetailsAdapter
import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.OpeningPeriod
import com.takaotech.ktravel.domain.search.model.PlaceClosure
import com.takaotech.ktravel.domain.search.model.PlaceContact
import com.takaotech.ktravel.domain.search.model.PlaceContactKind
import com.takaotech.ktravel.domain.search.model.PlaceDetails
import com.takaotech.ktravel.domain.search.model.PlaceReference
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.minutes
import com.takaotech.gunzou.api.search.place.OpeningPeriod as ApiOpeningPeriod
import com.takaotech.gunzou.api.search.place.PlaceContact as ApiPlaceContact
import com.takaotech.gunzou.api.search.place.PlaceDetails as ApiPlaceDetails

/**
 * The navigator's place details, as answered by [provider], in the shape every provider shares.
 *
 * The navigator already speaks one contract for every provider behind it, so a single adapter serves
 * them all; the provider is only needed to record who can be asked about the place again.
 *
 * Whatever the contract carries that this build cannot read — a contact kind, a day or a degree of
 * closure added by a newer navigator, an opening time that does not parse — is left out rather than
 * guessed: a wrong opening hour is worse than a missing one.
 */
internal class NavigatorPlaceDetailsAdapter(private val provider: SearchProviderId) :
    PlaceDetailsAdapter<ApiPlaceDetails> {

    override fun adapt(source: ApiPlaceDetails): PlaceDetails = PlaceDetails(
        title = source.title,
        coordinate = GeoCoordinate(lat = source.position.lat, lng = source.position.lng),
        addressLabel = source.address.label,
        locality = source.address.locality(),
        category = source.category?.toDomain(),
        contacts = source.contacts.mapNotNull { it.toDomain() }.toImmutableList(),
        openingHours = source.openingHours.mapNotNull { it.toDomain() }.toImmutableList(),
        timeZoneId = source.timeZone?.name,
        closure = when (source.permanentClosure) {
            PermanentClosure.CONFIRMED -> PlaceClosure.CONFIRMED
            PermanentClosure.POSSIBLE -> PlaceClosure.POSSIBLE
            else -> null
        },
        reference = PlaceReference(providerId = provider.value, placeId = source.id),
    )
}

private fun ApiPlaceContact.toDomain(): PlaceContact? {
    val kind = when (kind) {
        ContactKind.PHONE -> PlaceContactKind.PHONE
        ContactKind.MOBILE -> PlaceContactKind.MOBILE
        ContactKind.WEBSITE -> PlaceContactKind.WEBSITE
        ContactKind.EMAIL -> PlaceContactKind.EMAIL
        else -> return null
    }

    return PlaceContact(kind = kind, value = value, label = label)
}

private fun ApiOpeningPeriod.toDomain(): OpeningPeriod? {
    val days = days.mapNotNull { it.toDayOfWeek() }.toImmutableSet()
    val opensAt = opensAt.toLocalTimeOrNull()
    val duration = durationMinutes.takeIf { it > 0 }?.minutes

    return if (days.isEmpty() || opensAt == null || duration == null) {
        null
    } else {
        OpeningPeriod(days = days, opensAt = opensAt, duration = duration)
    }
}

private fun String.toLocalTimeOrNull(): LocalTime? = try {
    LocalTime.parse(this)
} catch (_: IllegalArgumentException) {
    null
}

private fun Weekday.toDayOfWeek(): DayOfWeek? = when (this) {
    Weekday.MONDAY -> DayOfWeek.MONDAY
    Weekday.TUESDAY -> DayOfWeek.TUESDAY
    Weekday.WEDNESDAY -> DayOfWeek.WEDNESDAY
    Weekday.THURSDAY -> DayOfWeek.THURSDAY
    Weekday.FRIDAY -> DayOfWeek.FRIDAY
    Weekday.SATURDAY -> DayOfWeek.SATURDAY
    Weekday.SUNDAY -> DayOfWeek.SUNDAY
    else -> null
}
