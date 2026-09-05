package com.takaotech.ktravel.ui.shared.format

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.nacular.measured.units.Length
import io.nacular.measured.units.times

/**
 * A distance has to be printed in the unit it is labelled with.
 *
 * The previous formatting converted to kilometres and then printed the result as metres, so 240
 * metres read "0.24 m" and 99 kilometres read "99417.00 m" — numbers that look like a unit bug to a
 * user and like a data bug to whoever reads the screenshot.
 */
class DistanceFormattingTest : BehaviorSpec() {

    init {
        given("a distance below a kilometre") {
            `when`("formatted") {
                val formatted = (240.0 * Length.meters).formatDistance()

                then("it should read in whole metres") {
                    formatted shouldBe "240 m"
                }
            }
        }

        given("a distance of exactly a kilometre") {
            `when`("formatted") {
                val formatted = (1000.0 * Length.meters).formatDistance()

                then("it should already read in kilometres") {
                    formatted shouldBe "1.0 km"
                }
            }
        }

        given("a long distance") {
            `when`("formatted") {
                val formatted = (99_417.0 * Length.meters).formatDistance()

                then("it should read in kilometres and not in metres") {
                    formatted shouldBe "99.4 km"
                }
            }
        }
    }
}
