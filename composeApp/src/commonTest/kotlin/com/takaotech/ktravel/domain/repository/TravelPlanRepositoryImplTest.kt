package com.takaotech.ktravel.domain.repository

import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSource
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.repository.TravelPlanRepositoryImpl
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.routing.model.Route
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
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

private const val TEST_PLAN_ID = "test-plan-id"
private const val START_MILLIS = 1609459200000L  // 2021-01-01T00:00:00Z
private const val END_MILLIS = 1609632000000L    // 2021-01-03T00:00:00Z

private val COLOSSEO = PlaceDomain(id = "place1", name = "Colosseo", lat = 41.8902, lng = 12.4922)
private val TREVI =
    PlaceDomain(id = "place2", name = "Fontana di Trevi", lat = 41.9009, lng = 12.4833)
private val PANTHEON = PlaceDomain(id = "place3", name = "Pantheon", lat = 41.8986, lng = 12.4769)

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

private data class Ctx(
    val repo: TravelPlanRepositoryImpl,
    val ds: TravelPlanStorageDataSource,
    val dayIds: List<String> = emptyList()
)

private fun freshCtx(): Ctx {
    val ds = mockDataSource()
    return Ctx(TravelPlanRepositoryImpl(TEST_PLAN_ID, ds), ds)
}

private suspend fun ctxWith3Days(): Ctx {
    val ds = mockDataSource()
    val repo = TravelPlanRepositoryImpl(TEST_PLAN_ID, ds)
    repo.updatePeriod(START_MILLIS, END_MILLIS)
    val dayIds = repo.planningState.value.days.map { it.id }
    return Ctx(repo, ds, dayIds)
}

class TravelPlanRepositoryImplTest : BehaviorSpec({

    given("repository initialization") {
        `when`("initialized from an empty entity") {
            val (repo) = freshCtx()

            then("planningState should not be null") {
                repo.planningState.value shouldNotBe null
            }

            then("period dates should be set to defaults") {
                repo.planningState.value.periodStart shouldNotBe null
                repo.planningState.value.periodEnd shouldNotBe null
            }

            then("days should be empty") {
                repo.planningState.value.days.shouldBeEmpty()
            }
        }
    }

    given("period management") {
        `when`("updatePeriod is called with a 3-day range") {
            val (repo, ds) = freshCtx()
            repo.updatePeriod(START_MILLIS, END_MILLIS)

            then("should set the correct period start date") {
                repo.planningState.value.periodStart shouldBe LocalDate(2021, 1, 1)
            }

            then("should set the correct period end date") {
                repo.planningState.value.periodEnd shouldBe LocalDate(2021, 1, 3)
            }

            then("should create one day per date in the range") {
                repo.planningState.value.days shouldHaveSize 3
            }

            then("days should have correct dates in order") {
                val days = repo.planningState.value.days
                days[0].date shouldBe LocalDate(2021, 1, 1)
                days[1].date shouldBe LocalDate(2021, 1, 2)
                days[2].date shouldBe LocalDate(2021, 1, 3)
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }
    }

    given("day flow") {
        `when`("getTravelDayFlow is called with an existing dayId") {
            val (repo, _, dayIds) = ctxWith3Days()
            val targetId = dayIds[1]
            val dayFlow = repo.getTravelDayFlow(targetId)

            then("should emit the correct day with the matching id and date") {
                val day = dayFlow.first()
                day.shouldNotBeNull()
                day.id shouldBe targetId
                day.date shouldBe LocalDate(2021, 1, 2)
            }
        }

        `when`("getTravelDayFlow is called with a non-existing dayId") {
            val (repo) = ctxWith3Days()
            val dayFlow = repo.getTravelDayFlow("non-existing-id")

            then("should emit TravelDayDomain.EMPTY") {
                dayFlow.first() shouldBe TravelDayDomain.EMPTY
            }
        }
    }

    given("plan name management") {
        `when`("updatePlanName is called with a new name") {
            val (repo, ds) = freshCtx()
            repo.updatePlanName("Viaggio a Roma")

            then("should update the plan name in planningState") {
                repo.planningState.value.name shouldBe "Viaggio a Roma"
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("updatePlanName is called with an empty name") {
            val (repo) = freshCtx()
            repo.updatePlanName("")

            then("should update the plan name to empty string") {
                repo.planningState.value.name shouldBe ""
            }
        }

        `when`("updatePlanName is called multiple times") {
            val (repo) = freshCtx()
            repo.updatePlanName("First Name")
            repo.updatePlanName("Second Name")

            then("should retain only the last name") {
                repo.planningState.value.name shouldBe "Second Name"
            }
        }

        `when`("updatePlanName is called with existing days and places") {
            val (repo) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)
            val stateBefore = repo.planningState.value
            repo.updatePlanName("Viaggio a Roma")

            then("should update only the plan name") {
                repo.planningState.value.name shouldBe "Viaggio a Roma"
            }

            then("should preserve the period start") {
                repo.planningState.value.periodStart shouldBe stateBefore.periodStart
            }

            then("should preserve the period end") {
                repo.planningState.value.periodEnd shouldBe stateBefore.periodEnd
            }

            then("should preserve the days count") {
                repo.planningState.value.days shouldHaveSize stateBefore.days.size
            }

            then("should preserve the places in the general list") {
                repo.planningState.value.places shouldHaveSize 1
                repo.planningState.value.places shouldContain COLOSSEO
            }
        }
    }

    given("savePlace") {
        `when`("savePlace with null dayId adds the place to the general list") {
            val (repo, ds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)

            then("should add the place to the general list") {
                repo.planningState.value.places shouldHaveSize 1
                repo.planningState.value.places shouldContain COLOSSEO
            }

            then("should not add the place to any day") {
                repo.planningState.value.days.forEach { it.places.shouldBeEmpty() }
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("savePlace with a valid dayId adds the place directly to the specified day") {
            val (repo, ds, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])

            then("should add the place to the specified day") {
                repo.planningState.value.days[1].places shouldHaveSize 1
                repo.planningState.value.days[1].places shouldContain COLOSSEO
            }

            then("should not add the place to the general list") {
                repo.planningState.value.places.shouldBeEmpty()
            }

            then("should not modify other days") {
                repo.planningState.value.days[0].places.shouldBeEmpty()
                repo.planningState.value.days[2].places.shouldBeEmpty()
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("savePlace with a non-existing dayId does not modify the state") {
            val (repo) = ctxWith3Days()
            repo.savePlace(COLOSSEO, "non-existing-day-id")

            then("should not add the place to the general list") {
                repo.planningState.value.places.shouldBeEmpty()
            }

            then("should not add the place to any day") {
                repo.planningState.value.days.forEach { it.places.shouldBeEmpty() }
            }
        }

        `when`("multiple savePlaces to the same day preserve insertion order") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.savePlace(TREVI, dayIds[1])

            then("should add all places to the specified day in insertion order") {
                val places = repo.planningState.value.days[1].places
                places shouldHaveSize 2
                places[0] shouldBe COLOSSEO
                places[1] shouldBe TREVI
            }

            then("should not add any place to the general list") {
                repo.planningState.value.places.shouldBeEmpty()
            }
        }

        `when`("saving places to both the general list and a specific day") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)
            repo.savePlace(TREVI, dayIds[1])
            repo.savePlace(PANTHEON, null)

            then("should add places with null dayId to the general list") {
                repo.planningState.value.places shouldHaveSize 2
                repo.planningState.value.places shouldContain COLOSSEO
                repo.planningState.value.places shouldContain PANTHEON
            }

            then("should add the place with a dayId to the specified day only") {
                repo.planningState.value.days[1].places shouldHaveSize 1
                repo.planningState.value.days[1].places shouldContain TREVI
            }
        }
    }

    given("movePlaceToDay") {
        `when`("movePlaceToDay moves a place from the general list to the specified day") {
            val (repo, ds, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)
            repo.savePlace(TREVI, null)
            repo.movePlaceToDay("place1", dayIds[1])

            then("should remove the place from the general list") {
                repo.planningState.value.places shouldHaveSize 1
                repo.planningState.value.places shouldNotContain COLOSSEO
                repo.planningState.value.places shouldContain TREVI
            }

            then("should add the place to the specified day") {
                repo.planningState.value.days[1].places shouldHaveSize 1
                repo.planningState.value.days[1].places shouldContain COLOSSEO
            }

            then("should not modify other days") {
                repo.planningState.value.days[0].places.shouldBeEmpty()
                repo.planningState.value.days[2].places.shouldBeEmpty()
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("movePlaceToDay with an invalid placeId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)
            val stateBefore = repo.planningState.value
            repo.movePlaceToDay("invalid-place-id", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("movePlaceToDay with an invalid dayId does not modify the state") {
            val (repo) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)
            val stateBefore = repo.planningState.value
            repo.movePlaceToDay("place1", "invalid-day-id")

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("multiple movePlaceToDay calls to the same day preserve insertion order") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)
            repo.savePlace(TREVI, null)
            repo.savePlace(PANTHEON, null)
            repo.movePlaceToDay("place1", dayIds[1])
            repo.movePlaceToDay("place2", dayIds[1])

            then("should add all moved places to the day in insertion order") {
                val places = repo.planningState.value.days[1].places
                places shouldHaveSize 2
                places[0] shouldBe COLOSSEO
                places[1] shouldBe TREVI
            }

            then("should keep remaining places in the general list") {
                repo.planningState.value.places shouldHaveSize 1
                repo.planningState.value.places shouldContain PANTHEON
            }
        }

        `when`("movePlaceToDay distributes places across different days") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)
            repo.savePlace(TREVI, null)
            repo.savePlace(PANTHEON, null)
            repo.movePlaceToDay("place1", dayIds[0])
            repo.movePlaceToDay("place2", dayIds[1])

            then("should assign each place to its specified day") {
                repo.planningState.value.days[0].places shouldHaveSize 1
                repo.planningState.value.days[0].places shouldContain COLOSSEO
                repo.planningState.value.days[1].places shouldHaveSize 1
                repo.planningState.value.days[1].places shouldContain TREVI
                repo.planningState.value.days[2].places.shouldBeEmpty()
            }

            then("should keep remaining places in the general list") {
                repo.planningState.value.places shouldHaveSize 1
                repo.planningState.value.places shouldContain PANTHEON
            }
        }
    }

    given("movePlaceToGeneral") {
        `when`("movePlaceToGeneral moves a place from a day to the general list") {
            val (repo, ds, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.savePlace(TREVI, dayIds[1])
            repo.movePlaceToGeneral("place1", dayIds[1])

            then("should remove the place from the day") {
                repo.planningState.value.days[1].places shouldHaveSize 1
                repo.planningState.value.days[1].places shouldNotContain COLOSSEO
                repo.planningState.value.days[1].places shouldContain TREVI
            }

            then("should add the place to the general list") {
                repo.planningState.value.places shouldHaveSize 1
                repo.planningState.value.places shouldContain COLOSSEO
            }

            then("should not modify other days") {
                repo.planningState.value.days[0].places.shouldBeEmpty()
                repo.planningState.value.days[2].places.shouldBeEmpty()
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("movePlaceToGeneral with an invalid placeId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            val stateBefore = repo.planningState.value
            repo.movePlaceToGeneral("invalid-place-id", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("movePlaceToGeneral with an invalid dayId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            val stateBefore = repo.planningState.value
            repo.movePlaceToGeneral("place1", "invalid-day-id")

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }
    }

    given("deletePlace") {
        `when`("deletePlace with null dayId removes the place from the general list") {
            val (repo, ds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)
            repo.savePlace(TREVI, null)
            repo.deletePlace("place1", null)

            then("should remove only the specified place from the general list") {
                repo.planningState.value.places shouldHaveSize 1
                repo.planningState.value.places shouldNotContain COLOSSEO
                repo.planningState.value.places shouldContain TREVI
            }

            then("should not modify any day") {
                repo.planningState.value.days.forEach { it.places.shouldBeEmpty() }
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("deletePlace with a valid dayId removes the place from the specified day") {
            val (repo, ds, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.savePlace(TREVI, dayIds[1])
            repo.deletePlace("place1", dayIds[1])

            then("should remove only the specified place from the day") {
                repo.planningState.value.days[1].places shouldHaveSize 1
                repo.planningState.value.days[1].places shouldNotContain COLOSSEO
                repo.planningState.value.days[1].places shouldContain TREVI
            }

            then("should not add the place to the general list") {
                repo.planningState.value.places.shouldBeEmpty()
            }

            then("should not modify other days") {
                repo.planningState.value.days[0].places.shouldBeEmpty()
                repo.planningState.value.days[2].places.shouldBeEmpty()
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("deletePlace with an invalid placeId and null dayId does not modify the state") {
            val (repo) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)
            val stateBefore = repo.planningState.value
            repo.deletePlace("invalid-place-id", null)

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("deletePlace with an invalid placeId and valid dayId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            val stateBefore = repo.planningState.value
            repo.deletePlace("invalid-place-id", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("deletePlace with an invalid dayId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            val stateBefore = repo.planningState.value
            repo.deletePlace("place1", "invalid-day-id")

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }
    }

    given("movePlaceToStep") {
        `when`("movePlaceToStep converts a day place into a Step.Place in the steps list") {
            val (repo, ds, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])

            then("should remove the place from the day's place list") {
                repo.planningState.value.days[1].places.shouldBeEmpty()
            }

            then("should add a Step.Place to the day's steps with the same id and location") {
                val steps = repo.planningState.value.days[1].steps
                steps shouldHaveSize 1
                val step = steps[0] as StepDomain.Place
                step.id shouldBe "place1"
                step.name shouldBe COLOSSEO.name
            }

            then("should not affect other days or the general places list") {
                repo.planningState.value.days[0].places.shouldBeEmpty()
                repo.planningState.value.days[0].steps.shouldBeEmpty()
                repo.planningState.value.days[2].places.shouldBeEmpty()
                repo.planningState.value.days[2].steps.shouldBeEmpty()
                repo.planningState.value.places.shouldBeEmpty()
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("movePlaceToStep with an invalid placeId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            val stateBefore = repo.planningState.value
            repo.movePlaceToStep("invalid-place-id", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("movePlaceToStep with an invalid dayId does not modify the state") {
            val (repo) = ctxWith3Days()
            repo.savePlace(COLOSSEO, null)
            val stateBefore = repo.planningState.value
            repo.movePlaceToStep("place1", "invalid-day-id")

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }
    }

    given("moveStepToPlace") {
        `when`("moveStepToPlace converts a Step.Place back to a place in the day") {
            val (repo, ds, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            repo.moveStepToPlace("place1", dayIds[1])

            then("should remove the step from the day's steps list") {
                repo.planningState.value.days[1].steps.shouldBeEmpty()
            }

            then("should recreate the place in the day with original id, name, lat and lng") {
                val places = repo.planningState.value.days[1].places
                places shouldHaveSize 1
                val place = places[0]
                place.id shouldBe "place1"
                place.name shouldBe COLOSSEO.name
                place.lat shouldBe COLOSSEO.lat
                place.lng shouldBe COLOSSEO.lng
            }

            then("should not affect other days or the general places list") {
                repo.planningState.value.days[0].places.shouldBeEmpty()
                repo.planningState.value.days[0].steps.shouldBeEmpty()
                repo.planningState.value.days[2].places.shouldBeEmpty()
                repo.planningState.value.days[2].steps.shouldBeEmpty()
                repo.planningState.value.places.shouldBeEmpty()
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("moveStepToPlace with an invalid stepId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            repo.moveStepToPlace("invalid-step-id", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("moveStepToPlace with an invalid dayId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            repo.moveStepToPlace("place1", "invalid-day-id")

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("moveStepToPlace on a Transport step does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val transportStep = StepDomain.Transport(
                id = "transport1",
                type = TransportType.TRAIN,
                route = Route(emptyList())
            )
            repo.addTransportStep(dayIds[1], "place1", transportStep)
            val stateBefore = repo.planningState.value
            repo.moveStepToPlace("transport1", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }
    }

    given("updateStep") {
        `when`("updateStep replaces the target step with the updated one") {
            val (repo, ds, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val updatedStep =
                StepDomain.Place(id = "place1", name = "Updated Location", lat = 0.0, lng = 0.0)
            repo.updateStep(dayIds[1], "place1", updatedStep)

            then("should replace the step with the updated version") {
                val steps = repo.planningState.value.days[1].steps
                steps shouldHaveSize 1
                steps[0] shouldBe updatedStep
            }

            then("should not affect other days") {
                repo.planningState.value.days[0].steps.shouldBeEmpty()
                repo.planningState.value.days[2].steps.shouldBeEmpty()
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("updateStep with an invalid dayId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            val updatedStep =
                StepDomain.Place(id = "place1", name = "Updated", lat = 0.0, lng = 0.0)
            repo.updateStep("invalid-day-id", "place1", updatedStep)

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("updateStep with an invalid stepId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            val updatedStep =
                StepDomain.Place(id = "other", name = "Updated", lat = 0.0, lng = 0.0)
            repo.updateStep(dayIds[1], "invalid-step-id", updatedStep)

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }
    }

    given("addTransportStep") {
        `when`("addTransportStep inserts a step immediately after the specified reference step") {
            val (repo, ds, dayIds) = ctxWith3Days()
            val p1 = PlaceDomain(id = "p1", name = "P1", lat = 0.0, lng = 0.0)
            val p2 = PlaceDomain(id = "p2", name = "P2", lat = 0.0, lng = 0.0)
            repo.savePlace(p1, dayIds[1])
            repo.savePlace(p2, dayIds[1])
            repo.movePlaceToStep("p1", dayIds[1])
            repo.movePlaceToStep("p2", dayIds[1])
            val newStep = StepDomain.Place(id = "new1", name = "New Stop", lat = 0.0, lng = 0.0)
            repo.addTransportStep(dayIds[1], "p1", newStep)

            then("should insert the step at afterIndex + 1") {
                val steps = repo.planningState.value.days[1].steps
                steps shouldHaveSize 3
                steps[0].id shouldBe "p1"
                steps[1].id shouldBe "new1"
                steps[2].id shouldBe "p2"
            }

            then("should not affect other days") {
                repo.planningState.value.days[0].steps.shouldBeEmpty()
                repo.planningState.value.days[2].steps.shouldBeEmpty()
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("addTransportStep with an invalid dayId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            val newStep = StepDomain.Place(id = "new1", name = "New", lat = 0.0, lng = 0.0)
            repo.addTransportStep("invalid-day-id", "place1", newStep)

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("addTransportStep with an invalid afterStepId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            val newStep = StepDomain.Place(id = "new1", name = "New", lat = 0.0, lng = 0.0)
            repo.addTransportStep(dayIds[1], "invalid-after-step-id", newStep)

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }
    }

    given("moveTravelStepUp") {
        `when`("moveTravelStepUp on the second step swaps it with the first") {
            val (repo, ds, dayIds) = ctxWith3Days()
            val p1 = PlaceDomain(id = "p1", name = "P1", lat = 0.0, lng = 0.0)
            val p2 = PlaceDomain(id = "p2", name = "P2", lat = 0.0, lng = 0.0)
            val p3 = PlaceDomain(id = "p3", name = "P3", lat = 0.0, lng = 0.0)
            repo.savePlace(p1, dayIds[1])
            repo.savePlace(p2, dayIds[1])
            repo.savePlace(p3, dayIds[1])
            repo.movePlaceToStep("p1", dayIds[1])
            repo.movePlaceToStep("p2", dayIds[1])
            repo.movePlaceToStep("p3", dayIds[1])
            repo.moveTravelStepUp("p2", dayIds[1])

            then("should reorder steps to p2, p1, p3") {
                val steps = repo.planningState.value.days[1].steps
                steps[0].id shouldBe "p2"
                steps[1].id shouldBe "p1"
                steps[2].id shouldBe "p3"
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("moveTravelStepUp on the first step does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            val p1 = PlaceDomain(id = "p1", name = "P1", lat = 0.0, lng = 0.0)
            val p2 = PlaceDomain(id = "p2", name = "P2", lat = 0.0, lng = 0.0)
            repo.savePlace(p1, dayIds[1])
            repo.savePlace(p2, dayIds[1])
            repo.movePlaceToStep("p1", dayIds[1])
            repo.movePlaceToStep("p2", dayIds[1])
            val stateBefore = repo.planningState.value
            repo.moveTravelStepUp("p1", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("moveTravelStepUp with an invalid stepId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            repo.moveTravelStepUp("invalid-step-id", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("moveTravelStepUp with an invalid dayId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            repo.moveTravelStepUp("place1", "invalid-day-id")

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }
    }

    given("deleteStep") {
        `when`("deleteStep removes a Transport step from the specified day") {
            val (repo, ds, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val transportStep = StepDomain.Transport(
                id = "transport1",
                type = TransportType.TRAIN,
                route = Route(emptyList())
            )
            repo.addTransportStep(dayIds[1], "place1", transportStep)
            repo.deleteStep("transport1", dayIds[1])

            then("should remove the Transport step from the day's steps list") {
                val steps = repo.planningState.value.days[1].steps
                steps.none { it.id == "transport1" } shouldBe true
            }

            then("should keep other steps intact") {
                val steps = repo.planningState.value.days[1].steps
                steps shouldHaveSize 1
                steps[0].id shouldBe "place1"
            }

            then("should not affect other days") {
                repo.planningState.value.days[0].steps.shouldBeEmpty()
                repo.planningState.value.days[2].steps.shouldBeEmpty()
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("deleteStep with an invalid stepId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            repo.deleteStep("invalid-step-id", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("deleteStep with an invalid dayId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            repo.deleteStep("place1", "invalid-day-id")

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }
    }

    given("moveTravelStepDown") {
        `when`("moveTravelStepDown on the second-to-last step swaps it with the last") {
            val (repo, ds, dayIds) = ctxWith3Days()
            val p1 = PlaceDomain(id = "p1", name = "P1", lat = 0.0, lng = 0.0)
            val p2 = PlaceDomain(id = "p2", name = "P2", lat = 0.0, lng = 0.0)
            val p3 = PlaceDomain(id = "p3", name = "P3", lat = 0.0, lng = 0.0)
            repo.savePlace(p1, dayIds[1])
            repo.savePlace(p2, dayIds[1])
            repo.savePlace(p3, dayIds[1])
            repo.movePlaceToStep("p1", dayIds[1])
            repo.movePlaceToStep("p2", dayIds[1])
            repo.movePlaceToStep("p3", dayIds[1])
            repo.moveTravelStepDown("p2", dayIds[1])

            then("should reorder steps to p1, p3, p2") {
                val steps = repo.planningState.value.days[1].steps
                steps[0].id shouldBe "p1"
                steps[1].id shouldBe "p3"
                steps[2].id shouldBe "p2"
            }

            then("should persist the state") {
                verifySuspend(VerifyMode.soft) { ds.saveTravelPlan(any()) }
            }
        }

        `when`("moveTravelStepDown on the last step does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            val p1 = PlaceDomain(id = "p1", name = "P1", lat = 0.0, lng = 0.0)
            val p2 = PlaceDomain(id = "p2", name = "P2", lat = 0.0, lng = 0.0)
            repo.savePlace(p1, dayIds[1])
            repo.savePlace(p2, dayIds[1])
            repo.movePlaceToStep("p1", dayIds[1])
            repo.movePlaceToStep("p2", dayIds[1])
            val stateBefore = repo.planningState.value
            repo.moveTravelStepDown("p2", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("moveTravelStepDown with an invalid stepId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            repo.moveTravelStepDown("invalid-step-id", dayIds[1])

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }

        `when`("moveTravelStepDown with an invalid dayId does not modify the state") {
            val (repo, _, dayIds) = ctxWith3Days()
            repo.savePlace(COLOSSEO, dayIds[1])
            repo.movePlaceToStep("place1", dayIds[1])
            val stateBefore = repo.planningState.value
            repo.moveTravelStepDown("place1", "invalid-day-id")

            then("should not modify the state") {
                repo.planningState.value shouldBe stateBefore
            }
        }
    }
})
