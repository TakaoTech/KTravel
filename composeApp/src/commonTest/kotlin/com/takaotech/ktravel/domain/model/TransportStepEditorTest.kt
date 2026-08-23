package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.domain.model.TravelPlanEditor.putTransportStep
import com.takaotech.ktravel.domain.model.TravelPlanEditor.transportAfter
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.testutil.roadAnswer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

/**
 * Verifies that a transport is identified by the two places it sits between: it is looked up by the
 * step it follows, and computing it again replaces it rather than adding a second one.
 */
class TransportStepEditorTest :
    BehaviorSpec({

        fun place(id: String) = StepDomain.Place(id = id, name = id, lat = 0.0, lng = 0.0)

        fun transport(id: String, type: TransportType = TransportType.CAR, request: RouteSelection? = null) =
            StepDomain.Transport(id = id, type = type, answer = roadAnswer(), request = request)

        fun planWith(vararg steps: StepDomain) = TravelPlanDomain(
            days = listOf(TravelDayDomain(id = "day", date = LocalDate(2024, 1, 1), steps = steps.toList())),
        )

        val request = RouteSelection.Routing(
            profileId = RoutingProfileId(provider = "here", profile = "routing"),
            mode = RoutingMode("CAR"),
        )

        given("a day whose second step is a transport") {
            val steps = listOf(place("a"), transport("t", request = request), place("b"))

            `when`("transportAfter is asked for the first place") {
                then("it returns that transport") {
                    steps.transportAfter("a")?.id shouldBe "t"
                }
                then("the request it was computed with comes with it") {
                    steps.transportAfter("a")?.request shouldBe request
                }
            }

            `when`("transportAfter is asked for the transport itself") {
                then("it returns nothing, because a place follows it") {
                    steps.transportAfter("t") shouldBe null
                }
            }

            `when`("transportAfter is asked for a step that is not there") {
                then("it returns nothing") {
                    steps.transportAfter("missing") shouldBe null
                }
            }
        }

        given("a day with two places and nothing between them") {
            val plan = planWith(place("a"), place("b"))

            `when`("putTransportStep files a transport after the first") {
                val updated = plan.putTransportStep("day", "a", transport("t"))

                then("it is inserted between the two places") {
                    updated.days[0].steps.map { it.id } shouldBe listOf("a", "t", "b")
                }
            }
        }

        given("a day that already has a transport between its two places") {
            val plan = planWith(place("a"), transport("first", TransportType.TRAIN), place("b"))

            `when`("putTransportStep files another transport after the first place") {
                val updated = plan.putTransportStep("day", "a", transport("second", TransportType.BUS, request))

                then("the day still has three steps") {
                    updated.days[0].steps shouldHaveSize 3
                }
                then("the transport carries what was just computed") {
                    val replaced = updated.days[0].steps[1] as StepDomain.Transport
                    replaced.type shouldBe TransportType.BUS
                    replaced.request shouldBe request
                }
                then("the id of the transport it replaced is kept") {
                    updated.days[0].steps[1].id shouldBe "first"
                }
            }
        }

        given("a plan being edited through an id that is not there") {
            val plan = planWith(place("a"), place("b"))

            `when`("putTransportStep is given an unknown day") {
                then("nothing changes") {
                    plan.putTransportStep("other-day", "a", transport("t")) shouldBe plan
                }
            }

            `when`("putTransportStep is given an unknown reference step") {
                then("nothing changes") {
                    plan.putTransportStep("day", "missing", transport("t")) shouldBe plan
                }
            }
        }
    })
