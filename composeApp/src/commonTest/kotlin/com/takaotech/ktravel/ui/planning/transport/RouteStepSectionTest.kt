package com.takaotech.ktravel.ui.planning.transport

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteSection
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.navigator.api.geometry.PolylineEncoderDecoder
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlin.time.Duration.Companion.seconds

/**
 * The map cannot be exercised on the JVM, so what is covered here is the contract the map depends
 * on: a click on a step resolves the section polyline at the action offset and reports that
 * coordinate. Rows are matched on [RouteAction.instruction], a plain string rather than a
 * `stringResource`, so the query does not depend on the host machine's locale.
 */
@OptIn(ExperimentalTestApi::class)
class RouteStepSectionTest : BehaviorSpec() {

    private val vertices = listOf(
        PolylineEncoderDecoder.LatLngZ(lat = 41.9028, lng = 12.4964),
        PolylineEncoderDecoder.LatLngZ(lat = 43.7696, lng = 11.2558),
        PolylineEncoderDecoder.LatLngZ(lat = 45.4642, lng = 9.19),
    )

    private val polyline = PolylineEncoderDecoder.encode(
        vertices,
        5,
        PolylineEncoderDecoder.ThirdDimension.ABSENT,
        0,
    )

    private val turnInstruction = "Turn right onto Via Roma"

    private fun action(offset: Int?) = RouteAction(
        action = "turn",
        durationSeconds = 45.seconds,
        distanceMeters = 320 * Length.meters,
        instruction = turnInstruction,
        offset = offset,
        direction = "right",
        severity = "normal",
    )

    private fun section(polyline: String?, offset: Int?) = RouteSection(
        summary = RouteSummary(durationSeconds = 600.seconds, distanceMeters = 5000),
        actions = listOf(action(offset)),
        polyline = polyline,
    )

    init {
        given("a section with a polyline and an action pointing at a vertex") {
            `when`("the step is clicked") {
                then("the coordinate at that offset should be reported") {
                    runComposeUiTest {
                        var clicked: PolylineEncoderDecoder.LatLngZ? = null

                        setContent {
                            RouteStepSection(
                                section = section(polyline = polyline, offset = 2),
                                onActionClick = { clicked = it },
                            )
                        }

                        onNodeWithText(turnInstruction).performClick()

                        val coordinate = clicked
                        coordinate.shouldNotBeNull()
                        coordinate.lat shouldBe (vertices[2].lat plusOrMinus TOLERANCE)
                        coordinate.lng shouldBe (vertices[2].lng plusOrMinus TOLERANCE)
                    }
                }
            }
        }

        given("a section without a polyline") {
            then("the step should not be clickable") {
                runComposeUiTest {
                    var clicked: PolylineEncoderDecoder.LatLngZ? = null

                    setContent {
                        RouteStepSection(
                            section = section(polyline = null, offset = 2),
                            onActionClick = { clicked = it },
                        )
                    }

                    onNodeWithText(turnInstruction).assertHasNoClickAction()
                    clicked shouldBe null
                }
            }
        }

        given("a section whose action carries no offset") {
            then("the step should not be clickable") {
                runComposeUiTest {
                    setContent {
                        RouteStepSection(
                            section = section(polyline = polyline, offset = null),
                            onActionClick = {},
                        )
                    }

                    onNodeWithText(turnInstruction).assertHasNoClickAction()
                }
            }
        }

        given("an offset outside the polyline") {
            `when`("the step is clicked") {
                then("the click should be swallowed rather than crash the screen") {
                    runComposeUiTest {
                        var clicked: PolylineEncoderDecoder.LatLngZ? = null

                        setContent {
                            RouteStepSection(
                                section = section(polyline = polyline, offset = 99),
                                onActionClick = { clicked = it },
                            )
                        }

                        onNodeWithText(turnInstruction).assertHasClickAction()
                        onNodeWithText(turnInstruction).performClick()

                        clicked shouldBe null
                    }
                }
            }
        }
    }

    private companion object {
        /** The polyline is encoded with five decimals, so a round trip is exact well within this. */
        const val TOLERANCE = 1e-6
    }
}
