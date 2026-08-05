package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.domain.model.TravelPlanEditor.clearFinalDestinationSchedules
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Verifies the rule that the final destination place of a day cannot hold a visit schedule (its
 * arrival/departure time is meaningless and must be dropped from the model).
 */
class FinalDestinationScheduleTest :
    BehaviorSpec({

        fun place(id: String, schedule: VisitScheduleDomain? = null) =
            StepDomain.Place(id = id, name = id, lat = 0.0, lng = 0.0, schedule = schedule)

        fun planWith(vararg steps: StepDomain) = TravelPlanDomain(
            days = listOf(
                TravelDayDomain(
                    id = "day",
                    date = LocalDate(2024, 1, 1),
                    steps = steps.toList(),
                ),
            ),
        )

        val schedule =
            VisitScheduleDomain(startTime = LocalTime(9, 0), endTime = LocalTime(10, 0))

        given("a day with two places where the last one has a schedule") {
            val plan = planWith(place("a", schedule), place("b", schedule))

            `when`("clearFinalDestinationSchedules is called") {
                val result = plan.clearFinalDestinationSchedules()
                val steps = result.days[0].steps

                then("the destination place schedule is removed") {
                    (steps[1] as StepDomain.Place).schedule shouldBe null
                }
                then("the non-destination place schedule is preserved") {
                    (steps[0] as StepDomain.Place).schedule shouldBe schedule
                }
            }
        }

        given("a day with a single scheduled place") {
            val plan = planWith(place("a", schedule))

            `when`("clearFinalDestinationSchedules is called") {
                val result = plan.clearFinalDestinationSchedules()

                then("a lone place is not a destination and keeps its schedule") {
                    (result.days[0].steps[0] as StepDomain.Place).schedule shouldBe schedule
                }
            }
        }

        given("a day where the last place has no schedule") {
            val plan = planWith(place("a", schedule), place("b"))

            `when`("clearFinalDestinationSchedules is called") {
                val result = plan.clearFinalDestinationSchedules()

                then("the state is left unchanged") {
                    result shouldBe plan
                }
            }
        }
    })
