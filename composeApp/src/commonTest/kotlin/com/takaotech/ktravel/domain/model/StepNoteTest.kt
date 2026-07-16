package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.data.entity.StepEntity
import com.takaotech.ktravel.data.mapper.TravelPlanEntityMapper
import com.takaotech.ktravel.domain.model.TravelPlanEditor.updatePlaceNote
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

/**
 * Verifica che il campo `note` di uno Step.Place attraversi correttamente editor, entity e ui mapper.
 */
class StepNoteTest : BehaviorSpec({

    fun planWithPlaceStep(note: String = ""): TravelPlanDomain {
        val step =
            StepDomain.Place(id = "s1", name = "Colosseo", lat = 41.89, lng = 12.49, note = note)
        val day =
            TravelDayDomain(id = "d1", date = LocalDate.fromEpochDays(0), steps = listOf(step))
        return TravelPlanDomain(days = listOf(day))
    }

    given("a plan with a Step.Place") {
        val plan = planWithPlaceStep()

        `when`("updatePlaceNote is called with a valid step and day") {
            val updated = plan.updatePlaceNote("d1", "s1", "# Notes\n- visit")

            then("it should set the note on the target step") {
                val step = updated.days[0].steps[0] as StepDomain.Place
                step.note shouldBe "# Notes\n- visit"
            }
        }

        `when`("updatePlaceNote is called with an unknown step id") {
            val updated = plan.updatePlaceNote("d1", "unknown", "note")

            then("it should return the plan unchanged") {
                updated shouldBe plan
            }
        }

        `when`("updatePlaceNote is called with an unknown day id") {
            val updated = plan.updatePlaceNote("unknown", "s1", "note")

            then("it should return the plan unchanged") {
                updated shouldBe plan
            }
        }
    }

    given("a plan whose step is a Transport") {
        val transport = StepDomain.Transport(
            id = "t1",
            type = TransportType.TRAIN,
            route = com.takaotech.ktravel.domain.routing.model.Route(emptyList())
        )
        val day =
            TravelDayDomain(id = "d1", date = LocalDate.fromEpochDays(0), steps = listOf(transport))
        val plan = TravelPlanDomain(days = listOf(day))

        `when`("updatePlaceNote targets the transport step") {
            val updated = plan.updatePlaceNote("d1", "t1", "note")

            then("it should return the plan unchanged") {
                updated shouldBe plan
            }
        }
    }

    given("a Step.Place with a note") {
        val step =
            StepDomain.Place(id = "s1", name = "Trevi", lat = 1.0, lng = 2.0, note = "**bold**")

        `when`("mapped Domain -> Entity -> Domain") {
            val entity = with(TravelPlanEntityMapper) { step.toEntity() } as StepEntity.Place
            val back = with(TravelPlanEntityMapper) { entity.toDomain() } as StepDomain.Place

            then("the entity should carry the note") {
                entity.note shouldBe "**bold**"
            }
            then("the round-trip should preserve the note") {
                back.note shouldBe "**bold**"
            }
        }

        `when`("mapped Domain -> UI") {
            val ui = with(TravelPlanUiMapper) { step.toUiStep() } as StepUi.Place

            then("the UI model should carry the note") {
                ui.note shouldBe "**bold**"
            }
        }
    }
})
