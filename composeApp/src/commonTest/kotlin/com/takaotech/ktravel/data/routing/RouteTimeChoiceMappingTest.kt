package com.takaotech.ktravel.data.routing

import com.takaotech.ktravel.domain.routing.RouteTimeChoice
import com.takaotech.gunzou.api.common.RouteTime
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/** Rome in May, which is UTC+2 — a fixed zone, so the test says the same thing on every machine. */
private val ZONE = TimeZone.of("Europe/Rome")

private val DAY = LocalDate(year = 2026, month = Month.MAY, day = 18)

private val TEN_THIRTY = LocalTime(hour = 10, minute = 30)

class RouteTimeChoiceMappingTest :
    BehaviorSpec({

        given("a leg the traveller wants to start immediately") {
            `when`("the choice is translated for the navigator") {
                val time = RouteTimeChoice.Now.toRouteTime(DAY, ZONE)

                then("it asks for no particular time, and the provider uses the current one") {
                    time shouldBe RouteTime.Now
                }
            }
        }

        given("a departure fixed at an hour of the day the leg belongs to") {
            `when`("the choice is translated for the navigator") {
                val time = RouteTimeChoice.DepartAt(TEN_THIRTY).toRouteTime(DAY, ZONE)

                then("it carries that hour as an instant resolved in the given zone") {
                    time shouldBe RouteTime.DepartAt(Instant.parse("2026-05-18T08:30:00Z"))
                }
            }
        }

        given("an arrival the traveller cannot be late for") {
            `when`("the choice is translated for the navigator") {
                val time = RouteTimeChoice.ArriveBy(TEN_THIRTY).toRouteTime(DAY, ZONE)

                then("it asks the provider to plan backwards from that instant") {
                    time shouldBe RouteTime.ArriveBy(Instant.parse("2026-05-18T08:30:00Z"))
                }
            }
        }

        given("the same hour on the same day") {
            `when`("it is read in two different zones") {
                val rome = RouteTimeChoice.DepartAt(TEN_THIRTY).toRouteTime(DAY, ZONE)
                val utc = RouteTimeChoice.DepartAt(TEN_THIRTY).toRouteTime(DAY, TimeZone.UTC)

                then("the two instants differ by the offset between them") {
                    rome shouldBe RouteTime.DepartAt(Instant.parse("2026-05-18T08:30:00Z"))
                    utc shouldBe RouteTime.DepartAt(Instant.parse("2026-05-18T10:30:00Z"))
                }
            }
        }
    })
