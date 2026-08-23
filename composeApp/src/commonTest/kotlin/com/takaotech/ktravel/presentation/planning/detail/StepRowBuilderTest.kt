package com.takaotech.ktravel.presentation.planning.detail

import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.testutil.roadAnswer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class StepRowBuilderTest : BehaviorSpec() {

    private fun place(id: String) = StepUi.Place(id = id, name = "Place $id", lat = 0.0, lng = 0.0)

    private fun transport(id: String) = StepUi.Transport(
        id = id,
        type = TransportType.TRAIN,
        answer = roadAnswer(),
    )

    init {
        given("an empty step list") {
            `when`("building rows") {
                val rows = buildStepRows(emptyList())

                then("no rows are produced") {
                    rows shouldHaveSize 0
                }
            }
        }

        given("a single place step") {
            `when`("building rows") {
                val rows = buildStepRows(listOf(place("a")))

                then("only the step row is produced, without transport slots") {
                    rows shouldHaveSize 1
                    rows[0].shouldBeInstanceOf<StepRow.Step>()
                }
            }
        }

        given("two adjacent place steps") {
            `when`("building rows") {
                val rows = buildStepRows(listOf(place("a"), place("b")))

                then("an add-transport slot is inserted between them") {
                    rows shouldHaveSize 3
                    rows[0].shouldBeInstanceOf<StepRow.Step>()
                    rows[1].shouldBeInstanceOf<StepRow.AddTransportSlot>()
                    rows[2].shouldBeInstanceOf<StepRow.Step>()
                }

                then("the slot carries the ids of the surrounding steps") {
                    val slot = rows[1].shouldBeInstanceOf<StepRow.AddTransportSlot>()
                    slot.startPlaceId shouldBe "a"
                    slot.endPlaceId shouldBe "b"
                }
            }
        }

        given("a place followed by a transport and another place") {
            `when`("building rows") {
                val rows = buildStepRows(listOf(place("a"), transport("t"), place("b")))

                then("no add-transport slot is inserted") {
                    rows shouldHaveSize 3
                    rows.filterIsInstance<StepRow.AddTransportSlot>() shouldHaveSize 0
                }
            }
        }

        given("a transport followed by a place") {
            `when`("building rows") {
                val rows = buildStepRows(listOf(transport("t"), place("a")))

                then("no slot is inserted after the transport (legacy behavior parity)") {
                    rows shouldHaveSize 2
                    rows.filterIsInstance<StepRow.AddTransportSlot>() shouldHaveSize 0
                }
            }
        }

        given("a trailing place step") {
            `when`("building rows") {
                val rows = buildStepRows(listOf(place("a"), transport("t"), place("b")))

                then("no slot is inserted after the last step") {
                    rows.last().shouldBeInstanceOf<StepRow.Step>()
                }
            }
        }

        given("a mixed sequence place-place-transport-place-place") {
            val steps = listOf(place("a"), place("b"), transport("t"), place("c"), place("d"))

            `when`("building rows") {
                val rows = buildStepRows(steps)

                then("slots are inserted only between adjacent places") {
                    val slots = rows.filterIsInstance<StepRow.AddTransportSlot>()
                    slots shouldHaveSize 2
                    slots[0].startPlaceId shouldBe "a"
                    slots[0].endPlaceId shouldBe "b"
                    slots[1].startPlaceId shouldBe "c"
                    slots[1].endPlaceId shouldBe "d"
                }

                then("all steps are preserved in order") {
                    rows.filterIsInstance<StepRow.Step>().map { it.step.id } shouldBe
                        listOf("a", "b", "t", "c", "d")
                }
            }
        }

        given("a transport between two places") {
            val rows = buildStepRows(listOf(place("a"), transport("t"), place("b")))

            `when`("looking for the places around it") {
                then("the place before and the place after are returned") {
                    rows.transportNeighbours("t") shouldBe ("a" to "b")
                }
            }

            `when`("looking for a step id that is not there") {
                then("nothing is returned") {
                    rows.transportNeighbours("missing") shouldBe null
                }
            }
        }

        given("a transport with no place after it") {
            val rows = buildStepRows(listOf(place("a"), transport("t")))

            `when`("looking for the places around it") {
                then("nothing is returned, because there is nowhere to travel to") {
                    rows.transportNeighbours("t") shouldBe null
                }
            }
        }

        given("a transport with no place before it") {
            val rows = buildStepRows(listOf(transport("t"), place("b")))

            `when`("looking for the places around it") {
                then("nothing is returned, because there is nowhere to travel from") {
                    rows.transportNeighbours("t") shouldBe null
                }
            }
        }

        given("rows built from the same steps") {
            `when`("comparing keys") {
                val rows1 = buildStepRows(listOf(place("a"), place("b")))
                val rows2 = buildStepRows(listOf(place("a"), place("b")))

                then("keys are stable across invocations") {
                    rows1.map { it.key } shouldBe rows2.map { it.key }
                }

                then("keys are unique within the list") {
                    val keys = rows1.map { it.key }
                    keys.toSet().size shouldBe keys.size
                }
            }
        }
    }
}
