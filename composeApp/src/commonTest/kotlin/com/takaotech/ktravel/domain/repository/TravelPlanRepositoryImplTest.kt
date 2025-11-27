package com.takaotech.ktravel.domain.repository

import com.takaotech.ktravel.presentation.planner.TravelDay
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TravelPlanRepositoryImplTest : BehaviorSpec({

    given("a newly created TravelPlanRepositoryImpl") {
        val repository = TravelPlanRepositoryImpl()

        then("should initialize planningState with a default period") {
            val state = repository.planningState.value
            state shouldNotBe null
            state.planHeader.period.start shouldNotBe null
            state.planHeader.period.end shouldNotBe null
        }

        then("should have at least one day in the default period") {
            val state = repository.planningState.value
            state.days.size shouldBe 1
        }
    }

    given("a repository with a set period") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        `when`("updatePeriod is called") {
            repository.updatePeriod(
                startMillis = startInstant.toEpochMilliseconds(),
                endMillis = endInstant.toEpochMilliseconds()
            )

            then("should update the period in planningState") {
                val state = repository.planningState.value
                state.planHeader.period.start shouldBe startInstant
                state.planHeader.period.end shouldBe endInstant
            }

            then("should create days for the specified period") {
                val state = repository.planningState.value
                state.days shouldHaveSize 3
                state.days[0].date shouldBe LocalDate(2021, 1, 1)
                state.days[1].date shouldBe LocalDate(2021, 1, 2)
                state.days[2].date shouldBe LocalDate(2021, 1, 3)
            }
        }
    }

    given("a repository with existing days") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        `when`("getTravelDayFlow is called with an existing dayId") {
            val state = repository.planningState.value
            val existingDayId = state.days[1].id
            val dayFlow = repository.getTravelDayFlow(existingDayId)

            then("should return the correct TravelDay") {
                val day = dayFlow.first()
                day.shouldNotBeNull()
                day.id shouldBe existingDayId
                day.date shouldBe LocalDate(2021, 1, 2)
            }
        }

        `when`("getTravelDayFlow is called with a non-existing dayId") {
            val dayFlow = repository.getTravelDayFlow("non-existing-id")

            then("should return null") {
                val day = dayFlow.first()
                day.shouldBeNull()
            }
        }
    }

    given("a repository with a specific day to add a single step") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        `when`("addStepToDay is called with a valid dayId") {
            val state = repository.planningState.value
            val dayId = state.days[1].id
            val step = TravelDay.Step.Place(location = "Roma")

            repository.addStepToDay(dayId, step)

            then("should add the step to the specified day") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.steps shouldHaveSize 1
                (day.steps[0] as TravelDay.Step.Place).location shouldBe "Roma"
            }

            then("should not modify other days") {
                val updatedState = repository.planningState.value
                updatedState.days[0].steps.shouldBeEmpty()
                updatedState.days[2].steps.shouldBeEmpty()
            }
        }
    }

    given("a repository with a specific day to test invalid dayId") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        `when`("addStepToDay is called with an invalid dayId") {
            val initialState = repository.planningState.value
            val step = TravelDay.Step.Place(location = "Milano")

            repository.addStepToDay("invalid-day-id", step)

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }
    }

    given("a repository with a specific day to add multiple steps") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        `when`("addStepToDay is called multiple times on the same day") {
            val state = repository.planningState.value
            val dayId = state.days[1].id
            val step1 = TravelDay.Step.Place(location = "Roma")
            val step2 = TravelDay.Step.Place(location = "Vaticano")
            val step3 = TravelDay.Step.Place(location = "Colosseo")

            repository.addStepToDay(dayId, step1)
            repository.addStepToDay(dayId, step2)
            repository.addStepToDay(dayId, step3)

            then("should add all steps in order") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.steps shouldHaveSize 3
                (day.steps[0] as TravelDay.Step.Place).location shouldBe "Roma"
                (day.steps[1] as TravelDay.Step.Place).location shouldBe "Vaticano"
                (day.steps[2] as TravelDay.Step.Place).location shouldBe "Colosseo"
            }
        }
    }

    given("a repository with a day containing steps") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val state = repository.planningState.value
        val dayId = state.days[1].id
        val step1 = TravelDay.Step.Place(id = "step1", location = "Roma")
        val step2 = TravelDay.Step.Place(id = "step2", location = "Milano")

        repository.addStepToDay(dayId, step1)
        repository.addStepToDay(dayId, step2)

        `when`("removeStepFromDay is called with a valid stepId") {
            repository.removeStepFromDay(dayId, "step1")

            then("should remove the specified step") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.steps shouldHaveSize 1
                (day.steps[0] as TravelDay.Step.Place).location shouldBe "Milano"
            }
        }

        `when`("removeStepFromDay is called with an invalid dayId") {
            val initialState = repository.planningState.value

            repository.removeStepFromDay("invalid-day-id", "step1")

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }
    }

    given("a repository with a step followed by transport") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val state = repository.planningState.value
        val dayId = state.days[1].id
        val place = TravelDay.Step.Place(id = "place1", location = "Roma")
        val transport = TravelDay.Step.Transport(id = "transport1", type = TravelDay.Step.Transport.Type.TRAIN)
        val place2 = TravelDay.Step.Place(id = "place2", location = "Milano")

        repository.addStepToDay(dayId, place)
        repository.addStepToDay(dayId, transport)
        repository.addStepToDay(dayId, place2)

        `when`("removeStepFromDay is called on a step followed by transport") {
            repository.removeStepFromDay(dayId, "place1")

            then("should remove both the step and the following transport") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.steps shouldHaveSize 1
                (day.steps[0] as TravelDay.Step.Place).location shouldBe "Milano"
            }
        }
    }

    given("a repository with steps to update") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val state = repository.planningState.value
        val dayId = state.days[1].id
        val step1 = TravelDay.Step.Place(id = "step1", location = "Roma")
        val step2 = TravelDay.Step.Place(id = "step2", location = "Milano")

        repository.addStepToDay(dayId, step1)
        repository.addStepToDay(dayId, step2)

        `when`("updateStep is called with valid parameters") {
            val updatedStep = TravelDay.Step.Place(id = "step1", location = "Firenze")
            repository.updateStep(dayId, "step1", updatedStep)

            then("should update the specified step") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.steps shouldHaveSize 2
                (day.steps[0] as TravelDay.Step.Place).location shouldBe "Firenze"
                (day.steps[1] as TravelDay.Step.Place).location shouldBe "Milano"
            }
        }

        `when`("updateStep is called with an invalid dayId") {
            val initialState = repository.planningState.value
            val updatedStep = TravelDay.Step.Place(id = "step1", location = "Firenze")

            repository.updateStep("invalid-day-id", "step1", updatedStep)

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }

        `when`("updateStep is called with an invalid stepId") {
            val initialState = repository.planningState.value
            val updatedStep = TravelDay.Step.Place(id = "invalid-step", location = "Firenze")

            repository.updateStep(dayId, "invalid-step-id", updatedStep)

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }
    }

    given("a repository to test reactive flow") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        `when`("observing a day and adding a step") {
            val state = repository.planningState.value
            val dayId = state.days[1].id
            val dayFlow = repository.getTravelDayFlow(dayId)

            val step = TravelDay.Step.Place(location = "Roma")
            repository.addStepToDay(dayId, step)

            then("the flow should emit the updated day") {
                val updatedDay = dayFlow.first()
                updatedDay.shouldNotBeNull()
                updatedDay.steps shouldHaveSize 1
                (updatedDay.steps[0] as TravelDay.Step.Place).location shouldBe "Roma"
            }
        }
    }
})
