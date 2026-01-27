package com.takaotech.ktravel.domain.repository

import com.takaotech.ktravel.presentation.planner.Place
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
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

    given("a repository with places in the general list") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = Place(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
        val place2 = Place(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)

        repository.savePlace(place1)
        repository.savePlace(place2)

        `when`("movePlaceToDay is called with valid placeId and dayId") {
            val state = repository.planningState.value
            val dayId = state.days[1].id

            repository.movePlaceToDay("place1", dayId)

            then("should remove the place from the general list") {
                val updatedState = repository.planningState.value
                updatedState.places shouldHaveSize 1
                updatedState.places shouldNotContain place1
                updatedState.places shouldContain place2
            }

            then("should add the place to the specified day") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.places shouldHaveSize 1
                day.places shouldContain place1
            }

            then("should not modify other days") {
                val updatedState = repository.planningState.value
                updatedState.days[0].places.shouldBeEmpty()
                updatedState.days[2].places.shouldBeEmpty()
            }
        }

        `when`("movePlaceToDay is called with an invalid placeId") {
            val initialState = repository.planningState.value
            val dayId = initialState.days[1].id

            repository.movePlaceToDay("invalid-place-id", dayId)

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }

        `when`("movePlaceToDay is called with an invalid dayId") {
            val initialState = repository.planningState.value

            repository.movePlaceToDay("place1", "invalid-day-id")

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }
    }

    given("a repository with multiple places to move to the same day") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = Place(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
        val place2 = Place(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)
        val place3 = Place(id = "place3", name = "Pantheon", lat = 41.8986, lng = 12.4769)

        repository.savePlace(place1)
        repository.savePlace(place2)
        repository.savePlace(place3)

        `when`("multiple places are moved to the same day") {
            val state = repository.planningState.value
            val dayId = state.days[1].id

            repository.movePlaceToDay("place1", dayId)
            repository.movePlaceToDay("place2", dayId)

            then("should add all places to the day in order") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.places shouldHaveSize 2
                day.places[0] shouldBe place1
                day.places[1] shouldBe place2
            }

            then("should keep remaining places in the general list") {
                val updatedState = repository.planningState.value
                updatedState.places shouldHaveSize 1
                updatedState.places shouldContain place3
            }
        }
    }

    given("a repository with multiple places to move to different days") {
        val repository = TravelPlanRepositoryImpl()
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = Place(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
        val place2 = Place(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)
        val place3 = Place(id = "place3", name = "Pantheon", lat = 41.8986, lng = 12.4769)

        repository.savePlace(place1)
        repository.savePlace(place2)
        repository.savePlace(place3)

        `when`("places are moved to different days") {
            val state = repository.planningState.value
            val day1Id = state.days[0].id
            val day2Id = state.days[1].id

            repository.movePlaceToDay("place1", day1Id)
            repository.movePlaceToDay("place2", day2Id)

            then("should distribute places correctly across days") {
                val updatedState = repository.planningState.value
                updatedState.days[0].places shouldHaveSize 1
                updatedState.days[0].places shouldContain place1
                updatedState.days[1].places shouldHaveSize 1
                updatedState.days[1].places shouldContain place2
                updatedState.days[2].places.shouldBeEmpty()
            }

            then("should keep remaining places in the general list") {
                val updatedState = repository.planningState.value
                updatedState.places shouldHaveSize 1
                updatedState.places shouldContain place3
            }
        }
    }
})
