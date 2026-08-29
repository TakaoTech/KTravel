package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.data.entity.StepEntity
import com.takaotech.ktravel.data.mapper.TravelPlanEntityMapper
import com.takaotech.ktravel.domain.model.TravelPlanEditor.addStepAttachment
import com.takaotech.ktravel.domain.model.TravelPlanEditor.removeStepAttachment
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

/**
 * Checks that the attachment inventory of a Step.Place survives the editor and the entity/UI
 * mappers, and that a document written before `attachments` existed still decodes.
 */
class StepAttachmentTest :
    BehaviorSpec({

        val attachment = AttachmentDomain(
            id = "a1",
            relativePath = "t1/s1/abc.jpg",
            originalName = "photo.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 1234,
        )

        fun planWithPlaceStep(attachments: List<AttachmentDomain> = emptyList()): TravelPlanDomain {
            val step = StepDomain.Place(
                id = "s1",
                name = "Colosseo",
                lat = 41.89,
                lng = 12.49,
                attachments = attachments,
            )
            val day =
                TravelDayDomain(id = "d1", date = LocalDate.fromEpochDays(0), steps = listOf(step))
            return TravelPlanDomain(days = listOf(day))
        }

        given("a plan with a Step.Place") {
            val plan = planWithPlaceStep()

            `when`("addStepAttachment is called with a valid step and day") {
                val updated = plan.addStepAttachment("d1", "s1", attachment)

                then("it should add the attachment to the step inventory") {
                    val step = updated.days[0].steps[0] as StepDomain.Place
                    step.attachments shouldHaveSize 1
                    step.attachments[0] shouldBe attachment
                }
            }

            `when`("addStepAttachment targets an unknown step") {
                val updated = plan.addStepAttachment("d1", "unknown", attachment)

                then("it should return the plan unchanged") {
                    updated shouldBe plan
                }
            }
        }

        given("a plan whose step already has an attachment") {
            val plan = planWithPlaceStep(listOf(attachment))

            `when`("removeStepAttachment is called with the attachment id") {
                val updated = plan.removeStepAttachment("d1", "s1", "a1")

                then("it should remove it from the inventory") {
                    val step = updated.days[0].steps[0] as StepDomain.Place
                    step.attachments.shouldBeEmpty()
                }
            }

            `when`("removeStepAttachment is called with an unknown id") {
                val updated = plan.removeStepAttachment("d1", "s1", "unknown")

                then("it should return the plan unchanged") {
                    updated shouldBe plan
                }
            }
        }

        given("a Step.Place with an attachment") {
            val step = StepDomain.Place(
                id = "s1",
                name = "Trevi",
                lat = 1.0,
                lng = 2.0,
                attachments = listOf(attachment),
            )

            `when`("mapped Domain -> Entity -> Domain") {
                val entity = with(TravelPlanEntityMapper) { step.toEntity() } as StepEntity.Place
                val back = with(TravelPlanEntityMapper) { entity.toDomain() } as StepDomain.Place

                then("the round-trip should preserve the attachments") {
                    entity.attachments shouldHaveSize 1
                    back.attachments shouldBe listOf(attachment)
                }
            }

            `when`("mapped Domain -> UI") {
                val ui = with(TravelPlanUiMapper) { step.toUiStep() } as StepUi.Place

                then("the UI model should carry the attachment") {
                    ui.attachments shouldHaveSize 1
                    ui.attachments[0].relativePath shouldBe attachment.relativePath
                    ui.attachments[0].isImage shouldBe true
                }
            }
        }

        given("a legacy place entity json without the attachments field") {
            val json = Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
            val legacy =
                """{"type":"place","id":"s1","location":"Colosseo","lat":41.89,"lng":12.49}"""

            `when`("decoded into a StepEntity") {
                val entity = json.decodeFromString<StepEntity>(legacy) as StepEntity.Place

                then("attachments default to an empty list (backward compatible)") {
                    entity.attachments.shouldBeEmpty()
                }
            }
        }
    })
