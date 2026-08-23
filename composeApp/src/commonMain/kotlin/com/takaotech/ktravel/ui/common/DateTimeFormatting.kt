@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.ui.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/*
 * The date and time formats the app writes on screen.
 *
 * They live together because they have to agree: a day printed as `18/05/2026` next to a timestamp
 * printed as `2026-05-18` reads as two different dates to anyone scanning the page. Every screen
 * takes its formatting from here rather than declaring a private `LocalDate.Format { }` of its own,
 * which is how the divergence started.
 */

/** Both halves of a clock are written with two digits, so `9:05` is printed `09:05`. */
private const val CLOCK_DIGITS = 2

/** Day, month and year, which is what an hour needs qualifying with and nothing more. */
private val DAY_MONTH_YEAR = LocalDate.Format {
    day()
    char('/')
    monthNumber()
    char('/')
    year()
}

/** The same date with the clock after it, for a moment rather than a day. */
private val DAY_MONTH_YEAR_CLOCK = LocalDateTime.Format {
    date(DAY_MONTH_YEAR)
    char(' ')
    hour()
    char(':')
    minute()
}

/** Weekday spelled out, then the date: the heading of a day in the plan. */
private val WEEKDAY_DAY_MONTH_YEAR = LocalDate.Format {
    // TODO Add support for other languages
    // temporary candidate for fix https://github.com/adrcotfas/kotlinx-datetime-names
    dayOfWeek(DayOfWeekNames.ENGLISH_FULL)
    char(' ')
    day()
    char('-')
    monthNumber()
    char('-')
    year()
}

/** The clock the traveller reads, as two digits and two digits. */
internal fun LocalTime.formatClock(): String {
    val hours = hour.toString().padStart(CLOCK_DIGITS, '0')
    val minutes = minute.toString().padStart(CLOCK_DIGITS, '0')
    return "$hours:$minutes"
}

/** The clock part of a moment, the date dropped: see [formatClock]. */
internal fun LocalDateTime.formatClock(): String = time.formatClock()

/** `18/05/2026`. */
internal fun LocalDate.formatDayMonthYear(): String = DAY_MONTH_YEAR.format(this)

/** `18/05/2026 10:30`. */
internal fun LocalDateTime.formatDayMonthYearClock(): String = DAY_MONTH_YEAR_CLOCK.format(this)

/**
 * `18/05/2026 10:30`, in the reader's own timezone.
 *
 * For the moments that are about the traveller sitting in front of the app — when something was
 * computed, when it was saved — and not about a departure board somewhere else, which carries its
 * own offset and is formatted from the [LocalDateTime] that offset produced.
 */
internal fun Instant.formatDayMonthYearClockHere(): String =
    toLocalDateTime(TimeZone.currentSystemDefault()).formatDayMonthYearClock()

/** `Monday 18-05-2026`. */
internal fun LocalDate.formatWeekdayDayMonthYear(): String = WEEKDAY_DAY_MONTH_YEAR.format(this)

/** `2026-05-18`, the form the date pickers echo back. */
internal fun LocalDate.formatIso(): String = LocalDate.Formats.ISO.format(this)
