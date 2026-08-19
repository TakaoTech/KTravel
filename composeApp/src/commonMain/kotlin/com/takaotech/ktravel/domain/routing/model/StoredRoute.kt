package com.takaotech.ktravel.domain.routing.model

import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents

// The bridge from a calculation to the plan it gets saved into.
//
// [Route] is the shape a step carries once it is part of a trip, and it is deliberately poorer than
// either answer: it is read back from an archive that older builds wrote, so widening it means a
// schema migration. Until there is a reason to pay for one, a saved journey keeps the shape of the
// route and loses what only a live timetable can tell you — the operator, the intermediate stops,
// how the line is coloured. What it must not lose is the mode, the times and the geometry, which is
// what a plan is drawn from.

/** The alternative the traveller picked, as the plan stores it. */
fun RoutingRoute.toStoredRoute(): Route = Route(
    sections = sections.map { section ->
        RouteSection(
            summary = section.summary,
            actions = section.actions,
            departure = section.departure,
            arrival = section.arrival,
            transport = RouteTransport(mode = section.mode),
            polyline = section.polyline,
            tollSystems = section.tollSystems,
            tolls = section.tolls,
        )
    },
)

/**
 * The journey the traveller picked, as the plan stores it.
 *
 * A leg becomes a section carrying the mode it was travelled in, so a saved journey still reads as
 * "walk, train, walk" rather than as an undifferentiated list.
 */
fun TransitJourney.toStoredRoute(): Route = Route(
    sections = steps.map { leg ->
        RouteSection(
            summary = leg.summary,
            departure = leg.storedDeparture(),
            arrival = leg.storedArrival(),
            transport = RouteTransport(mode = leg.storedMode()),
            polyline = leg.polyline,
        )
    },
)

/**
 * What the step is filed under.
 *
 * The first vehicle and not the first leg: a journey almost always starts on foot, and taking the
 * mode of the first leg filed every train ride in the plan as a walk.
 */
fun TransitJourney.storedTransportMode(): String? = rides.firstOrNull()?.line?.mode

private fun TransitStep.storedMode(): String = when (this) {
    is TransitStep.Walk -> PEDESTRIAN_MODE
    is TransitStep.Ride -> line.mode
}

private fun TransitStep.storedDeparture(): RouteDeparture? = when (this) {
    is TransitStep.Walk -> from?.let { RouteDeparture(it, departure?.toDateTimeComponents()) }
    is TransitStep.Ride -> boarding?.let { RouteDeparture(it.location, departure?.toDateTimeComponents()) }
}

private fun TransitStep.storedArrival(): RouteDeparture? = when (this) {
    is TransitStep.Walk -> to?.let { RouteDeparture(it, arrival?.toDateTimeComponents()) }
    is TransitStep.Ride -> alighting?.let { RouteDeparture(it.location, arrival?.toDateTimeComponents()) }
}

/**
 * The stored model dates from when times arrived as strings, so it holds a [DateTimeComponents].
 *
 * Building one means formatting and parsing again, because that type has no public constructor: it
 * exists to be produced by a parser. The round trip is cheap and it is the only way to keep the
 * offset — which is the local one at the stop, and the only reason the field is not just an instant.
 */
private fun TransitTime.toDateTimeComponents(): DateTimeComponents = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
    .parse(instant.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET, offset))

/** How a leg on foot is filed, in the same vocabulary the road profile uses for one. */
private const val PEDESTRIAN_MODE = "PEDESTRIAN"
