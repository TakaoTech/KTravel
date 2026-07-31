package com.takaotech.ktravel.presentation.planning

import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.Route
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

class TravelDayUiTest : BehaviorSpec() {

    private fun place(id: String) = StepUi.Place(id = id, name = "Place $id", lat = 0.0, lng = 0.0)

    private fun transport(id: String) = StepUi.Transport(
        id = id,
        type = TransportType.TRAIN,
        route = Route(sections = emptyList())
    )

    private fun day(vararg steps: StepUi) = TravelDayUi(
        id = "day",
        date = LocalDate(2024, 6, 15),
        steps = persistentListOf(*steps)
    )

    init {
        given("a day with interleaved place and transport steps") {
            val travelDay =
                day(place("a"), transport("t1"), place("b"), transport("t2"), place("c"))

            `when`("reading placeSteps") {
                val placeSteps = travelDay.placeSteps

                then("only the place steps are kept, in the original order") {
                    placeSteps.map { it.id } shouldBe listOf("a", "b", "c")
                }
            }
        }

        given("a day with transport steps only") {
            val travelDay = day(transport("t1"), transport("t2"))

            `when`("reading placeSteps") {
                then("no place step is produced") {
                    travelDay.placeSteps shouldHaveSize 0
                }
            }
        }

        given("a day without steps") {
            `when`("reading placeSteps") {
                then("the list is empty") {
                    day().placeSteps shouldHaveSize 0
                    TravelDayUi.EMPTY.placeSteps shouldHaveSize 0
                }
            }
        }

        given("a day already holding a place step") {
            val travelDay = day(place("a"))

            `when`("copying the day with an additional place step") {
                val updated = travelDay.copy(steps = travelDay.steps.adding(place("b")))

                then("placeSteps reflects the new steps") {
                    updated.placeSteps.map { it.id } shouldBe listOf("a", "b")
                }

                then("the original day is left untouched") {
                    travelDay.placeSteps.map { it.id } shouldBe listOf("a")
                }
            }
        }
    }
}
