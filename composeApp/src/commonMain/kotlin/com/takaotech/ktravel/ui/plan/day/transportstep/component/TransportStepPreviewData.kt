package com.takaotech.ktravel.ui.plan.day.transportstep.component

import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.RoutingSection
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import com.takaotech.ktravel.presentation.plan.day.TransportStepUi as TransportStepUiModel

/** Central European summer time, which is the offset the fixture's clocks read in. */
internal val TRANSPORT_PREVIEW_OFFSET = UtcOffset(hours = 2)

/** Totals of the road fixture, shared by the route and by the leg that carries it. */
private val ROAD_SUMMARY = RouteSummary(1500.seconds, 5200.0 * Length.meters)

/** Totals of the journey fixture, walking and riding together. */
private val TRANSIT_SUMMARY = RouteSummary(1800.seconds, 9000.0 * Length.meters)

/** A moment on a fixed day, so that a preview never moves with the clock. */
internal fun previewTime(hour: Int, minute: Int): LocalDateTime =
    LocalDateTime(year = 2026, month = 5, day = 14, hour = hour, minute = minute)

/**
 * A leg covered by road: one section, its manoeuvres, and a clock at both ends.
 *
 * The arguments are the parts a preview needs to take away or replace. A leg filed before the plan
 * started recording it has no [TransportStepUiModel.calculatedAt]; an answer asked for "now" comes
 * back with no times at all; and [mode] is what the chip reads, which is not always a mode the
 * project has a word for.
 */
internal fun previewRoadStep(
    departure: LocalDateTime? = previewTime(hour = 9, minute = 12),
    arrival: LocalDateTime? = previewTime(hour = 9, minute = 37),
    calculatedAt: Instant? = previewTime(hour = 8, minute = 55).toInstant(TRANSPORT_PREVIEW_OFFSET),
    mode: String = "car",
    type: TransportType = TransportType.CAR,
): TransportStepUiModel = TransportStepUiModel(
    type = type,
    fromName = "Kyoto Station",
    toName = "Fushimi Inari Taisha",
    answer = TransportAnswer.Routing(
        RoutingRoute(
            summary = ROAD_SUMMARY,
            sections = listOf(
                RoutingSection(
                    summary = ROAD_SUMMARY,
                    mode = mode,
                    actions = listOf(
                        RouteAction(
                            action = "depart",
                            durationSeconds = 180.seconds,
                            distanceMeters = 700.0 * Length.meters,
                            instruction = "Head south on Karasuma-dori",
                        ),
                        RouteAction(
                            action = "turn",
                            durationSeconds = 420.seconds,
                            distanceMeters = 2600.0 * Length.meters,
                            instruction = "Turn left onto Kujo-dori",
                        ),
                        RouteAction(
                            action = "arrive",
                            durationSeconds = 900.seconds,
                            distanceMeters = 1900.0 * Length.meters,
                            instruction = "Arrive at Fushimi Inari Taisha",
                        ),
                    ),
                ),
            ),
        ),
    ),
    totalDuration = ROAD_SUMMARY.durationSeconds,
    totalDistance = ROAD_SUMMARY.distance,
    departure = departure,
    arrival = arrival,
    calculatedAt = calculatedAt,
)

/**
 * A leg covered on scheduled services: a walk to the platform, then the line that carries it.
 *
 * The two answers are not two skins of the same thing — a journey has no manoeuvres and a road
 * route has no line — so the components that read the answer need both fixtures, not one.
 */
internal fun previewTransitStep(
    departure: LocalDateTime? = previewTime(hour = 9, minute = 12),
    arrival: LocalDateTime? = previewTime(hour = 9, minute = 42),
): TransportStepUiModel = TransportStepUiModel(
    type = TransportType.TRAIN,
    fromName = "Kyoto Station",
    toName = "Gion-Shijo",
    answer = TransportAnswer.Transit(
        TransitJourney(
            summary = TRANSIT_SUMMARY,
            steps = listOf(
                TransitStep.Walk(
                    summary = RouteSummary(
                        durationSeconds = 480.seconds,
                        distance = 600.0 * Length.meters,
                    ),
                ),
                TransitStep.Ride(
                    summary = RouteSummary(
                        durationSeconds = 1320.seconds,
                        distance = 8400.0 * Length.meters,
                    ),
                    line = TransitLine(mode = "SUBWAY", name = "Karasuma line", color = "#009944"),
                ),
            ),
        ),
    ),
    totalDuration = TRANSIT_SUMMARY.durationSeconds,
    totalDistance = TRANSIT_SUMMARY.distance,
    departure = departure,
    arrival = arrival,
    calculatedAt = previewTime(hour = 8, minute = 55).toInstant(TRANSPORT_PREVIEW_OFFSET),
)
