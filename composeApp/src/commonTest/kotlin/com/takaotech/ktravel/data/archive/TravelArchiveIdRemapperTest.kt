package com.takaotech.ktravel.data.archive

import com.takaotech.ktravel.data.entity.StepEntity
import com.takaotech.ktravel.domain.model.AttachmentReference
import com.takaotech.ktravel.testutil.testAppLogger
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TravelArchiveIdRemapperTest :
    BehaviorSpec({

        given("a plan imported as a duplicate") {
            val original = ArchiveTestFixtures.plan()
            val ids = generateSequence(1) { it + 1 }.map { "new-$it" }.iterator()
            val remapped = TravelArchiveIdRemapper(testAppLogger()).remap(
                plan = original,
                newTravelId = "travel-copy",
                newId = { ids.next() },
            )

            `when`("the ids are regenerated") {
                then("the travel id is the new one") {
                    remapped.plan.id shouldBe "travel-copy"
                }

                then("no day, step, place or attachment keeps its original id") {
                    val originalIds = original.allIds()
                    remapped.plan.allIds().none { it in originalIds } shouldBe true
                }

                then("the plan structure is preserved") {
                    remapped.plan.days.size shouldBe original.days.size
                    remapped.plan.days.first().steps.size shouldBe original.days.first().steps.size
                    remapped.plan.places.size shouldBe original.places.size
                }
            }

            `when`("the attachment paths are rebuilt") {
                val newPlaceStep = remapped.plan.days.first().steps
                    .filterIsInstance<StepEntity.Place>()
                    .first()

                then("each path moves under the new travel and step folders, keeping the file name") {
                    newPlaceStep.attachments.map { it.relativePath } shouldBe listOf(
                        "travel-copy/${newPlaceStep.id}/photo.jpg",
                        "travel-copy/${newPlaceStep.id}/guide.pdf",
                    )
                }

                then("the mapping covers every attachment") {
                    remapped.attachmentPathMapping.keys shouldContainExactlyInAnyOrder listOf(
                        ArchiveTestFixtures.PHOTO_PATH,
                        ArchiveTestFixtures.DOC_PATH,
                    )
                }

                then("the note references point to the new paths") {
                    AttachmentReference.extractRelativePaths(newPlaceStep.note) shouldBe
                        newPlaceStep.attachments.map { it.relativePath }
                }

                then("the note keeps its non-attachment content") {
                    newPlaceStep.note.contains("[link](https://example.com)") shouldBe true
                }

                then("no old path survives in the note") {
                    newPlaceStep.note.contains(ArchiveTestFixtures.PHOTO_PATH) shouldBe false
                }
            }

            `when`("the transport step is remapped") {
                val transport = remapped.plan.days.first().steps
                    .filterIsInstance<StepEntity.Transport>()
                    .first()

                then("its id changes but the answer is untouched") {
                    transport.id shouldNotBe "step-2"
                    transport.answer shouldBe (
                        original.days.first().steps
                            .filterIsInstance<StepEntity.Transport>().first().answer
                        )
                }
            }
        }

        given("a plan whose backlog place carries a note and a file") {
            val original = with(ArchiveTestFixtures) { ArchiveTestFixtures.plan().withBacklogAttachment() }
            val ids = generateSequence(1) { it + 1 }.map { "new-$it" }.iterator()
            val remapped = TravelArchiveIdRemapper(testAppLogger()).remap(
                plan = original,
                newTravelId = "travel-copy",
                newId = { ids.next() },
            )

            `when`("the place is remapped") {
                val place = remapped.plan.places.single()

                then("its id is regenerated") {
                    place.id shouldNotBe ArchiveTestFixtures.BACKLOG_PLACE_ID
                }

                then("its file moves under the new travel and place folders, keeping the file name") {
                    place.attachments.single().relativePath shouldBe "travel-copy/${place.id}/ticket.pdf"
                }

                then("the mapping covers the backlog path as well") {
                    remapped.attachmentPathMapping.keys shouldContain ArchiveTestFixtures.BACKLOG_PATH
                }

                then("the note reference points to the new path") {
                    AttachmentReference.extractRelativePaths(place.note) shouldBe
                        place.attachments.map { it.relativePath }
                }

                then("the note keeps its non-attachment content") {
                    place.note.contains("Ferry at 8am") shouldBe true
                }
            }
        }
    })

private fun com.takaotech.ktravel.data.entity.TravelPlanEntity.allIds(): List<String> = days.map { it.id } +
    days.flatMap { day -> day.steps.map { it.id } } +
    days.flatMap { day -> day.places.map { it.id } } +
    places.map { it.id } +
    days.flatMap { day ->
        day.steps.filterIsInstance<StepEntity.Place>().flatMap { step ->
            step.attachments.map { it.id }
        }
    } +
    places.flatMap { place -> place.attachments.map { it.id } } +
    days.flatMap { day -> day.places.flatMap { place -> place.attachments.map { it.id } } }
