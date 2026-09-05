package com.takaotech.ktravel.presentation.plan.transport

import com.takaotech.ktravel.domain.routing.RouteTimeChoice
import com.takaotech.ktravel.presentation.plan.StepUi
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * The three things a traveller can ask of a leg, as one value the selector can be drawn from.
 *
 * [RouteTimeChoice] is the answer and this is the question: the choice carries an hour and the mode
 * does not, which is what lets a segmented button be selected without the screen having to invent
 * an hour to compare against.
 */
enum class RouteTimeMode { NOW, DEPART_AT, ARRIVE_BY }

/** Which segment of the selector a choice sits on. */
internal val RouteTimeChoice.mode: RouteTimeMode
    get() = when (this) {
        RouteTimeChoice.Now -> RouteTimeMode.NOW
        is RouteTimeChoice.DepartAt -> RouteTimeMode.DEPART_AT
        is RouteTimeChoice.ArriveBy -> RouteTimeMode.ARRIVE_BY
    }

/** The hour a choice carries, or null when it carries none. */
internal val RouteTimeChoice.timeOrNull: LocalTime?
    get() = when (this) {
        RouteTimeChoice.Now -> null
        is RouteTimeChoice.DepartAt -> time
        is RouteTimeChoice.ArriveBy -> time
    }

/** How coarse a suggested hour is, in minutes: nobody plans a departure to the minute. */
private const val ROUNDING_MINUTES = 5

/**
 * The hour to propose for a departure: the one the traveller leaves the previous place at.
 *
 * The *end* of the visit and not its start, because that is when the leg begins — a stop that runs
 * from 09:00 to 10:30 is a stop you leave at 10:30.
 */
internal fun departureSuggestion(startPlace: StepUi.Place?): LocalTime? = startPlace?.schedule?.endTime

/**
 * The hour to propose for an arrival: the one the next visit starts at.
 *
 * Often absent, and legitimately so — the last place of a day carries no schedule once the day has
 * more than one — which is what the fallback in [nowRoundedUp] is for.
 */
internal fun arrivalSuggestion(endPlace: StepUi.Place?): LocalTime? = endPlace?.schedule?.startTime

/**
 * Now, rounded up to the next five minutes, for a picker that has nothing better to open on.
 *
 * Rounded *up* rather than to the nearest, so the hour offered is never one that has already
 * passed.
 */
internal fun Clock.nowRoundedUp(zone: TimeZone): LocalTime {
    val now = now().toLocalDateTime(zone).time
    val minutes = now.hour * MINUTES_PER_HOUR + now.minute
    val rounded = ((minutes + ROUNDING_MINUTES - 1) / ROUNDING_MINUTES) * ROUNDING_MINUTES

    // Rounding the last minutes of the day lands on the next one, which this leg is not on: the day
    // ends at 23:55 instead, which is the latest hour that is still today.
    if (rounded >= MINUTES_PER_DAY) {
        return LocalTime(
        hour = 23,
        minute = MINUTES_PER_HOUR - ROUNDING_MINUTES,
    )
    }

    return LocalTime(hour = rounded / MINUTES_PER_HOUR, minute = rounded % MINUTES_PER_HOUR)
}

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
