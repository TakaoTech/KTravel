package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.core.data.mime.MimeType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime

/**
 * Verifica la semantica voluta della conversione Place <-> Step (vincoli V1/V2.1).
 */
class StepPlaceMapperTest :
    BehaviorSpec({

        given("a PlaceDomain in the backlog") {
            val place = PlaceDomain(id = "p1", name = "Colosseo", lat = 41.89, lng = 12.49)

            `when`("placeToStep is called") {
                val step = StepPlaceMapper.placeToStep(place)

                then("it should keep the same id and fields") {
                    step.id shouldBe "p1"
                    step.name shouldBe "Colosseo"
                    step.lat shouldBe 41.89
                    step.lng shouldBe 12.49
                }
                then("the step should not be scheduled yet") {
                    step.schedule shouldBe null
                }
            }
        }

        given("a scheduled StepDomain.Place in the itinerary") {
            val step = StepDomain.Place(
                id = "s1",
                name = "Pantheon",
                lat = 41.89,
                lng = 12.47,
                schedule = VisitScheduleDomain(startTime = LocalTime(10, 30)),
            )

            `when`("stepToPlace is called") {
                val place = StepPlaceMapper.stepToPlace(step)

                then("it should keep id and fields") {
                    place.id shouldBe "s1"
                    place.name shouldBe "Pantheon"
                    place.lat shouldBe 41.89
                    place.lng shouldBe 12.47
                }
            }

            `when`("a full round-trip place -> step -> place is performed") {
                // V1: la perdita dell'orario nel verso Step -> Place è voluta e non deve dare errori.
                val asPlace = StepPlaceMapper.stepToPlace(step)
                val backToStep = StepPlaceMapper.placeToStep(asPlace)

                then("the place identity is preserved") {
                    asPlace.id shouldBe step.id
                    asPlace.name shouldBe step.name
                }
                then("the schedule is intentionally dropped when leaving the itinerary") {
                    backToStep.schedule shouldBe null
                }
            }
        }

        given("a step in the itinerary with notes and attachments") {
            val attachment = AttachmentDomain(
                id = "att-1",
                relativePath = "travel-1/s3/photo.jpg",
                originalName = "photo.jpg",
                mimeType = MimeType("image/jpeg"),
                sizeBytes = 2048,
            )
            val step = StepDomain.Place(
                id = "s3",
                name = "Fori Imperiali",
                lat = 41.89,
                lng = 12.48,
                schedule = VisitScheduleDomain(startTime = LocalTime(14, 0)),
                note = "Bring the ticket",
                attachments = listOf(attachment),
            )

            `when`("it is sent back to the backlog") {
                val place = StepPlaceMapper.stepToPlace(step)

                then("the note follows the place") {
                    place.note shouldBe "Bring the ticket"
                }
                then("the file inventory follows the place") {
                    place.attachments shouldBe listOf(attachment)
                }
                then("only the schedule is dropped") {
                    StepPlaceMapper.placeToStep(place).schedule shouldBe null
                }
            }

            `when`("a full round-trip step -> place -> step is performed") {
                val backToStep = StepPlaceMapper.placeToStep(StepPlaceMapper.stepToPlace(step))

                then("the step is the one that left, minus the schedule") {
                    backToStep shouldBe step.copy(schedule = null)
                }
                then("the attachment paths still point at the same id") {
                    // The relative path holds the step id: regenerating it would strand the files.
                    backToStep.attachments.single().relativePath shouldBe "travel-1/s3/photo.jpg"
                }
            }
        }

        given("a place in the backlog with notes and attachments") {
            val attachment = AttachmentDomain(
                id = "att-2",
                relativePath = "travel-1/p2/map.pdf",
                originalName = "map.pdf",
                mimeType = MimeType("application/pdf"),
                sizeBytes = 512,
            )
            val place = PlaceDomain(
                id = "p2",
                name = "Villa Borghese",
                lat = 41.91,
                lng = 12.49,
                note = "Book in advance",
                attachments = listOf(attachment),
            )

            `when`("it enters the itinerary") {
                val step = StepPlaceMapper.placeToStep(place)

                then("the note reaches the step") {
                    step.note shouldBe "Book in advance"
                }
                then("the file inventory reaches the step") {
                    step.attachments shouldBe listOf(attachment)
                }
            }
        }

        given("a step that originally had a schedule") {
            val step = StepDomain.Place(
                id = "s2",
                name = "Trevi",
                lat = 0.0,
                lng = 0.0,
                schedule = VisitScheduleDomain(startTime = LocalTime(9, 0)),
            )

            `when`("converting it to a place") {
                then("it must not throw") {
                    // Nessuna eccezione attesa (V1).
                    StepPlaceMapper.stepToPlace(step).shouldNotBeNull()
                }
            }
        }
    })
