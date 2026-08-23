@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.presentation.planning.detail

import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import com.takaotech.ktravel.data.datasource.AttachmentDataSource
import com.takaotech.ktravel.di.PlanningGraph
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.testutil.roadAnswer
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private const val TRAVEL_ID = "TRAVEL_ID"
private const val DAY_ID = "DAY_ID"
private const val TRANSPORT_ID = "transport-1"

class TransportDetailPresenterTest : BehaviorSpec() {

    private val screen = TransportDetailScreen(TRAVEL_ID, DAY_ID, TRANSPORT_ID)

    private val answer = roadAnswer(
        duration = 14.minutes,
        metres = 10400.0,
        actions = listOf(
            RouteAction(action = "turn", durationSeconds = 45.seconds, distanceMeters = 320.0 * Length.meters),
        ),
    )

    private fun transport(note: String = "") = StepDomain.Transport(
        id = TRANSPORT_ID,
        type = TransportType.CAR,
        answer = answer,
        note = note,
    )

    private fun createRepository(day: TravelDayDomain): TravelPlanRepository {
        val repository = mock<TravelPlanRepository>(MockMode.autoUnit)
        every { repository.getTravelDayFlow(DAY_ID) } returns flowOf(day)
        return repository
    }

    private fun createStore(repository: TravelPlanRepository): PlanningGraphStore {
        val graph = mock<PlanningGraph>()
        every { graph.travelPlanRepository } returns repository
        val factory = mock<PlanningGraph.Factory>()
        every { factory.create(any()) } returns graph
        return PlanningGraphStore(factory)
    }

    private fun attachments(): AttachmentDataSource = mock<AttachmentDataSource>(MockMode.autoUnit)

    private fun dayWithTransportBetweenPlaces(note: String = "") = TravelDayDomain(
        id = DAY_ID,
        date = LocalDate(2026, 5, 30),
        steps = listOf(
            StepDomain.Place(id = "place-a", name = "Binasco", lat = 45.33, lng = 9.10),
            transport(note),
            StepDomain.Place(id = "place-b", name = "Assago", lat = 45.40, lng = 9.12),
        ),
    )

    private suspend fun com.slack.circuit.test.CircuitReceiveTurbine<TransportDetailUiState>.awaitLoaded():
        TransportDetailUiState {
        var state = awaitItem()
        while (state.transport == null) {
            state = awaitItem()
        }
        return state
    }

    init {
        given("a transport sitting between two places") {
            `when`("the presenter is started") {
                then("it should publish the leg with the names of the places it joins") {
                    val repository = createRepository(dayWithTransportBetweenPlaces(note = "**Carriage 4**"))

                    presenterTestOf({
                        TransportDetailPresenter(
                            screen,
                            FakeNavigator(screen),
                            createStore(repository),
                            attachments(),
                        )
                    }) {
                        val state = awaitLoaded()
                        val transport = state.transport.shouldNotBeNull()

                        transport.fromName shouldBe "Binasco"
                        transport.toName shouldBe "Assago"
                        transport.totalDuration shouldBe 14.minutes
                        transport.answer.shouldBeInstanceOf<TransportAnswer.Routing>()
                        state.notes.note shouldBe "**Carriage 4**"
                        state.canRecalculate shouldBe true
                    }
                }
            }

            `when`("a Recalculate event is sent") {
                then("it should open the computation flow on those two places") {
                    val repository = createRepository(dayWithTransportBetweenPlaces())
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({
                        TransportDetailPresenter(screen, navigator, createStore(repository), attachments())
                    }) {
                        awaitLoaded().eventSink(TransportDetailEvent.Recalculate)

                        val target = navigator.awaitNextScreen().shouldBeInstanceOf<AddTransportScreen>()
                        target.dayId shouldBe DAY_ID
                        target.startPlaceId shouldBe "place-a"
                        target.endPlaceId shouldBe "place-b"
                    }
                }
            }

            `when`("a note event is sent") {
                then("it should reach the repository through the shared notes block") {
                    val repository = createRepository(dayWithTransportBetweenPlaces())

                    presenterTestOf({
                        TransportDetailPresenter(
                            screen,
                            FakeNavigator(screen),
                            createStore(repository),
                            attachments(),
                        )
                    }) {
                        val state = awaitLoaded()

                        state.eventSink(
                            TransportDetailEvent.Notes(StepNotesEvent.NoteChanged("# Stop at the services")),
                        )

                        eventually(3.seconds) {
                            verifySuspend {
                                repository.updateStepNote(DAY_ID, TRANSPORT_ID, "# Stop at the services")
                            }
                        }

                        // Typing re-emits the state; the assertion is on the repository, not on how
                        // many times the screen recomposed on the way there.
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("a transport with no place after it") {
            val day = TravelDayDomain(
                id = DAY_ID,
                date = LocalDate(2026, 5, 30),
                steps = listOf(
                    StepDomain.Place(id = "place-a", name = "Binasco", lat = 45.33, lng = 9.10),
                    transport(),
                ),
            )

            `when`("the presenter is started") {
                then("the leg should still be shown, but not be recomputable") {
                    val repository = createRepository(day)

                    presenterTestOf({
                        TransportDetailPresenter(
                            screen,
                            FakeNavigator(screen),
                            createStore(repository),
                            attachments(),
                        )
                    }) {
                        val state = awaitLoaded()

                        state.transport.shouldNotBeNull().toName shouldBe ""
                        state.canRecalculate shouldBe false
                    }
                }
            }
        }
    }
}
