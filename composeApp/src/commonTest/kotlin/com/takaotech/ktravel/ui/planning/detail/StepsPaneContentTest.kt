package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.Route
import com.takaotech.ktravel.domain.routing.model.RouteSection
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.detail.buildStepRows
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalTestApi::class)
class StepsPaneContentTest : BehaviorSpec() {

    private val placeA = StepUi.Place(id = "step-a", name = "Tokyo Tower", lat = 0.0, lng = 0.0)
    private val placeB =
        StepUi.Place(id = "step-b", name = "Shibuya Crossing", lat = 0.0, lng = 0.0)
    private val transport = StepUi.Transport(
        id = "step-t",
        type = TransportType.TRAIN,
        route = Route(
            sections = listOf(
                RouteSection(
                    summary = RouteSummary(
                        durationSeconds = 30.minutes,
                        distanceMeters = 1000
                    )
                )
            )
        )
    )

    init {
        given("StepsPaneContent with empty rows") {
            then("the empty state should be visible") {
                runComposeUiTest {
                    setContent {
                        StepsPaneContent(
                            rows = persistentListOf(),
                            onNavigationBackClick = {},
                            onOpenBacklogClick = {},
                            onStepClick = {},
                            onDeleteStepClick = {},
                            onMoveStepUpClick = {},
                            onMoveStepDownClick = {},
                            onAddTransportClick = { _, _ -> }
                        )
                    }
                    onNodeWithTag(StepsPaneTestTags.EMPTY).assertIsDisplayed()
                }
            }
        }

        given("StepsPaneContent with a place and a transport step") {
            val rows = buildStepRows(listOf(placeA, transport, placeB))

            then("the place names should be displayed") {
                runComposeUiTest {
                    setContent {
                        StepsPaneContent(
                            rows = rows,
                            onNavigationBackClick = {},
                            onOpenBacklogClick = {},
                            onStepClick = {},
                            onDeleteStepClick = {},
                            onMoveStepUpClick = {},
                            onMoveStepDownClick = {},
                            onAddTransportClick = { _, _ -> }
                        )
                    }
                    onNodeWithText(placeA.name).assertIsDisplayed()
                    onNodeWithText(placeB.name).assertIsDisplayed()
                }
            }

            then("the transport row should show the aggregated duration") {
                runComposeUiTest {
                    setContent {
                        StepsPaneContent(
                            rows = rows,
                            onNavigationBackClick = {},
                            onOpenBacklogClick = {},
                            onStepClick = {},
                            onDeleteStepClick = {},
                            onMoveStepUpClick = {},
                            onMoveStepDownClick = {},
                            onAddTransportClick = { _, _ -> }
                        )
                    }
                    onNodeWithText("Duration 30m").assertIsDisplayed()
                }
            }
        }

        given("StepsPaneContent with two adjacent places") {
            val rows = buildStepRows(listOf(placeA, placeB))

            `when`("the add-transport slot is clicked") {
                then("onAddTransportClick should be called with the surrounding step ids") {
                    var clicked: Pair<String, String>? = null
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = rows,
                                onNavigationBackClick = {},
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { start, end -> clicked = start to end }
                            )
                        }
                        onNodeWithTag(
                            StepsPaneTestTags.addTransportTag(placeA.id, placeB.id)
                        ).performClick()
                    }
                    clicked shouldBe (placeA.id to placeB.id)
                }
            }
        }

        given("StepsPaneContent with a single place step") {
            val rows = buildStepRows(listOf(placeA))

            `when`("the delete button is clicked") {
                then("onDeleteStepClick should be called with that step") {
                    var deleted: StepUi? = null
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = rows,
                                onNavigationBackClick = {},
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onDeleteStepClick = { deleted = it },
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> }
                            )
                        }
                        onNodeWithContentDescription("Delete step").performClick()
                    }
                    deleted shouldBe placeA
                }
            }

            `when`("the move-up button is clicked") {
                then("onMoveStepUpClick should be called with the step id") {
                    var movedUp: String? = null
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = rows,
                                onNavigationBackClick = {},
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = { movedUp = it },
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> }
                            )
                        }
                        onNodeWithContentDescription("Move step up").performClick()
                    }
                    movedUp shouldBe placeA.id
                }
            }

            `when`("the move-down button is clicked") {
                then("onMoveStepDownClick should be called with the step id") {
                    var movedDown: String? = null
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = rows,
                                onNavigationBackClick = {},
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = { movedDown = it },
                                onAddTransportClick = { _, _ -> }
                            )
                        }
                        onNodeWithContentDescription("Move step down").performClick()
                    }
                    movedDown shouldBe placeA.id
                }
            }
        }

        given("StepsPaneContent top bar actions") {
            `when`("the back button is clicked") {
                then("onNavigationBackClick should be invoked") {
                    var backClicked = false
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = persistentListOf(),
                                onNavigationBackClick = { backClicked = true },
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> }
                            )
                        }
                        onNodeWithTag(StepsPaneTestTags.BACK_BUTTON).performClick()
                    }
                    backClicked shouldBe true
                }
            }

            `when`("the open-backlog button is clicked") {
                then("onOpenBacklogClick should be invoked") {
                    var openClicked = false
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = persistentListOf(),
                                onNavigationBackClick = {},
                                onOpenBacklogClick = { openClicked = true },
                                onStepClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> }
                            )
                        }
                        onNodeWithTag(StepsPaneTestTags.OPEN_BACKLOG_BUTTON).performClick()
                    }
                    openClicked shouldBe true
                }
            }
        }
    }
}
