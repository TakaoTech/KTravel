@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.data.entity.StepEntity
import com.takaotech.ktravel.data.mapper.TravelPlanEntityMapper
import com.takaotech.ktravel.domain.model.TravelPlanEditor.addStepAttachment
import com.takaotech.ktravel.domain.model.TravelPlanEditor.removeStepAttachment
import com.takaotech.ktravel.domain.model.TravelPlanEditor.updateStepNote
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import com.takaotech.ktravel.testutil.roadAnswer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A leg carries notes and files just like a place does: what to do at the wheel, where to stop,
 * the ticket. This covers the editor over the transport variant and the round trip that has to keep
 * them, including reading back a document written before the fields existed.
 */
class TransportStepNotesTest : BehaviorSpec() {

    private val attachment = AttachmentDomain(
        id = "a1",
        relativePath = "t1/t-step/ticket.pdf",
        originalName = "ticket.pdf",
        mimeType = "application/pdf",
        sizeBytes = 512,
    )

    private fun planWithTransport(transport: StepDomain.Transport) = TravelPlanDomain(
        days = listOf(
            TravelDayDomain(
                id = "d1",
                date = LocalDate.fromEpochDays(0),
                steps = listOf(transport),
            ),
        ),
    )

    private fun transport(
        note: String = "",
        attachments: List<AttachmentDomain> = emptyList(),
        calculatedAt: Instant? = null,
    ) = StepDomain.Transport(
        id = "t1",
        type = TransportType.TRAIN,
        answer = roadAnswer(),
        note = note,
        attachments = attachments,
        calculatedAt = calculatedAt,
    )

    private fun TravelPlanDomain.transportStep(): StepDomain.Transport =
        days.first().steps.first().shouldBeInstanceOf<StepDomain.Transport>()

    init {
        given("a plan whose only step is a transport") {
            val plan = planWithTransport(transport())

            `when`("updateStepNote is called on it") {
                val updated = plan.updateStepNote("d1", "t1", "# Board at the front")

                then("it should store the note on the transport") {
                    updated.transportStep().note shouldBe "# Board at the front"
                }
            }

            `when`("addStepAttachment is called on it") {
                val updated = plan.addStepAttachment("d1", "t1", attachment)

                then("it should add the file to the transport inventory") {
                    updated.transportStep().attachments shouldBe listOf(attachment)
                }
            }

            `when`("addStepAttachment targets an unknown step") {
                val updated = plan.addStepAttachment("d1", "unknown", attachment)

                then("it should return the plan unchanged") {
                    updated shouldBe plan
                }
            }
        }

        given("a transport that already holds a file") {
            val plan = planWithTransport(transport(attachments = listOf(attachment)))

            `when`("removeStepAttachment is called with its id") {
                val updated = plan.removeStepAttachment("d1", "t1", "a1")

                then("the inventory should be empty") {
                    updated.transportStep().attachments shouldHaveSize 0
                }
            }

            `when`("removeStepAttachment is called with an unknown id") {
                val updated = plan.removeStepAttachment("d1", "t1", "unknown")

                then("it should return the plan unchanged") {
                    updated shouldBe plan
                }
            }
        }

        given("a transport with a note, a file and a computation time") {
            val computedAt = Instant.parse("2026-05-30T09:38:00Z")
            val step = transport(
                note = "**Carriage 4**",
                attachments = listOf(attachment),
                calculatedAt = computedAt,
            )

            `when`("mapped Domain -> Entity -> Domain") {
                val entity = with(TravelPlanEntityMapper) { step.toEntity() } as StepEntity.Transport
                val back = with(TravelPlanEntityMapper) { entity.toDomain() } as StepDomain.Transport

                then("note, inventory and computation time should survive") {
                    back.note shouldBe "**Carriage 4**"
                    back.attachments shouldBe listOf(attachment)
                    back.calculatedAt shouldBe computedAt
                }
            }

            `when`("mapped Domain -> Ui") {
                val ui = with(TravelPlanUiMapper) { step.toUiStep() }.shouldBeInstanceOf<StepUi.Transport>()

                then("the UI step should carry them too") {
                    ui.note shouldBe "**Carriage 4**"
                    ui.attachments.single().id shouldBe "a1"
                    ui.calculatedAt shouldBe computedAt
                }
            }
        }

        given("a transport document written before notes were recorded") {
            val json = """
                {
                    "type": "transport",
                    "id": "t1",
                    "transport_type": "TRAIN",
                    "route": { "sections": [] }
                }
            """.trimIndent()

            `when`("deserialized") {
                val entity = Json.decodeFromString<StepEntity>(json).shouldBeInstanceOf<StepEntity.Transport>()

                then("it should read back with the defaults rather than failing") {
                    entity.note shouldBe ""
                    entity.attachments shouldHaveSize 0
                    entity.calculatedAt.shouldBeNull()
                }
            }
        }

        given("a transport document written before the two kinds of answer were told apart") {
            // Exactly what sits in Couchbase on a device that has not been updated: the flat route,
            // no "answer" key at all. Nothing migrates those documents, so they are decoded by the
            // same lenient Json the storage datasource uses.
            val json = """
                {
                    "type": "transport",
                    "id": "t1",
                    "transport_type": "TRAIN",
                    "route": {
                        "sections": [
                            {
                                "duration_seconds": 300,
                                "distance_meters": 200.0,
                                "transport_mode": "PEDESTRIAN"
                            },
                            {
                                "duration_seconds": 1500,
                                "distance_meters": 4800.0,
                                "transport_mode": "SUBWAY",
                                "polyline": "abc"
                            }
                        ]
                    }
                }
            """.trimIndent()
            val lenient = Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }

            `when`("decoded and mapped to the domain") {
                val entity = lenient.decodeFromString<StepEntity>(json)
                    .shouldBeInstanceOf<StepEntity.Transport>()
                val step = with(TravelPlanEntityMapper) { entity.toDomain() }
                    .shouldBeInstanceOf<StepDomain.Transport>()

                then("the plan should open rather than throw") {
                    entity.answer.shouldBeNull()
                    step.id shouldBe "t1"
                }

                then("the leg should read back as the journey that shape could hold") {
                    val transit = step.answer.shouldBeInstanceOf<TransportAnswer.Transit>()
                    transit.journey.steps shouldHaveSize 2
                    transit.principalMode shouldBe "SUBWAY"
                }
            }
        }
    }
}
