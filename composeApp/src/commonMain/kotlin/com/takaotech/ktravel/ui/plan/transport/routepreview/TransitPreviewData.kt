package com.takaotech.ktravel.ui.plan.transport.routepreview

import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.TransitAgency
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransitStop
import com.takaotech.ktravel.domain.routing.model.TransitTime
import com.takaotech.ktravel.domain.routing.model.WheelchairAccess
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.minutes

/**
 * Central European summer time, which is the offset the departure boards in the fixture read in.
 */
internal val PREVIEW_OFFSET = UtcOffset(hours = 2)

/**
 * A moment on the fixture's timetable, fixed rather than relative to now so the preview never
 * moves.
 */
internal fun previewTime(hour: Int, minute: Int): TransitTime = TransitTime(
    instant = LocalDateTime(
        year = 2026,
        monthNumber = 5,
        dayOfMonth = 14,
        hour = hour,
        minute = minute,
    )
        .toInstant(PREVIEW_OFFSET),
    offset = PREVIEW_OFFSET,
)

internal fun previewStop(
    name: String,
    lat: Double,
    lng: Double,
    arrival: TransitTime? = null,
    departure: TransitTime? = null,
): TransitStop = TransitStop(
    location = RouteLocation(lat = lat, lng = lng),
    name = name,
    arrival = arrival,
    departure = departure,
)

internal val PREVIEW_AGENCY = TransitAgency(name = "ATM", id = "atm", website = "")

/** Assago to Brera, changing from the underground onto a tram. */
internal val PREVIEW_WITH_CHANGE = TransitJourney(
    summary = RouteSummary(durationSeconds = 43.minutes, distance = 12_190 * Length.meters),
    steps = listOf(
        TransitStep.Walk(
            summary = RouteSummary(durationSeconds = 4.minutes, distance = 320 * Length.meters),
            departure = previewTime(9, 27),
            arrival = previewTime(9, 31),
            from = RouteLocation(lat = 45.40887, lng = 9.12565),
            to = RouteLocation(lat = 45.41043, lng = 9.12718),
        ),
        TransitStep.Ride(
            summary = RouteSummary(durationSeconds = 21.minutes, distance = 9_200 * Length.meters),
            line = TransitLine(
                mode = "SUBWAY",
                name = "M2",
                shortName = "M2",
                category = "Metropolitana",
                headsign = "Cologno Nord",
                color = "#00A94F",
                textColor = "#FFFFFF",
                wheelchairAccessible = WheelchairAccess.YES,
                url = "",
            ),
            agency = PREVIEW_AGENCY,
            boarding = previewStop(
                "Assago Milanofiori Forum",
                45.41043,
                9.12718,
                departure = previewTime(9, 31),
            ),
            alighting = previewStop("Cadorna FN", 45.46870, 9.17570, arrival = previewTime(9, 52)),
            intermediateStops = listOf(
                previewStop("Famagosta", 45.43128, 9.15676, departure = previewTime(9, 38)),
                previewStop("Porta Genova FS", 45.45191, 9.17300, departure = previewTime(9, 44)),
                previewStop("Sant'Ambrogio", 45.46246, 9.17500, departure = previewTime(9, 49)),
            ),
        ),
        TransitStep.Walk(
            summary = RouteSummary(durationSeconds = 3.minutes, distance = 210 * Length.meters),
            departure = previewTime(9, 52),
            arrival = previewTime(9, 55),
        ),
        TransitStep.Ride(
            summary = RouteSummary(durationSeconds = 11.minutes, distance = 2_200 * Length.meters),
            line = TransitLine(
                mode = "LIGHT_RAIL",
                name = "1",
                shortName = "1",
                category = "Tram",
                headsign = "Greco",
                color = "#F5A623",
                textColor = "#000000",
                wheelchairAccessible = WheelchairAccess.LIMITED,
            ),
            agency = PREVIEW_AGENCY,
            boarding = previewStop("Cadorna", 45.46830, 9.17660, departure = previewTime(9, 57)),
            alighting = previewStop("Manzoni", 45.46980, 9.19170, arrival = previewTime(10, 8)),
            intermediateStops = listOf(
                previewStop("Cordusio", 45.46590, 9.18420, departure = previewTime(10, 2)),
            ),
        ),
        TransitStep.Walk(
            summary = RouteSummary(durationSeconds = 4.minutes, distance = 260 * Length.meters),
            departure = previewTime(10, 8),
            arrival = previewTime(10, 12),
        ),
    ),
)

/** The same trip on one coach: longer, but nothing to catch in the middle. */
internal val PREVIEW_DIRECT = TransitJourney(
    summary = RouteSummary(durationSeconds = 52.minutes, distance = 13_400 * Length.meters),
    steps = listOf(
        TransitStep.Walk(
            summary = RouteSummary(durationSeconds = 6.minutes, distance = 480 * Length.meters),
            departure = previewTime(9, 24),
            arrival = previewTime(9, 30),
        ),
        TransitStep.Ride(
            summary = RouteSummary(durationSeconds = 41.minutes, distance = 12_520 * Length.meters),
            line = TransitLine(
                mode = "BUS",
                name = "321",
                shortName = "321",
                category = "Autobus",
                headsign = "Milano Famagosta",
            ),
            agency = PREVIEW_AGENCY,
            boarding = previewStop(
                "Assago Forum",
                45.40887,
                9.12565,
                departure = previewTime(9, 30),
            ),
            alighting = previewStop(
                "Via Manzoni",
                45.46980,
                9.19170,
                arrival = previewTime(10, 11),
            ),
        ),
        TransitStep.Walk(
            summary = RouteSummary(durationSeconds = 5.minutes, distance = 400 * Length.meters),
            departure = previewTime(10, 11),
            arrival = previewTime(10, 16),
        ),
    ),
)

//region Preview
