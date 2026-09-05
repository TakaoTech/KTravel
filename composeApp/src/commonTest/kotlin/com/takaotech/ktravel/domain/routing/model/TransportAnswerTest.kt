@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.domain.routing.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/**
 * What the two kinds of answer say about themselves.
 *
 * The screens ask an answer three things — how long it takes, what to draw, which vehicle it is
 * filed under — and the two kinds answer them differently. Getting either wrong is not visible
 * until a trip is on screen, so it is pinned here.
 */
class TransportAnswerTest : BehaviorSpec() {

    private fun summary(duration: Int, metres: Double) =
        RouteSummary(durationSeconds = duration.minutes, distance = metres * Length.meters)

    /** Central European summer time, the offset the fixture's departure boards read in. */
    private val offset = UtcOffset(hours = 2)

    private fun time(hour: Int, minute: Int) = TransitTime(
        instant = LocalDateTime(year = 2026, month = Month.MAY, day = 30, hour = hour, minute = minute)
            .toInstant(offset),
        offset = offset,
    )

    init {
        given("a road route") {
            val answer = TransportAnswer.Routing(
                RoutingRoute(
                    summary = summary(14, 10_400.0),
                    sections = listOf(
                        RoutingSection(
                            summary = summary(14, 10_400.0),
                            mode = "car",
                            polyline = "road-shape",
                        ),
                    ),
                ),
            )

            then("its totals should be the ones the navigator aggregated") {
                answer.summary.durationSeconds shouldBe 14.minutes
            }

            then("it should be filed under the vehicle it is driven in") {
                answer.principalMode shouldBe "car"
            }

            then("it should draw its geometry, with no line colour to use") {
                answer.paths shouldBe listOf(TransportPath("road-shape"))
            }
        }

        given("a journey that waits on a platform between two vehicles") {
            // Travelled 8 + 22 = 30 minutes, but the traveller is out from 09:12 to 09:52.
            val answer = TransportAnswer.Transit(
                TransitJourney(
                    summary = summary(30, 9_000.0),
                    steps = listOf(
                        TransitStep.Walk(
                            summary = summary(8, 600.0),
                            polyline = "walk-shape",
                            departure = time(9, 12),
                            arrival = time(9, 20),
                        ),
                        TransitStep.Ride(
                            summary = summary(22, 8_400.0),
                            line = TransitLine(mode = "SUBWAY", name = "M1", color = "#D52B1E"),
                            polyline = "ride-shape",
                            boarding = TransitStop(RouteLocation(45.46, 9.19), departure = time(9, 30)),
                            alighting = TransitStop(RouteLocation(45.53, 9.24), arrival = time(9, 52)),
                        ),
                    ),
                ),
            )

            then("its duration should be door to door, waits included") {
                // Not the 30 minutes travelled: the ten spent on the platform are part of the day.
                answer.summary.durationSeconds shouldBe 40.minutes
            }

            then("it should be filed under the first vehicle, not under the walk to it") {
                answer.principalMode shouldBe "SUBWAY"
            }

            then("each shape should carry the colour of the line that runs it") {
                answer.paths shouldBe listOf(
                    TransportPath("walk-shape"),
                    TransportPath("ride-shape", "#D52B1E"),
                )
            }
        }

        given("a journey the feed timed at neither end") {
            val answer = TransportAnswer.Transit(
                TransitJourney(
                    summary = summary(25, 7_000.0),
                    steps = listOf(
                        TransitStep.Ride(
                            summary = summary(25, 7_000.0),
                            line = TransitLine(mode = "BUS"),
                        ),
                    ),
                ),
            )

            then("it should fall back to the time actually travelled") {
                answer.summary.durationSeconds shouldBe 25.minutes
            }

            then("it should have nothing to draw") {
                answer.paths shouldBe emptyList()
            }
        }

        given("a journey that is nothing but walking") {
            val answer = TransportAnswer.Transit(
                TransitJourney(
                    summary = summary(12, 900.0),
                    steps = listOf(TransitStep.Walk(summary = summary(12, 900.0))),
                ),
            )

            then("it should be filed under no vehicle rather than under the walk") {
                answer.principalMode.shouldBeNull()
            }
        }
    }
}
