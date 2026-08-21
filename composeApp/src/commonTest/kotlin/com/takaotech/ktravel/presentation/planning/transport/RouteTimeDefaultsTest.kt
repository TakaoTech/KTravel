package com.takaotech.ktravel.presentation.planning.transport

import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.VisitScheduleUi
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

private val ZONE = TimeZone.of("Europe/Rome")

/** A clock that always answers the same thing, which is what makes the rounding assertable. */
private fun clockAt(iso: String): Clock = object : Clock {
    override fun now(): Instant = Instant.parse(iso)
}

private fun place(schedule: VisitScheduleUi?) = StepUi.Place(name = "Duomo", lat = 0.0, lng = 0.0, schedule = schedule)

class RouteTimeDefaultsTest :
    BehaviorSpec({

        given("a previous stop with a visit that ends at a known hour") {
            val startPlace = place(
                VisitScheduleUi(
                    startTime = LocalTime(hour = 9, minute = 0),
                    endTime = LocalTime(hour = 10, minute = 30),
                ),
            )

            `when`("a departure hour is suggested for the leg that follows it") {
                val suggestion = departureSuggestion(startPlace)

                then("it is the hour the visit ends, which is when the traveller leaves") {
                    suggestion shouldBe LocalTime(hour = 10, minute = 30)
                }
            }
        }

        given("a next stop with a visit that starts at a known hour") {
            val endPlace = place(
                VisitScheduleUi(
                    startTime = LocalTime(hour = 11, minute = 15),
                    endTime = LocalTime(hour = 13, minute = 0),
                ),
            )

            `when`("an arrival hour is suggested for the leg that precedes it") {
                val suggestion = arrivalSuggestion(endPlace)

                then("it is the hour the visit starts, which is when the traveller must be there") {
                    suggestion shouldBe LocalTime(hour = 11, minute = 15)
                }
            }
        }

        given("a stop with no schedule at all") {
            val unscheduled = place(schedule = null)

            `when`("either hour is suggested from it") {
                then("there is nothing to suggest") {
                    departureSuggestion(unscheduled).shouldBeNull()
                    arrivalSuggestion(unscheduled).shouldBeNull()
                }
            }
        }

        given("a stop that is missing the half of the schedule being asked for") {
            val onlyStart = place(VisitScheduleUi(startTime = LocalTime(hour = 9, minute = 0)))
            val onlyEnd = place(VisitScheduleUi(endTime = LocalTime(hour = 10, minute = 30)))

            `when`("the other half is asked for") {
                then("nothing is invented from the half that is there") {
                    departureSuggestion(onlyStart).shouldBeNull()
                    arrivalSuggestion(onlyEnd).shouldBeNull()
                }
            }
        }

        given("no stop at all, which is what the screen holds before the plan loads") {
            `when`("either hour is suggested") {
                then("there is nothing to suggest") {
                    departureSuggestion(null).shouldBeNull()
                    arrivalSuggestion(null).shouldBeNull()
                }
            }
        }

        given("a clock reading an hour that is not a round five minutes") {
            `when`("the fallback hour is computed") {
                // 12:33 in Rome, which is 10:33 UTC.
                val fallback = clockAt("2026-05-18T10:33:12Z").nowRoundedUp(ZONE)

                then("it is rounded up to the next five minutes, never to one already gone") {
                    fallback shouldBe LocalTime(hour = 12, minute = 35)
                }
            }
        }

        given("a clock reading an hour that is already a round five minutes") {
            `when`("the fallback hour is computed") {
                val fallback = clockAt("2026-05-18T10:35:00Z").nowRoundedUp(ZONE)

                then("it is left alone") {
                    fallback shouldBe LocalTime(hour = 12, minute = 35)
                }
            }
        }

        given("a clock reading the last minutes of the day") {
            `when`("the fallback hour is computed") {
                // 23:58 in Rome.
                val fallback = clockAt("2026-05-18T21:58:00Z").nowRoundedUp(ZONE)

                then("it stays on the day the leg belongs to instead of rolling into the next") {
                    fallback shouldBe LocalTime(hour = 23, minute = 55)
                }
            }
        }
    })
