package com.takaotech.ktravel.presentation.planning.detail

import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import com.takaotech.ktravel.di.PlanningGraph
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds

private const val TRAVEL_ID = "TRAVEL_ID"
private const val DAY_ID = "DAY_ID"

class StepsPanePresenterTest : BehaviorSpec() {

    private val screen = StepsPaneScreen(TRAVEL_ID, DAY_ID)

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

    private fun dayWithTwoPlaces() = TravelDayDomain(
        id = DAY_ID,
        date = LocalDate(2024, 3, 1),
        steps = listOf(
            StepDomain.Place(id = "step-a", name = "Tokyo Tower", lat = 0.0, lng = 0.0),
            StepDomain.Place(id = "step-b", name = "Shibuya Crossing", lat = 0.0, lng = 0.0),
        ),
    )

    private suspend fun com.slack.circuit.test.CircuitReceiveTurbine<StepsPaneUiState>.awaitLoadedState(): StepsPaneUiState {
        var state = awaitItem()
        while (state.rows.isEmpty()) {
            state = awaitItem()
        }
        return state
    }

    init {
        given("a StepsPanePresenter with a day containing two adjacent places") {
            `when`("the presenter is started") {
                then("the state emits step rows with an add-transport slot between the places") {
                    val repository = createRepository(dayWithTwoPlaces())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        StepsPanePresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.rows shouldHaveSize 3
                        state.rows[0].shouldBeInstanceOf<StepRow.Step>()
                        val slot = state.rows[1].shouldBeInstanceOf<StepRow.AddTransportSlot>()
                        slot.startPlaceId shouldBe "step-a"
                        slot.endPlaceId shouldBe "step-b"
                        state.rows[2].shouldBeInstanceOf<StepRow.Step>()
                    }
                }
            }

            `when`("a DeleteStep event is sent") {
                then("removeStep is called on the repository with the step and day ids") {
                    val repository = createRepository(dayWithTwoPlaces())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        StepsPanePresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()
                        val step = (state.rows[0] as StepRow.Step).step

                        state.eventSink(StepsPaneEvent.DeleteStep(step))

                        eventually(2.seconds) {
                            verifySuspend { repository.removeStep("step-a", DAY_ID) }
                        }
                    }
                }
            }

            `when`("a MoveStepUp event is sent") {
                then("moveTravelStepUp is called on the repository") {
                    val repository = createRepository(dayWithTwoPlaces())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        StepsPanePresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(StepsPaneEvent.MoveStepUp("step-b"))

                        eventually(2.seconds) {
                            verifySuspend { repository.moveTravelStepUp("step-b", DAY_ID) }
                        }
                    }
                }
            }

            `when`("a MoveStepDown event is sent") {
                then("moveTravelStepDown is called on the repository") {
                    val repository = createRepository(dayWithTwoPlaces())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        StepsPanePresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(StepsPaneEvent.MoveStepDown("step-a"))

                        eventually(2.seconds) {
                            verifySuspend { repository.moveTravelStepDown("step-a", DAY_ID) }
                        }
                    }
                }
            }

            `when`("an AddTransport event is sent") {
                then("the navigator goes to AddTransportScreen with the screen dayId and the step ids") {
                    val repository = createRepository(dayWithTwoPlaces())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        StepsPanePresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(StepsPaneEvent.AddTransport("step-a", "step-b"))

                        navigator.awaitNextScreen() shouldBe
                                AddTransportScreen(DAY_ID, "step-a", "step-b")
                    }
                }
            }

            `when`("a NavigateBack event is sent") {
                then("the navigator pops") {
                    val repository = createRepository(dayWithTwoPlaces())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        StepsPanePresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(StepsPaneEvent.NavigateBack)

                        navigator.awaitPop()
                    }
                }
            }

            `when`("an OpenBacklog event is sent") {
                then("the navigator goes to PlacesBacklogScreen for the same travel and day") {
                    val repository = createRepository(dayWithTwoPlaces())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        StepsPanePresenter(screen, navigator, createStore(repository))
                    }) {
                        val state = awaitLoadedState()

                        state.eventSink(StepsPaneEvent.OpenBacklog)

                        navigator.awaitNextScreen() shouldBe PlacesBacklogScreen(TRAVEL_ID, DAY_ID)
                    }
                }
            }
        }
    }
}
