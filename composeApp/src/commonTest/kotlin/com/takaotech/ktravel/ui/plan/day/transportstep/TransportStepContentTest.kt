@file:OptIn(ExperimentalTestApi::class, ExperimentalTime::class)

package com.takaotech.ktravel.ui.plan.day.transportstep

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.TransitAgency
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransitStop
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.presentation.plan.day.StepNotesEvent
import com.takaotech.ktravel.presentation.plan.day.StepNotesUiState
import com.takaotech.ktravel.presentation.plan.day.TransportStepUi
import com.takaotech.ktravel.testutil.roadAnswer
import com.takaotech.ktravel.testutil.summaryOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The screen draws two very different things — a list of manoeuvres or a timeline of departures —
 * and now draws each from the shape it was answered in. What is pinned here is that the right one
 * is chosen, that a journey arrives with its line and its stops intact, and that what the plan does
 * not know stays off the screen.
 */
class TransportStepContentTest : BehaviorSpec() {

    private val turnInstruction = "Turn right onto Via Roma"

    private val roadAnswer = roadAnswer(
        duration = 14.minutes,
        metres = 10400.0,
        actions = listOf(
            RouteAction(
                action = "turn",
                durationSeconds = 45.seconds,
                distanceMeters = 320.0 * Length.meters,
                instruction = turnInstruction,
            ),
        ),
    )

    private val lineName = "M1"
    private val boardingStop = "Duomo"
    private val alightingStop = "Sesto FS"

    /**
     * A journey with everything a departure board knows, which is what has to survive to the
     * screen.
     */
    private val transitAnswer = TransportAnswer.Transit(
        TransitJourney(
            summary = summaryOf(30.minutes, 9000.0),
            steps = listOf(
                TransitStep.Walk(summary = summaryOf(8.minutes, 600.0)),
                TransitStep.Ride(
                    summary = summaryOf(22.minutes, 8400.0),
                    line = TransitLine(mode = "SUBWAY", name = lineName, color = "#D52B1E", headsign = "Sesto"),
                    agency = TransitAgency(name = "ATM"),
                    boarding = TransitStop(location = RouteLocation(45.46, 9.19), name = boardingStop),
                    alighting = TransitStop(location = RouteLocation(45.53, 9.24), name = alightingStop),
                ),
            ),
        ),
    )

    private fun detail(answer: TransportAnswer, calculatedAt: Instant? = null) = TransportStepUi(
        type = if (answer is TransportAnswer.Routing) TransportType.CAR else TransportType.TRAIN,
        fromName = "Binasco",
        toName = "Assago",
        answer = answer,
        totalDuration = answer.summary.durationSeconds,
        totalDistance = answer.summary.distance,
        calculatedAt = calculatedAt,
    )

    @Composable
    private fun Content(
        answer: TransportAnswer,
        calculatedAt: Instant? = null,
        canRecalculate: Boolean = true,
        onRecalculate: () -> Unit = {},
        onNotesEvent: (StepNotesEvent) -> Unit = {},
    ) {
        TransportStepContent(
            transport = detail(answer, calculatedAt),
            notes = StepNotesUiState(),
            canRecalculate = canRecalculate,
            onBack = {},
            onRecalculate = onRecalculate,
            onNotesEvent = onNotesEvent,
        )
    }

    init {
        given("a saved road route") {
            then("it should draw the manoeuvres") {
                runComposeUiTest {
                    setContent { Content(roadAnswer) }

                    onNodeWithTag(TransportStepTestTags.SEGMENT_HEAD).assertIsDisplayed()
                    onNodeWithTag(TransportStepTestTags.MANOEUVRES).assertIsDisplayed()
                    onNodeWithText(turnInstruction).assertIsDisplayed()
                }
            }

            then("the computation time should be hidden when the plan does not record it") {
                runComposeUiTest {
                    setContent { Content(roadAnswer) }

                    onNodeWithTag(TransportStepTestTags.METRICS).assertIsDisplayed()
                    onNodeWithTag(TransportStepTestTags.CALCULATED_AT).assertDoesNotExist()
                }
            }

            then("the computation time should be shown when it is recorded") {
                runComposeUiTest {
                    setContent { Content(roadAnswer, calculatedAt = Instant.parse("2026-05-30T09:38:00Z")) }

                    onNodeWithTag(TransportStepTestTags.CALCULATED_AT, useUnmergedTree = true)
                        .assertIsDisplayed()
                }
            }
        }

        given("a saved journey on scheduled services") {
            then("it should draw the timeline instead of the manoeuvres") {
                runComposeUiTest {
                    setContent { Content(transitAnswer) }

                    onNodeWithTag(TransportStepTestTags.TRANSIT_TIMELINE).assertIsDisplayed()
                    onNodeWithTag(TransportStepTestTags.MANOEUVRES).assertDoesNotExist()
                }
            }

            then("the line and the stops it calls at should be on screen") {
                runComposeUiTest {
                    setContent { Content(transitAnswer) }

                    // The whole point of saving a journey as a journey: these are exactly the
                    // things the flattened shape used to drop.
                    onNodeWithText(lineName, substring = true).assertIsDisplayed()
                    onNodeWithText(boardingStop, substring = true).assertIsDisplayed()
                    onNodeWithText(alightingStop, substring = true).assertIsDisplayed()
                }
            }
        }

        given("a leg that has lost one of the places it joined") {
            then("the recompute action should be disabled") {
                runComposeUiTest {
                    setContent { Content(roadAnswer, canRecalculate = false) }

                    onNodeWithTag(TransportStepTestTags.RECALCULATE).assertIsNotEnabled()
                }
            }
        }

        given("a leg between two places") {
            then("the pencil should ask for a recomputation") {
                var recalculated = false
                runComposeUiTest {
                    setContent { Content(roadAnswer, onRecalculate = { recalculated = true }) }

                    onNodeWithTag(TransportStepTestTags.RECALCULATE).assertIsEnabled().performClick()
                }
                recalculated shouldBe true
            }

            then("the edit toggle should open the note editor") {
                var event: StepNotesEvent? = null
                runComposeUiTest {
                    setContent { Content(roadAnswer, onNotesEvent = { event = it }) }

                    onNodeWithTag(TransportStepTestTags.NOTES.editToggle).performClick()
                }
                event shouldBe StepNotesEvent.ToggleEdit(true)
            }
        }
    }
}
