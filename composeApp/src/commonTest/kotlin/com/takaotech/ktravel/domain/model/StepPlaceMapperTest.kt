package com.takaotech.ktravel.domain.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime

/**
 * Verifica la semantica voluta della conversione Place <-> Step (vincoli V1/V2.1).
 */
class StepPlaceMapperTest : BehaviorSpec({

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
            schedule = VisitScheduleDomain(time = LocalTime(10, 30))
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

    given("a step that originally had a schedule") {
        val step = StepDomain.Place(
            id = "s2",
            name = "Trevi",
            lat = 0.0,
            lng = 0.0,
            schedule = VisitScheduleDomain(time = LocalTime(9, 0))
        )

        `when`("converting it to a place") {
            then("it must not throw") {
                // Nessuna eccezione attesa (V1).
                StepPlaceMapper.stepToPlace(step).shouldNotBeNull()
            }
        }
    }
})
