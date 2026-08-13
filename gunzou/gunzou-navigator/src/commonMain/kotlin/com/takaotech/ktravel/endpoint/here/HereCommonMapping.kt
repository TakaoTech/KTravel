package com.takaotech.ktravel.endpoint.here

import com.takaotech.navigator.api.common.ZonedTime
import com.takaotech.navigator.api.response.NoticeSeverity
import kotlinx.datetime.format.DateTimeComponents

private const val CRITICAL_SEVERITY = "critical"

// The handful of translations both HERE profiles need. Everything else is specific to one of them
// and lives beside it.

/**
 * Reads a HERE timestamp, keeping the offset it was written with.
 *
 * The offset is the local one at the place the time refers to, and it is what a traveller reads on a
 * departure board. Parsing to a bare instant would throw it away, and no client could get it back —
 * which on the transit profile means showing a train time in the wrong timezone as soon as the
 * journey crosses a border.
 *
 * A timestamp that does not parse becomes null rather than a failure: a route whose geometry and
 * distance are intact is worth returning without one of its times.
 */
internal fun String.toZonedTime(): ZonedTime? {
    val parsed = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.parseOrNull(this) ?: return null

    return ZonedTime(instant = parsed.toInstantUsingOffset(), offsetSeconds = parsed.toUtcOffset().totalSeconds)
}

/** How much a HERE notice should worry the caller. Anything but `critical` is advisory. */
internal fun String?.toNoticeSeverity(): NoticeSeverity =
    if (equals(CRITICAL_SEVERITY, ignoreCase = true)) NoticeSeverity.CRITICAL else NoticeSeverity.INFO
