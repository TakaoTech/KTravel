package com.takaotech.ktravel.presentation.plan.day

import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import com.takaotech.ktravel.di.PlanningGraph
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds

private const val TRAVEL_ID = "TRAVEL_ID"
private const val DAY_ID = "DAY_ID"
private const val PLACE_ID = "place-1"

class PlacesBacklogPresenterTest : BehaviorSpec() {

    private val screen = PlacesBacklogScreen(TRAVEL_ID, DAY_ID)

    private fun createRepository(day: TravelDayDomain): TravelPlanRepository {
        val repository = mock<TravelPlanRepository>(MockMode.autoUnit)
        every { repository.getTravelDayFlow(DAY_ID) } returns flowOf(day)
        return repository
    }

    private fun createStore(repository: TravelPlanRepository): PlanningGraphStore {
        val mockPlanningGraph = mock<PlanningGraph>()
        every { mockPlanningGraph.travelPlanRepository } returns repository
        val mockFactory = mock<PlanningGraph.Factory>()
        every { mockFactory.create(any()) } returns mockPlanningGraph
        return PlanningGraphStore(mockFactory)
    }

    private fun dayWithOnePlace() = TravelDayDomain(
        id = DAY_ID,
        date = LocalDate(2024, 3, 1),
        places = listOf(
            PlaceDomain(id = PLACE_ID, name = "Tokyo Tower", lat = 0.0, lng = 0.0),
        ),
    )

    private suspend fun com.slack.circuit.test.CircuitReceiveTurbine<PlacesBacklogUiState>.awaitLoadedState(): PlacesBacklogUiState {
        var state = awaitItem()
        while (state.places.isEmpty()) {
            state = awaitItem()
        }
        return state
    }

    init {
        given("a PlacesBacklogPresenter with a day containing one backlog place") {
            `when`("the presenter is started") {
                then("the state emits the places and no pending delete") {
                    val repository = createRepository(dayWithOnePlace())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        PlacesBacklogPresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.places shouldHaveSize 1
                        state.places[0].id shouldBe PLACE_ID
                        state.pendingPermanentDelete shouldBe null
                    }
                }
            }

            `when`("a MovePlaceToSteps event is sent") {
                then("movePlaceToStep is called on the repository") {
                    val repository = createRepository(dayWithOnePlace())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        PlacesBacklogPresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(PlacesBacklogEvent.MovePlaceToSteps(PLACE_ID))

                        eventually(2.seconds) {
                            verifySuspend { repository.movePlaceToStep(PLACE_ID, DAY_ID) }
                        }
                    }
                }
            }

            `when`("a MovePlaceToBacklog event is sent") {
                then("movePlaceToGeneral is called on the repository") {
                    val repository = createRepository(dayWithOnePlace())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        PlacesBacklogPresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(PlacesBacklogEvent.MovePlaceToBacklog(PLACE_ID))

                        eventually(2.seconds) {
                            verifySuspend { repository.movePlaceToGeneral(PLACE_ID, DAY_ID) }
                        }
                    }
                }
            }

            `when`("a PermanentDeleteRequested event is sent") {
                then("the pending delete is set to the requested place") {
                    val repository = createRepository(dayWithOnePlace())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        PlacesBacklogPresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(PlacesBacklogEvent.PermanentDeleteRequested(PLACE_ID))

                        val updated = awaitItem()
                        updated.pendingPermanentDelete?.id shouldBe PLACE_ID
                    }
                }
            }

            `when`("a pending delete is dismissed") {
                then("the pending delete is cleared and deletePlace is never called") {
                    val repository = createRepository(dayWithOnePlace())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        PlacesBacklogPresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(PlacesBacklogEvent.PermanentDeleteRequested(PLACE_ID))
                        awaitItem().pendingPermanentDelete?.id shouldBe PLACE_ID

                        state.eventSink(PlacesBacklogEvent.PermanentDeleteDismissed)

                        awaitItem().pendingPermanentDelete shouldBe null
                        verifySuspend(VerifyMode.exactly(0)) {
                            repository.deletePlace(any(), any())
                        }
                    }
                }
            }

            `when`("a pending delete is confirmed") {
                then("deletePlace is called with the place and day ids and the pending delete is cleared") {
                    val repository = createRepository(dayWithOnePlace())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        PlacesBacklogPresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(PlacesBacklogEvent.PermanentDeleteRequested(PLACE_ID))
                        awaitItem().pendingPermanentDelete?.id shouldBe PLACE_ID

                        state.eventSink(PlacesBacklogEvent.PermanentDeleteConfirmed)

                        awaitItem().pendingPermanentDelete shouldBe null
                        eventually(2.seconds) {
                            verifySuspend { repository.deletePlace(PLACE_ID, DAY_ID) }
                        }
                    }
                }
            }

            `when`("an AddPlace event is sent") {
                then("the navigator goes to AddPlaceScreen with the screen dayId") {
                    val repository = createRepository(dayWithOnePlace())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        PlacesBacklogPresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(PlacesBacklogEvent.AddPlace)

                        navigator.awaitNextScreen() shouldBe AddPlaceScreen(TRAVEL_ID, DAY_ID)
                    }
                }
            }

            `when`("a Close event is sent") {
                then("the navigator pops") {
                    val repository = createRepository(dayWithOnePlace())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        PlacesBacklogPresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(PlacesBacklogEvent.Close)

                        navigator.awaitPop()
                    }
                }
            }
        }
    }
}
