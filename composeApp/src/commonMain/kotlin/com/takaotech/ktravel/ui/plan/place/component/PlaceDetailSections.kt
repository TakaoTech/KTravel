package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.search.model.OpeningPeriod
import com.takaotech.ktravel.domain.search.model.PlaceContact
import com.takaotech.ktravel.domain.search.model.PlaceContactKind
import com.takaotech.ktravel.ui.plan.place.previewTorrazzoDetails
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.times
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.call
import ktravel.composeapp.generated.resources.link_24dp
import ktravel.composeapp.generated.resources.mail
import ktravel.composeapp.generated.resources.place_detail_closed
import ktravel.composeapp.generated.resources.place_detail_contact_email
import ktravel.composeapp.generated.resources.place_detail_contact_mobile
import ktravel.composeapp.generated.resources.place_detail_contact_phone
import ktravel.composeapp.generated.resources.place_detail_contact_website
import ktravel.composeapp.generated.resources.place_detail_contacts
import ktravel.composeapp.generated.resources.place_detail_day_friday
import ktravel.composeapp.generated.resources.place_detail_day_monday
import ktravel.composeapp.generated.resources.place_detail_day_saturday
import ktravel.composeapp.generated.resources.place_detail_day_sunday
import ktravel.composeapp.generated.resources.place_detail_day_thursday
import ktravel.composeapp.generated.resources.place_detail_day_tuesday
import ktravel.composeapp.generated.resources.place_detail_day_wednesday
import ktravel.composeapp.generated.resources.place_detail_opening_hours
import ktravel.composeapp.generated.resources.smartphone
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val URL_SCHEME_SEPARATOR = "://"

/** A whole day on the clock, which a period closing after midnight wraps around. */
private val DAY: Measure<Time> = 24 * Time.hours

/**
 * The contacts of a place, one row each: tapping a row dials the number, opens the site or writes
 * the email, through whatever the platform has for it.
 */
@Composable
internal fun PlaceContactsSection(contacts: ImmutableList<PlaceContact>, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    DetailSection(title = stringResource(Res.string.place_detail_contacts), modifier = modifier) {
        contacts.forEach { contact ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    // Nothing to do when the platform has no app for the link: the value stays readable.
                    .clickable { runCatching { uriHandler.openUri(contact.toUri()) } }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    painter = painterResource(contact.kind.icon),
                    contentDescription = stringResource(contact.kind.label),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    contact.label?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** The week of a place, from Monday: its periods on each day, or that it is closed. */
@Composable
internal fun PlaceOpeningHoursSection(openingHours: ImmutableList<OpeningPeriod>, modifier: Modifier = Modifier) {
    val closed = stringResource(Res.string.place_detail_closed)
    DetailSection(title = stringResource(Res.string.place_detail_opening_hours), modifier = modifier) {
        openingHoursByDay(openingHours).forEach { day ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = stringResource(day.day.label),
                    modifier = Modifier.width(48.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = day.spans.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: closed,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier) {
        Text(
            text = title,
            modifier = Modifier.padding(bottom = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        content()
    }
}

/**
 * The opening hours of one day, as the card lists them.
 *
 * @property day The day of the week.
 * @property spans Each period that opens on [day], as `HH:mm–HH:mm` and in opening order. Empty when
 *   the place is closed that day.
 */
@Immutable
internal data class DayOpeningHours(val day: DayOfWeek, val spans: ImmutableList<String>)

/**
 * The week of [periods], one entry per day from Monday to Sunday.
 *
 * A period belongs to the day it opens on, even when it closes after midnight: a bar open on Friday
 * from 20:00 to 02:00 is Friday's, which is how a traveller reads it.
 */
internal fun openingHoursByDay(periods: List<OpeningPeriod>): ImmutableList<DayOpeningHours> =
    DayOfWeek.entries.map { day ->
        DayOpeningHours(
            day = day,
            spans = periods
                .filter { day in it.days }
                .sortedBy { it.opensAt }
                .map { "${it.opensAt.toClock()}–${it.closesAt().toClock()}" }
                .toImmutableList(),
        )
    }.toImmutableList()

/** Where [PlaceContact] leads when tapped: a number to dial, an address to write to, a page. */
internal fun PlaceContact.toUri(): String = when (kind) {
    PlaceContactKind.PHONE, PlaceContactKind.MOBILE ->
        "tel:" + value.filter { it.isDigit() || it == '+' }

    PlaceContactKind.EMAIL -> "mailto:$value"

    PlaceContactKind.WEBSITE -> if (URL_SCHEME_SEPARATOR in value) value else "https://$value"
}

/** The local time the period closes at, wrapped past midnight onto the next day's clock. */
private fun OpeningPeriod.closesAt(): LocalTime {
    val closing = opensAt.toSecondOfDay() * Time.seconds + duration.inWholeSeconds * Time.seconds

    return LocalTime.fromSecondOfDay(((closing `in` Time.seconds) % (DAY `in` Time.seconds)).toInt())
}

private fun LocalTime.toClock(): String = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

private val PlaceContactKind.icon: DrawableResource
    get() = when (this) {
        PlaceContactKind.PHONE -> Res.drawable.call
        PlaceContactKind.MOBILE -> Res.drawable.smartphone
        PlaceContactKind.WEBSITE -> Res.drawable.link_24dp
        PlaceContactKind.EMAIL -> Res.drawable.mail
    }

private val PlaceContactKind.label: StringResource
    get() = when (this) {
        PlaceContactKind.PHONE -> Res.string.place_detail_contact_phone
        PlaceContactKind.MOBILE -> Res.string.place_detail_contact_mobile
        PlaceContactKind.WEBSITE -> Res.string.place_detail_contact_website
        PlaceContactKind.EMAIL -> Res.string.place_detail_contact_email
    }

private val DayOfWeek.label: StringResource
    get() = when (this) {
        DayOfWeek.MONDAY -> Res.string.place_detail_day_monday
        DayOfWeek.TUESDAY -> Res.string.place_detail_day_tuesday
        DayOfWeek.WEDNESDAY -> Res.string.place_detail_day_wednesday
        DayOfWeek.THURSDAY -> Res.string.place_detail_day_thursday
        DayOfWeek.FRIDAY -> Res.string.place_detail_day_friday
        DayOfWeek.SATURDAY -> Res.string.place_detail_day_saturday
        DayOfWeek.SUNDAY -> Res.string.place_detail_day_sunday
    }

//region Previews
@Preview(showBackground = true)
@Composable
private fun PlaceDetailSectionsPreview() = KTravelTheme {
    Surface {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(320.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlaceContactsSection(contacts = previewTorrazzoDetails.contacts)
            PlaceOpeningHoursSection(openingHours = previewTorrazzoDetails.openingHours)
        }
    }
}
//endregion Previews
