package com.takaotech.ktravel.presentation.planning

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
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
                result.planHeader.period.start shouldBe startInstant.toEpochMilliseconds()
                result.planHeader.period.end shouldBe endInstant.toEpochMilliseconds()
            }

            then("it should create TravelDayUi objects for each date in the range") {
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

            then("it should create a single TravelDayUi") {
                result.days.size shouldBe 1
                result.days[0].date shouldBe LocalDate(2021, 1, 1)
            }
        }

        `when`("setPeriod is called on a state with existing TravelDayUi objects") {
            val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
            val endInstant = startInstant + 2.days // 2021-01-03
            val stateWithDays = initialState.setPeriod(startInstant, endInstant).let { state ->
                val dayIndex = state.days.indexOfFirst { it.date == LocalDate(2021, 1, 2) }
                val day = state.days[dayIndex]
                val updatedDay = day.copy(
                    steps = persistentListOf(
                        StepUi.Place(name = "Existing Location", lat = 0.0, lng = 0.0)
                    )
                )
                state.copy(days = state.days.set(dayIndex, updatedDay))
            }

            and("the new period overlaps with existing days") {
                val newStartInstant = startInstant + 1.days // 2021-01-02
                val newEndInstant = startInstant + 3.days // 2021-01-04
                val result = stateWithDays.setPeriod(newStartInstant, newEndInstant)

                then("it should preserve existing TravelDayUi objects with their steps") {
                    result.days.size shouldBe 3
                    result.days[0].date shouldBe LocalDate(2021, 1, 2)
                    result.days[0].steps.size shouldBe 1
                    (result.days[0].steps[0] as StepUi.Place).name shouldBe "Existing Location"
                }

                then("it should create new TravelDayUi objects for new dates") {
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
            val stateWithSteps = initialState.setPeriod(startInstant, endInstant).let { state ->
                val dayIndex2 = state.days.indexOfFirst { it.date == LocalDate(2021, 1, 2) }
                val day2 = state.days[dayIndex2]
                val updatedDay2 = day2.copy(
                    steps = persistentListOf(
                        StepUi.Place(name = "Location Day 2", lat = 0.0, lng = 0.0)
                    )
                )
                val days1 = state.days.set(dayIndex2, updatedDay2)

                val dayIndex3 = days1.indexOfFirst { it.date == LocalDate(2021, 1, 3) }
                val day3 = days1[dayIndex3]
                val updatedDay3 = day3.copy(
                    steps = persistentListOf(
                        StepUi.Place(name = "Location Day 3", lat = 0.0, lng = 0.0)
                    )
                )
                state.copy(days = days1.set(dayIndex3, updatedDay3))
            }

            and("the new period includes those 2 dates") {
                val newStartInstant = startInstant + 1.days // 2021-01-02
                val newEndInstant = startInstant + 4.days // 2021-01-05
                val result = stateWithSteps.setPeriod(newStartInstant, newEndInstant)

                then("it should preserve steps in both overlapping dates") {
                    result.days.size shouldBe 4
                    result.days[0].date shouldBe LocalDate(2021, 1, 2)
                    result.days[0].steps.size shouldBe 1
                    (result.days[0].steps[0] as StepUi.Place).name shouldBe "Location Day 2"
                    result.days[1].date shouldBe LocalDate(2021, 1, 3)
                    result.days[1].steps.size shouldBe 1
                    (result.days[1].steps[0] as StepUi.Place).name shouldBe "Location Day 3"
                }

                then("it should create new TravelDayUi objects for new dates without steps") {
                    result.days[2].date shouldBe LocalDate(2021, 1, 4)
                    result.days[2].steps.size shouldBe 0
                    result.days[3].date shouldBe LocalDate(2021, 1, 5)
                    result.days[3].steps.size shouldBe 0
                }
            }
        }
    }
})
