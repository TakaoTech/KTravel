package com.takaotech.ktravel.testutil

import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteDeparture
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.RoutingSection
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.format.DateTimeComponents
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Answers to file into a plan, for the tests that need a transport step and do not care which kind.
 *
 * A transport now carries the answer in the shape it was given, so a test that only wants "some
 * leg" would otherwise have to build a route or a journey by hand every time.
 */
fun summaryOf(duration: Duration = 30.minutes, metres: Double = 1000.0): RouteSummary =
    RouteSummary(durationSeconds = duration, distance = metres * Length.meters)

/**
 * A road route, by default a single sectionless-of-detail leg with no manoeuvres.
 *
 * [departure] and [arrival] are ISO 8601 with the offset in force where the moment happens, the way
 * the navigator answers them, and default to absent: a route asked for "now" comes back untimed.
 */
fun roadAnswer(
    duration: Duration = 30.minutes,
    metres: Double = 1000.0,
    mode: String = "car",
    actions: List<RouteAction> = emptyList(),
    polyline: String? = null,
    departure: String? = null,
    arrival: String? = null,
): TransportAnswer.Routing = TransportAnswer.Routing(
    RoutingRoute(
        summary = summaryOf(duration, metres),
        sections = listOf(
            RoutingSection(
                summary = summaryOf(duration, metres),
                mode = mode,
                actions = actions,
                departure = departure?.let(::waypointAt),
                arrival = arrival?.let(::waypointAt),
                polyline = polyline,
            ),
        ),
    ),
)

/** A dated waypoint at the origin of the coordinate system: the tests only read the time off it. */
private fun waypointAt(isoWithOffset: String): RouteDeparture = RouteDeparture(
    location = RouteLocation(lat = 0.0, lng = 0.0),
    time = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.parse(isoWithOffset),
)

/** A journey that walks to a stop, rides [mode], and walks off — which is every journey. */
fun transitAnswer(
    mode: String = "SUBWAY",
    line: TransitLine = TransitLine(mode = mode),
    duration: Duration = 30.minutes,
    metres: Double = 1000.0,
): TransportAnswer.Transit = TransportAnswer.Transit(
    TransitJourney(
        summary = summaryOf(duration, metres),
        steps = listOf(
            TransitStep.Walk(summary = summaryOf(4.minutes, 200.0)),
            TransitStep.Ride(summary = summaryOf(duration - 8.minutes, metres - 300.0), line = line),
            TransitStep.Walk(summary = summaryOf(4.minutes, 100.0)),
        ),
    ),
)
