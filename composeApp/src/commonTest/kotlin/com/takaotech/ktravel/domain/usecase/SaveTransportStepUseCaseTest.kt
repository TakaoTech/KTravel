package com.takaotech.ktravel.domain.usecase

import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.RoutingSection
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration.Companion.minutes

class SaveTransportStepUseCaseTest :
    BehaviorSpec({

        given("a SaveTransportStepUseCase") {
            `when`("invoked with a route whose first section mode is TRAIN") {
                val fakeRepository = FakeTravelPlanRepositoryForTransport()
                val useCase = SaveTransportStepUseCase(fakeRepository)
                val route = routeWithMode("TRAIN")

                useCase("day-1", "step-1", route)

                then("should save a Transport step with type TRAIN") {
                    val saved = fakeRepository.savedStep
                    saved.shouldBeInstanceOf<StepDomain.Transport>()
                    saved.type shouldBe TransportType.TRAIN
                }
            }

            `when`("invoked with a route whose first section mode is BUS") {
                val fakeRepository = FakeTravelPlanRepositoryForTransport()
                val useCase = SaveTransportStepUseCase(fakeRepository)
                val route = routeWithMode("BUS")

                useCase("day-1", "step-1", route)

                then("should save a Transport step with type BUS") {
                    val saved = fakeRepository.savedStep
                    saved.shouldBeInstanceOf<StepDomain.Transport>()
                    saved.type shouldBe TransportType.BUS
                }
            }

            `when`("invoked with a route whose first section mode is FLIGHT") {
                val fakeRepository = FakeTravelPlanRepositoryForTransport()
                val useCase = SaveTransportStepUseCase(fakeRepository)
                val route = routeWithMode("FLIGHT")

                useCase("day-1", "step-1", route)

                then("should save a Transport step with type FLIGHT") {
                    val saved = fakeRepository.savedStep
                    saved.shouldBeInstanceOf<StepDomain.Transport>()
                    saved.type shouldBe TransportType.FLIGHT
                }
            }

            `when`("invoked with a route whose first section mode is lowercase 'train'") {
                val fakeRepository = FakeTravelPlanRepositoryForTransport()
                val useCase = SaveTransportStepUseCase(fakeRepository)
                val route = routeWithMode("train")

                useCase("day-1", "step-1", route)

                then("should save a Transport step with type TRAIN (case-insensitive)") {
                    val saved = fakeRepository.savedStep
                    saved.shouldBeInstanceOf<StepDomain.Transport>()
                    saved.type shouldBe TransportType.TRAIN
                }
            }

            `when`("invoked with a route whose first section mode is unknown") {
                val fakeRepository = FakeTravelPlanRepositoryForTransport()
                val useCase = SaveTransportStepUseCase(fakeRepository)
                val route = routeWithMode("FERRY")

                useCase("day-1", "step-1", route)

                then("should save a Transport step with type CAR as default") {
                    val saved = fakeRepository.savedStep
                    saved.shouldBeInstanceOf<StepDomain.Transport>()
                    saved.type shouldBe TransportType.CAR
                }
            }

            `when`("invoked with a route that has no sections") {
                val fakeRepository = FakeTravelPlanRepositoryForTransport()
                val useCase = SaveTransportStepUseCase(fakeRepository)
                val route = RoutingRoute(
                    summary = RouteSummary(durationSeconds = 0.minutes, distance = 0),
                    sections = emptyList(),
                )

                useCase("day-1", "step-1", route)

                then("should save a Transport step with type CAR as default") {
                    val saved = fakeRepository.savedStep
                    saved.shouldBeInstanceOf<StepDomain.Transport>()
                    saved.type shouldBe TransportType.CAR
                }
            }

            `when`("invoked with a route whose first section has no transport info") {
                val fakeRepository = FakeTravelPlanRepositoryForTransport()
                val useCase = SaveTransportStepUseCase(fakeRepository)
                val route = RoutingRoute(
                    summary = RouteSummary(durationSeconds = 30.minutes, distance = 1000),
                    sections = emptyList(),
                )

                useCase("day-1", "step-1", route)

                then("should save a Transport step with type CAR as default") {
                    val saved = fakeRepository.savedStep
                    saved.shouldBeInstanceOf<StepDomain.Transport>()
                    saved.type shouldBe TransportType.CAR
                }
            }

            `when`("invoked with a journey whose first step is the walk to the station") {
                val fakeRepository = FakeTravelPlanRepositoryForTransport()
                val useCase = SaveTransportStepUseCase(fakeRepository)

                useCase("day-1", "step-1", journeyRiding("REGIONAL_TRAIN"))

                then("should file it under the first vehicle and not under the walk") {
                    val saved = fakeRepository.savedStep
                    saved.shouldBeInstanceOf<StepDomain.Transport>()
                    saved.type shouldBe TransportType.TRAIN
                }
            }

            `when`("invoked with a journey on the underground") {
                val fakeRepository = FakeTravelPlanRepositoryForTransport()
                val useCase = SaveTransportStepUseCase(fakeRepository)

                useCase("day-1", "step-1", journeyRiding("SUBWAY"))

                then("should fall back to the default, which the plan's vocabulary has no word for") {
                    val saved = fakeRepository.savedStep
                    saved.shouldBeInstanceOf<StepDomain.Transport>()
                    saved.type shouldBe TransportType.CAR
                }
            }

            `when`("invoked with specific dayId and afterStepId") {
                val fakeRepository = FakeTravelPlanRepositoryForTransport()
                val useCase = SaveTransportStepUseCase(fakeRepository)
                val route = routeWithMode("CAR")

                useCase("my-day", "my-step", route)

                then("should pass the correct dayId and afterStepId to the repository") {
                    fakeRepository.savedDayId shouldBe "my-day"
                    fakeRepository.savedAfterStepId shouldBe "my-step"
                }
            }
        }
    })

private fun routeWithMode(mode: String) = RoutingRoute(
    summary = RouteSummary(durationSeconds = 30.minutes, distance = 1000),
    sections = listOf(
        RoutingSection(summary = RouteSummary(durationSeconds = 30.minutes, distance = 1000), mode = mode),
    ),
)

/** A journey that walks to a stop, rides [mode], and walks off — which is every journey. */
private fun journeyRiding(mode: String) = TransitJourney(
    summary = RouteSummary(durationSeconds = 30.minutes, distance = 1000),
    steps = listOf(
        TransitStep.Walk(summary = RouteSummary(durationSeconds = 4.minutes, distance = 200)),
        TransitStep.Ride(
            summary = RouteSummary(durationSeconds = 22.minutes, distance = 700),
            line = TransitLine(mode = mode, name = "M2"),
        ),
        TransitStep.Walk(summary = RouteSummary(durationSeconds = 4.minutes, distance = 100)),
    ),
)

private class FakeTravelPlanRepositoryForTransport : TravelPlanRepository {
    var savedStep: StepDomain? = null
    var savedDayId: String? = null
    var savedAfterStepId: String? = null

    private val _planningState = MutableStateFlow(TravelPlanDomain())
    override val planningState: StateFlow<TravelPlanDomain> = _planningState

    override fun getTravelDayFlow(dayId: String): Flow<TravelDayDomain> = flowOf(TravelDayDomain.EMPTY)
    override suspend fun updatePeriod(startMillis: Long, endMillis: Long) = Unit
    override suspend fun updateStep(dayId: String, stepId: String, updatedStep: StepDomain) = Unit
    override suspend fun updatePlaceNote(dayId: String, stepId: String, note: String) = Unit
    override suspend fun updatePlaceStartTime(dayId: String, stepId: String, time: kotlinx.datetime.LocalTime) = Unit

    override suspend fun updatePlaceEndTime(dayId: String, stepId: String, time: kotlinx.datetime.LocalTime) = Unit

    override suspend fun addAttachment(dayId: String, stepId: String, source: io.github.vinceglb.filekit.PlatformFile) =
        Unit

    override suspend fun removeAttachment(dayId: String, stepId: String, attachmentId: String) = Unit
    override suspend fun updatePlanName(name: String) = Unit

    override suspend fun updateSettings(settings: com.takaotech.ktravel.domain.model.TravelSettingsDomain) = Unit

    override suspend fun savePlace(place: com.takaotech.ktravel.domain.model.PlaceDomain, dayId: String?) = Unit
    override suspend fun movePlaceToDay(placeId: String, dayId: String) = Unit
    override suspend fun movePlaceToGeneral(placeId: String, dayId: String) = Unit
    override suspend fun movePlaceToStep(placeId: String, dayId: String) = Unit
    override suspend fun moveStepToPlace(stepId: String, dayId: String) = Unit
    override suspend fun moveTravelStepUp(stepId: String, dayId: String) = Unit
    override suspend fun moveTravelStepDown(stepId: String, dayId: String) = Unit
    override suspend fun addTransportStep(dayId: String, afterStepId: String, step: StepDomain) {
        savedDayId = dayId
        savedAfterStepId = afterStepId
        savedStep = step
    }

    override suspend fun deletePlace(placeId: String, dayId: String?) = Unit
    override suspend fun deleteStep(stepId: String, dayId: String) = Unit
    override suspend fun removeStep(stepId: String, dayId: String) = Unit
}
