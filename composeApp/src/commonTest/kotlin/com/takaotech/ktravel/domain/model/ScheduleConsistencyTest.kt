package com.takaotech.ktravel.domain.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime

/**
 * Verifica che la coerenza temporale dell'itinerario sia *segnalata* e mai imposta (vincolo V2).
 */
class ScheduleConsistencyTest : BehaviorSpec({

    fun placeAt(id: String, hour: Int, minute: Int = 0) = StepDomain.Place(
        id = id,
        name = id,
        lat = 0.0,
        lng = 0.0,
        schedule = VisitScheduleDomain(time = LocalTime(hour, minute))
    )

    given("an itinerary with strictly increasing times") {
        val steps = listOf(placeAt("a", 9), placeAt("b", 11), placeAt("c", 14))

        `when`("scheduleInconsistencies is called") {
            val warnings = steps.scheduleInconsistencies()

            then("there should be no warnings") {
                warnings.shouldBeEmpty()
            }
        }
    }

    given("an itinerary with an out-of-order time") {
        val steps = listOf(placeAt("a", 9), placeAt("b", 14), placeAt("c", 11))

        `when`("scheduleInconsistencies is called") {
            val warnings = steps.scheduleInconsistencies()

            then("it should not throw and should signal the offending step") {
                warnings.size shouldBe 1
                warnings[0].stepId shouldBe "c"
                warnings[0].previousStepId shouldBe "b"
                warnings[0].reason shouldBe ScheduleWarning.Reason.OUT_OF_ORDER
            }
        }
    }

    given("an itinerary where some steps have no schedule") {
        val steps = listOf(
            placeAt("a", 9),
            StepDomain.Place(id = "b", name = "b", lat = 0.0, lng = 0.0, schedule = null),
            placeAt("c", 11)
        )

        `when`("scheduleInconsistencies is called") {
            val warnings = steps.scheduleInconsistencies()

            then("unscheduled steps are ignored and order stays consistent") {
                warnings.shouldBeEmpty()
            }
        }
    }
})
