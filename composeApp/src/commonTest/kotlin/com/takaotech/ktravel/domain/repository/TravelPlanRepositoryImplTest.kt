package com.takaotech.ktravel.domain.repository

import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSource
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.repository.TravelPlanRepositoryImpl
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.PlanningScopeData
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TravelDayDomain
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val TEST_PLAN_ID = "test-plan-id"

private fun mockDataSource(): TravelPlanStorageDataSource = mock(MockMode.autoUnit) {
    every { getTravelPlan(any()) } returns TravelPlanEntity(
        id = TEST_PLAN_ID,
        name = "",
        periodStart = LocalDate.fromEpochDays(0),
        periodEnd = LocalDate.fromEpochDays(0),
        days = emptyList(),
        places = emptyList()
    )
}

private fun mockScopeData(): PlanningScopeData = PlanningScopeData().apply {
    travelId = TEST_PLAN_ID
}

@OptIn(ExperimentalTime::class)
class TravelPlanRepositoryImplTest : BehaviorSpec({

    given("a newly created TravelPlanRepositoryImpl") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())

        then("should initialize planningState with a default period") {
            val state = repository.planningState.value
            state shouldNotBe null
            state.periodStart shouldNotBe null
            state.periodEnd shouldNotBe null
        }

        then("should have empty days when initialized from empty entity") {
            val state = repository.planningState.value
            state.days.size shouldBe 0
        }
    }

    given("a repository with a set period") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        `when`("updatePeriod is called") {
            repository.updatePeriod(
                startMillis = startInstant.toEpochMilliseconds(),
                endMillis = endInstant.toEpochMilliseconds()
            )

            then("should update the period in planningState") {
                val state = repository.planningState.value
                state.periodStart shouldBe LocalDate(2021, 1, 1)
                state.periodEnd shouldBe LocalDate(2021, 1, 3)
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
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
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

            then("should return TravelDayDomain.EMPTY") {
                val day = dayFlow.first()
                day shouldBe TravelDayDomain.EMPTY
            }
        }
    }

    given("a repository with places in the general list") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
        val place2 =
            PlaceDomain(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)

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
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
        val place2 =
            PlaceDomain(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)
        val place3 = PlaceDomain(id = "place3", name = "Pantheon", lat = 41.8986, lng = 12.4769)

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
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
        val place2 =
            PlaceDomain(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)
        val place3 = PlaceDomain(id = "place3", name = "Pantheon", lat = 41.8986, lng = 12.4769)

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

    given("a repository for savePlace with dayId parameter") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        `when`("savePlace is called with null dayId") {
            val place = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)

            repository.savePlace(place, null)

            then("should add the place to the general list") {
                val state = repository.planningState.value
                state.places shouldHaveSize 1
                state.places shouldContain place
            }

            then("should not add the place to any day") {
                val state = repository.planningState.value
                state.days.forEach { day ->
                    day.places.shouldBeEmpty()
                }
            }
        }
    }

    given("a repository for savePlace with valid dayId") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        `when`("savePlace is called with a valid dayId") {
            val state = repository.planningState.value
            val dayId = state.days[1].id
            val place = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)

            repository.savePlace(place, dayId)

            then("should add the place directly to the specified day") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.places shouldHaveSize 1
                day.places shouldContain place
            }

            then("should not add the place to the general list") {
                val updatedState = repository.planningState.value
                updatedState.places.shouldBeEmpty()
            }

            then("should not modify other days") {
                val updatedState = repository.planningState.value
                updatedState.days[0].places.shouldBeEmpty()
                updatedState.days[2].places.shouldBeEmpty()
            }
        }
    }

    given("a repository for savePlace with invalid dayId") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        `when`("savePlace is called with a non-existing dayId") {
            val place = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)

            repository.savePlace(place, "non-existing-day-id")

            then("should not add the place to the general list") {
                val state = repository.planningState.value
                state.places.shouldBeEmpty()
            }

            then("should not add the place to any day") {
                val state = repository.planningState.value
                state.days.forEach { day ->
                    day.places.shouldBeEmpty()
                }
            }
        }
    }

    given("a repository for savePlace with multiple places to the same day") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        `when`("multiple places are saved directly to the same day") {
            val state = repository.planningState.value
            val dayId = state.days[1].id
            val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
            val place2 =
                PlaceDomain(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)

            repository.savePlace(place1, dayId)
            repository.savePlace(place2, dayId)

            then("should add all places to the day in order") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.places shouldHaveSize 2
                day.places[0] shouldBe place1
                day.places[1] shouldBe place2
            }

            then("should not add any place to the general list") {
                val updatedState = repository.planningState.value
                updatedState.places.shouldBeEmpty()
            }
        }
    }

    given("a repository for savePlace mixing general list and day-specific places") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        `when`("places are saved both to general list and to specific days") {
            val state = repository.planningState.value
            val dayId = state.days[1].id
            val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
            val place2 =
                PlaceDomain(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)
            val place3 = PlaceDomain(id = "place3", name = "Pantheon", lat = 41.8986, lng = 12.4769)

            repository.savePlace(place1, null)
            repository.savePlace(place2, dayId)
            repository.savePlace(place3, null)

            then("should add places to the general list when dayId is null") {
                val updatedState = repository.planningState.value
                updatedState.places shouldHaveSize 2
                updatedState.places shouldContain place1
                updatedState.places shouldContain place3
            }

            then("should add places to the specified day when dayId is provided") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.places shouldHaveSize 1
                day.places shouldContain place2
            }
        }
    }

    given("a repository with places in a TravelDay for movePlaceToGeneral") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
        val place2 =
            PlaceDomain(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)

        val state = repository.planningState.value
        val dayId = state.days[1].id

        repository.savePlace(place1, dayId)
        repository.savePlace(place2, dayId)

        `when`("movePlaceToGeneral is called with valid placeId and dayId") {
            repository.movePlaceToGeneral("place1", dayId)

            then("should remove the place from the day") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.places shouldHaveSize 1
                day.places shouldNotContain place1
                day.places shouldContain place2
            }

            then("should add the place to the general list") {
                val updatedState = repository.planningState.value
                updatedState.places shouldHaveSize 1
                updatedState.places shouldContain place1
            }

            then("should not modify other days") {
                val updatedState = repository.planningState.value
                updatedState.days[0].places.shouldBeEmpty()
                updatedState.days[2].places.shouldBeEmpty()
            }
        }
    }

    given("a repository for movePlaceToGeneral with invalid placeId") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)

        val state = repository.planningState.value
        val dayId = state.days[1].id

        repository.savePlace(place1, dayId)

        `when`("movePlaceToGeneral is called with an invalid placeId") {
            val initialState = repository.planningState.value

            repository.movePlaceToGeneral("invalid-place-id", dayId)

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }
    }

    given("a repository for movePlaceToGeneral with invalid dayId") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)

        val state = repository.planningState.value
        val dayId = state.days[1].id

        repository.savePlace(place1, dayId)

        `when`("movePlaceToGeneral is called with an invalid dayId") {
            val initialState = repository.planningState.value

            repository.movePlaceToGeneral("place1", "invalid-day-id")

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }
    }

    given("a repository for deletePlace from general list") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
        val place2 =
            PlaceDomain(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)

        repository.savePlace(place1, null)
        repository.savePlace(place2, null)

        `when`("deletePlace is called with null dayId") {
            repository.deletePlace("place1", null)

            then("should remove the place from the general list") {
                val updatedState = repository.planningState.value
                updatedState.places shouldHaveSize 1
                updatedState.places shouldNotContain place1
                updatedState.places shouldContain place2
            }

            then("should not modify any day") {
                val updatedState = repository.planningState.value
                updatedState.days.forEach { day ->
                    day.places.shouldBeEmpty()
                }
            }
        }
    }

    given("a repository for deletePlace from a TravelDay") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
        val place2 =
            PlaceDomain(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)

        val state = repository.planningState.value
        val dayId = state.days[1].id

        repository.savePlace(place1, dayId)
        repository.savePlace(place2, dayId)

        `when`("deletePlace is called with a valid dayId") {
            repository.deletePlace("place1", dayId)

            then("should remove the place from the specified day") {
                val updatedState = repository.planningState.value
                val day = updatedState.days[1]
                day.places shouldHaveSize 1
                day.places shouldNotContain place1
                day.places shouldContain place2
            }

            then("should not add the place to the general list") {
                val updatedState = repository.planningState.value
                updatedState.places.shouldBeEmpty()
            }

            then("should not modify other days") {
                val updatedState = repository.planningState.value
                updatedState.days[0].places.shouldBeEmpty()
                updatedState.days[2].places.shouldBeEmpty()
            }
        }
    }

    given("a repository for deletePlace with invalid placeId from general list") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)

        repository.savePlace(place1, null)

        `when`("deletePlace is called with an invalid placeId and null dayId") {
            val initialState = repository.planningState.value

            repository.deletePlace("invalid-place-id", null)

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }
    }

    given("a repository for deletePlace with invalid placeId from a TravelDay") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)

        val state = repository.planningState.value
        val dayId = state.days[1].id

        repository.savePlace(place1, dayId)

        `when`("deletePlace is called with an invalid placeId and valid dayId") {
            val initialState = repository.planningState.value

            repository.deletePlace("invalid-place-id", dayId)

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }
    }

    given("a repository for deletePlace with invalid dayId") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)

        val state = repository.planningState.value
        val dayId = state.days[1].id

        repository.savePlace(place1, dayId)

        `when`("deletePlace is called with an invalid dayId") {
            val initialState = repository.planningState.value

            repository.deletePlace("place1", "invalid-day-id")

            then("should not modify the state") {
                val updatedState = repository.planningState.value
                updatedState shouldBe initialState
            }
        }
    }

    given("a repository for updatePlanName") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())

        `when`("updatePlanName is called with a new name") {
            val newName = "Viaggio a Roma"

            repository.updatePlanName(newName)

            then("should update the plan name in planningState") {
                val updatedState = repository.planningState.value
                updatedState.name shouldBe newName
            }
        }
    }

    given("a repository for updatePlanName with empty name") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())

        `when`("updatePlanName is called with an empty name") {
            val emptyName = ""

            repository.updatePlanName(emptyName)

            then("should update the plan name to empty") {
                val updatedState = repository.planningState.value
                updatedState.name shouldBe emptyName
            }
        }
    }

    given("a repository for updatePlanName called multiple times") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())

        `when`("updatePlanName is called multiple times") {
            val firstName = "Primo Nome"
            val secondName = "Secondo Nome"

            repository.updatePlanName(firstName)
            repository.updatePlanName(secondName)

            then("should keep only the last name") {
                val updatedState = repository.planningState.value
                updatedState.name shouldBe secondName
            }
        }
    }

    given("a repository for updatePlanName preserving other state") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
        repository.savePlace(place, null)

        `when`("updatePlanName is called") {
            val initialState = repository.planningState.value
            val newName = "Viaggio a Roma"

            repository.updatePlanName(newName)

            then("should update only the plan name") {
                val updatedState = repository.planningState.value
                updatedState.name shouldBe newName
            }

            then("should preserve the period") {
                val updatedState = repository.planningState.value
                updatedState.periodStart shouldBe initialState.periodStart
                updatedState.periodEnd shouldBe initialState.periodEnd
            }

            then("should preserve the days") {
                val updatedState = repository.planningState.value
                updatedState.days shouldHaveSize initialState.days.size
            }

            then("should preserve the places in general list") {
                val updatedState = repository.planningState.value
                updatedState.places shouldHaveSize 1
                updatedState.places shouldContain place
            }
        }
    }
    given("a repository with a place in a TravelDay to move to steps") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)

        val state = repository.planningState.value
        val dayId = state.days[1].id

        repository.savePlace(place1, dayId)

        `when`("movePlaceToStep is called with valid placeId and dayId") {
            repository.movePlaceToStep("place1", dayId)

            then("should remove the place from places and add a Step.Place to steps with same id and name") {
                val updated = repository.planningState.value
                val day = updated.days[1]
                day.places.shouldBeEmpty()
                day.steps shouldHaveSize 1
                val step = day.steps[0] as StepDomain.Place
                step.id shouldBe "place1"
                step.location shouldBe place1.name
            }

            then("should not affect other days or general places list") {
                val updated = repository.planningState.value
                updated.days[0].places.shouldBeEmpty()
                updated.days[0].steps.shouldBeEmpty()
                updated.days[2].places.shouldBeEmpty()
                updated.days[2].steps.shouldBeEmpty()
                updated.places.shouldBeEmpty()
            }
        }

        `when`("movePlaceToStep is called with an invalid placeId") {
            val snapshot = repository.planningState.value
            repository.movePlaceToStep("invalid-place-id", dayId)

            then("should not modify the state") {
                repository.planningState.value shouldBe snapshot
            }
        }

        `when`("movePlaceToStep is called with an invalid dayId") {
            val snapshot = repository.planningState.value
            repository.movePlaceToStep("place1", "invalid-day-id")

            then("should not modify the state") {
                repository.planningState.value shouldBe snapshot
            }
        }
    }

    given("a repository with a Step.Place in a TravelDay to move back to places") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val place1 = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)

        val state = repository.planningState.value
        val dayId = state.days[1].id

        // Crea uno Step.Place partendo da un Place
        repository.savePlace(place1, dayId)
        repository.movePlaceToStep("place1", dayId)

        `when`("moveStepToPlace is called with valid stepId and dayId") {
            // Lo step creato ha lo stesso id del place
            repository.moveStepToPlace("place1", dayId)

            then("should remove the step from steps and add a Place to places with same id and name") {
                val updated = repository.planningState.value
                val day = updated.days[1]
                day.steps.shouldBeEmpty()
                day.places shouldHaveSize 1
                val backPlace = day.places[0]
                backPlace.id shouldBe "place1"
                backPlace.name shouldBe place1.name
                backPlace.lat shouldBe place1.lat
                backPlace.lng shouldBe place1.lng
            }

            then("should not affect other days or general places list") {
                val updated = repository.planningState.value
                updated.days[0].places.shouldBeEmpty()
                updated.days[0].steps.shouldBeEmpty()
                updated.days[2].places.shouldBeEmpty()
                updated.days[2].steps.shouldBeEmpty()
                updated.places.shouldBeEmpty()
            }
        }

        `when`("moveStepToPlace is called with an invalid stepId") {
            val snapshot = repository.planningState.value
            repository.moveStepToPlace("invalid-step-id", dayId)

            then("should not modify the state") {
                repository.planningState.value shouldBe snapshot
            }
        }

        `when`("moveStepToPlace is called with an invalid dayId") {
            val snapshot = repository.planningState.value
            repository.moveStepToPlace("place1", "invalid-day-id")

            then("should not modify the state") {
                repository.planningState.value shouldBe snapshot
            }
        }
    }

    given("a repository with steps in a TravelDay to move up and down") {
        val repository = TravelPlanRepositoryImpl(mockScopeData(), mockDataSource())
        val startInstant = Instant.fromEpochMilliseconds(1609459200000) // 2021-01-01
        val endInstant = startInstant + 2.days // 2021-01-03

        repository.updatePeriod(
            startMillis = startInstant.toEpochMilliseconds(),
            endMillis = endInstant.toEpochMilliseconds()
        )

        val state = repository.planningState.value
        val dayId = state.days[1].id

        val place1 = PlaceDomain(id = "p1", name = "P1", lat = 0.0, lng = 0.0)
        val place2 = PlaceDomain(id = "p2", name = "P2", lat = 0.0, lng = 0.0)
        val place3 = PlaceDomain(id = "p3", name = "P3", lat = 0.0, lng = 0.0)

        repository.savePlace(place1, dayId)
        repository.savePlace(place2, dayId)
        repository.savePlace(place3, dayId)

        repository.movePlaceToStep("p1", dayId)
        repository.movePlaceToStep("p2", dayId)
        repository.movePlaceToStep("p3", dayId)

        // Initial order: P1, P2, P3

        `when`("moveStepUp is called on the second step") {
            repository.moveTravelStepUp("p2", dayId)

            then("should swap it with the first step") {
                val day = repository.planningState.value.days[1]
                day.steps[0].id shouldBe "p2"
                day.steps[1].id shouldBe "p1"
                day.steps[2].id shouldBe "p3"
            }
        }

        `when`("moveStepUp is called on the first step") {
            val snapshot = repository.planningState.value
            repository.moveTravelStepUp("p2", dayId) // p2 is now at index 0

            then("should not change the state") {
                repository.planningState.value shouldBe snapshot
            }
        }

        `when`("moveStepDown is called on the second step") {
            // Current order: P2, P1, P3. P1 is at index 1
            repository.moveTravelStepDown("p1", dayId)

            then("should swap it with the third step") {
                val day = repository.planningState.value.days[1]
                day.steps[0].id shouldBe "p2"
                day.steps[1].id shouldBe "p3"
                day.steps[2].id shouldBe "p1"
            }
        }

        `when`("moveStepDown is called on the last step") {
            val snapshot = repository.planningState.value
            repository.moveTravelStepDown("p1", dayId) // p1 is at index 2

            then("should not change the state") {
                repository.planningState.value shouldBe snapshot
            }
        }
    }

})
