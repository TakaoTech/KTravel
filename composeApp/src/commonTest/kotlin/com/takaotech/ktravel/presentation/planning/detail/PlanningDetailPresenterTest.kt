package com.takaotech.ktravel.presentation.planning.detail

import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

private const val TRAVEL_ID = "TRAVEL_ID"
private const val DAY_ID = "DAY_ID"

class PlanningDetailPresenterTest : BehaviorSpec() {

    private val screen = PlanningDetailScreen(TRAVEL_ID, DAY_ID)

    init {
        given("a PlanningDetailPresenter") {
            `when`("the presenter is started") {
                then("the state carries the child screens with the same travel and day ids") {
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({ PlanningDetailPresenter(screen, navigator) }) {
                        val state = awaitItem()

                        state.stepsPaneScreen shouldBe StepsPaneScreen(TRAVEL_ID, DAY_ID)
                        state.placesBacklogScreen shouldBe PlacesBacklogScreen(TRAVEL_ID, DAY_ID)
                    }
                }
            }

            `when`("a ChildNav event with a GoTo is sent") {
                then("the navigator goes to the child's target screen") {
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({ PlanningDetailPresenter(screen, navigator) }) {
                        val state = awaitItem()

                        state.eventSink(
                            PlanningDetailEvent.ChildNav(NavEvent.GoTo(AddPlaceScreen(DAY_ID)))
                        )

                        navigator.awaitNextScreen() shouldBe AddPlaceScreen(DAY_ID)
                    }
                }
            }

            `when`("a ChildNav event with a Pop is sent") {
                then("the navigator pops") {
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({ PlanningDetailPresenter(screen, navigator) }) {
                        val state = awaitItem()

                        state.eventSink(PlanningDetailEvent.ChildNav(NavEvent.Pop()))

                        navigator.awaitPop()
                    }
                }
            }
        }
    }
}
