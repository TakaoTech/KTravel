package com.takaotech.ktravel.presentation.planner

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class PlanningUiStateTest : BehaviorSpec({

    given("a PlanningUiState") {
        val initialState = PlanningUiState()

        `when`("setPeriod is called with a date range") {
            val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
            val endInstant = startInstant + 2.days // 2021-01-03
            val result = initialState.setPeriod(startInstant, endInstant)

            then("it should update the period in planHeader") {
                result.planHeader.period.start shouldBe startInstant
                result.planHeader.period.end shouldBe endInstant
            }

            then("it should create TravelDay objects for each date in the range") {
                result.days.size shouldBe 3
                result.days[0].date shouldBe LocalDate(2021, 1, 1)
                result.days[1].date shouldBe LocalDate(2021, 1, 2)
                result.days[2].date shouldBe LocalDate(2021, 1, 3)
            }
        }

        `when`("setPeriod is called with a single day range") {
            val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
            val endInstant = startInstant // Same day
            val result = initialState.setPeriod(startInstant, endInstant)

            then("it should create a single TravelDay") {
                result.days.size shouldBe 1
                result.days[0].date shouldBe LocalDate(2021, 1, 1)
            }
        }

        `when`("setPeriod is called on a state with existing TravelDay objects") {
            val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
            val endInstant = startInstant + 2.days // 2021-01-03
            val stateWithDays = initialState.setPeriod(startInstant, endInstant)
                .addTravelStep(LocalDate(2021, 1, 2), "Existing Location")

            and("the new period overlaps with existing days") {
                val newStartInstant = startInstant + 1.days // 2021-01-02
                val newEndInstant = startInstant + 3.days // 2021-01-04
                val result = stateWithDays.setPeriod(newStartInstant, newEndInstant)

                then("it should preserve existing TravelDay objects with their steps") {
                    result.days.size shouldBe 3
                    result.days[0].date shouldBe LocalDate(2021, 1, 2)
                    result.days[0].steps.size shouldBe 1
                    (result.days[0].steps[0] as TravelDay.Step.Place).location shouldBe "Existing Location"
                }

                then("it should create new TravelDay objects for new dates") {
                    result.days[1].date shouldBe LocalDate(2021, 1, 3)
                    result.days[1].steps.size shouldBe 0
                    result.days[2].date shouldBe LocalDate(2021, 1, 4)
                    result.days[2].steps.size shouldBe 0
                }
            }
        }

        `when`("setPeriod is called and 2 dates from old range have steps") {
            val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
            val endInstant = startInstant + 3.days // 2021-01-04
            val stateWithSteps = initialState.setPeriod(startInstant, endInstant)
                .addTravelStep(LocalDate(2021, 1, 2), "Location Day 2")
                .addTravelStep(LocalDate(2021, 1, 3), "Location Day 3")

            and("the new period includes those 2 dates") {
                val newStartInstant = startInstant + 1.days // 2021-01-02
                val newEndInstant = startInstant + 4.days // 2021-01-05
                val result = stateWithSteps.setPeriod(newStartInstant, newEndInstant)

                then("it should preserve steps in both overlapping dates") {
                    result.days.size shouldBe 4
                    result.days[0].date shouldBe LocalDate(2021, 1, 2)
                    result.days[0].steps.size shouldBe 1
                    (result.days[0].steps[0] as TravelDay.Step.Place).location shouldBe "Location Day 2"
                    result.days[1].date shouldBe LocalDate(2021, 1, 3)
                    result.days[1].steps.size shouldBe 1
                    (result.days[1].steps[0] as TravelDay.Step.Place).location shouldBe "Location Day 3"
                }

                then("it should create new TravelDay objects for new dates without steps") {
                    result.days[2].date shouldBe LocalDate(2021, 1, 4)
                    result.days[2].steps.size shouldBe 0
                    result.days[3].date shouldBe LocalDate(2021, 1, 5)
                    result.days[3].steps.size shouldBe 0
                }
            }
        }
    }

    given("a PlanningUiState with days") {
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03
        val stateWithDays = PlanningUiState().setPeriod(startInstant, endInstant)

        `when`("addTravelStep is called with a valid day and location") {
            val result = stateWithDays.addTravelStep(LocalDate(2021, 1, 2), "Rome")

            then("it should add a step to the specified day") {
                result.days.size shouldBe 3
                result.days[1].date shouldBe LocalDate(2021, 1, 2)
                result.days[1].steps.size shouldBe 1
                (result.days[1].steps[0] as TravelDay.Step.Place).location shouldBe "Rome"
            }

            then("it should generate a unique ID for the step") {
                result.days[1].steps[0].id.isNotEmpty() shouldBe true
            }

            then("it should not affect other days") {
                result.days[0].steps.size shouldBe 0
                result.days[2].steps.size shouldBe 0
            }
        }

        `when`("addTravelStep is called multiple times on the same day") {
            val result = stateWithDays
                .addTravelStep(LocalDate(2021, 1, 2), "Rome")
                .addTravelStep(LocalDate(2021, 1, 2), "Vatican City")
                .addTravelStep(LocalDate(2021, 1, 2), "Colosseum")

            then("it should add all steps to the specified day") {
                result.days[1].steps.size shouldBe 3
                (result.days[1].steps[0] as TravelDay.Step.Place).location shouldBe "Rome"
                (result.days[1].steps[1] as TravelDay.Step.Place).location shouldBe "Vatican City"
                (result.days[1].steps[2] as TravelDay.Step.Place).location shouldBe "Colosseum"
            }

            then("each step should have a unique ID") {
                val ids = result.days[1].steps.map { it.id }
                ids.size shouldBe ids.toSet().size
            }
        }

        `when`("addTravelStep is called on different days") {
            val result = stateWithDays
                .addTravelStep(LocalDate(2021, 1, 1), "Paris")
                .addTravelStep(LocalDate(2021, 1, 2), "Rome")
                .addTravelStep(LocalDate(2021, 1, 3), "Milan")

            then("it should add steps to each specified day") {
                result.days[0].steps.size shouldBe 1
                (result.days[0].steps[0] as TravelDay.Step.Place).location shouldBe "Paris"
                result.days[1].steps.size shouldBe 1
                (result.days[1].steps[0] as TravelDay.Step.Place).location shouldBe "Rome"
                result.days[2].steps.size shouldBe 1
                (result.days[2].steps[0] as TravelDay.Step.Place).location shouldBe "Milan"
            }
        }
    }
})