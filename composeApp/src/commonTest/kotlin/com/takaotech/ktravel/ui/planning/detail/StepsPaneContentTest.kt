package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.VisitScheduleUi
import com.takaotech.ktravel.presentation.planning.detail.buildStepRows
import com.takaotech.ktravel.testutil.roadAnswer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_cd_close_backlog
import ktravel.composeapp.generated.resources.planning_detail_cd_open_backlog
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.minutes

/**
 * Icon buttons are queried by test tag rather than by content description, and the transport row is
 * asserted on the interpolated duration only: labels come from
 * [org.jetbrains.compose.resources.stringResource], which resolves in the JVM default locale, so
 * matching their wording would tie the test to whichever language the host machine runs in.
 */
@OptIn(ExperimentalTestApi::class)
class StepsPaneContentTest : BehaviorSpec() {

    private val placeA = StepUi.Place(id = "step-a", name = "Tokyo Tower", lat = 0.0, lng = 0.0)
    private val placeB =
        StepUi.Place(id = "step-b", name = "Shibuya Crossing", lat = 0.0, lng = 0.0)
    private val transport = StepUi.Transport(
        id = "step-t",
        type = TransportType.TRAIN,
        answer = roadAnswer(duration = 30.minutes),
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
                            onTransportClick = {},
                            onDeleteStepClick = {},
                            onMoveStepUpClick = {},
                            onMoveStepDownClick = {},
                            onAddTransportClick = { _, _ -> },
                            onSetArrivalTime = { _, _ -> },
                            onSetDepartureTime = { _, _ -> },
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
                            onTransportClick = {},
                            onDeleteStepClick = {},
                            onMoveStepUpClick = {},
                            onMoveStepDownClick = {},
                            onAddTransportClick = { _, _ -> },
                            onSetArrivalTime = { _, _ -> },
                            onSetDepartureTime = { _, _ -> },
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
                            onTransportClick = {},
                            onDeleteStepClick = {},
                            onMoveStepUpClick = {},
                            onMoveStepDownClick = {},
                            onAddTransportClick = { _, _ -> },
                            onSetArrivalTime = { _, _ -> },
                            onSetDepartureTime = { _, _ -> },
                        )
                    }
                    // The route sections aggregate to "30m"; the surrounding wording comes from the
                    // string resource and is intentionally not asserted. The row itself is
                    // clickable, so its children are merged into it and only the unmerged tree
                    // still carries the tag.
                    onNodeWithTag(StepsPaneTestTags.TRANSPORT_DURATION, useUnmergedTree = true)
                        .assertIsDisplayed()
                        .assertTextContains("30m", substring = true)
                }
            }

            `when`("the transport row is clicked") {
                then("onTransportClick should be called with the transport step id") {
                    var clicked: String? = null
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = rows,
                                onNavigationBackClick = {},
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onTransportClick = { clicked = it },
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                            )
                        }

                        onNodeWithTag(StepsPaneTestTags.TRANSPORT_DURATION, useUnmergedTree = true)
                            .onParent()
                            .performClick()

                        clicked shouldBe transport.id
                    }
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
                                onTransportClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { start, end -> clicked = start to end },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                            )
                        }
                        onNodeWithTag(
                            StepsPaneTestTags.addTransportTag(placeA.id, placeB.id),
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
                                onTransportClick = {},
                                onDeleteStepClick = { deleted = it },
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                            )
                        }
                        onNodeWithTag(
                            StepsPaneTestTags.deleteStepTag(placeA.id),
                        ).performClick()
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
                                onTransportClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = { movedUp = it },
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                            )
                        }
                        onNodeWithTag(
                            StepsPaneTestTags.moveStepUpTag(placeA.id),
                        ).performClick()
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
                                onTransportClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = { movedDown = it },
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                            )
                        }
                        onNodeWithTag(
                            StepsPaneTestTags.moveStepDownTag(placeA.id),
                        ).performClick()
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
                                onTransportClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
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
                                onTransportClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                            )
                        }
                        onNodeWithTag(StepsPaneTestTags.OPEN_BACKLOG_BUTTON).performClick()
                    }
                    openClicked shouldBe true
                }
            }
        }

        given("StepsPaneContent with a scheduled place followed by another one") {
            val scheduled = placeA.copy(
                schedule = VisitScheduleUi(
                    startTime = LocalTime(9, 30),
                    endTime = LocalTime(11, 0),
                ),
            )
            val rows = buildStepRows(listOf(scheduled, placeB))

            then("the chips of the place should show its arrival and departure") {
                runComposeUiTest {
                    setContent {
                        StepsPaneContent(
                            rows = rows,
                            onNavigationBackClick = {},
                            onOpenBacklogClick = {},
                            onStepClick = {},
                            onTransportClick = {},
                            onDeleteStepClick = {},
                            onMoveStepUpClick = {},
                            onMoveStepDownClick = {},
                            onAddTransportClick = { _, _ -> },
                            onSetArrivalTime = { _, _ -> },
                            onSetDepartureTime = { _, _ -> },
                        )
                    }
                    // Clock strings are built by formatClock() and carry no locale, unlike the
                    // labels around them.
                    onNodeWithTag(StepsPaneTestTags.arrivalChipTag(scheduled.id))
                        .assertTextEquals("09:30")
                    onNodeWithTag(StepsPaneTestTags.departureChipTag(scheduled.id))
                        .assertTextEquals("11:00")
                }
            }

            then("the last place should carry no chips, being the destination") {
                runComposeUiTest {
                    setContent {
                        StepsPaneContent(
                            rows = rows,
                            onNavigationBackClick = {},
                            onOpenBacklogClick = {},
                            onStepClick = {},
                            onTransportClick = {},
                            onDeleteStepClick = {},
                            onMoveStepUpClick = {},
                            onMoveStepDownClick = {},
                            onAddTransportClick = { _, _ -> },
                            onSetArrivalTime = { _, _ -> },
                            onSetDepartureTime = { _, _ -> },
                        )
                    }
                    onNodeWithTag(StepsPaneTestTags.arrivalChipTag(placeB.id)).assertDoesNotExist()
                    onNodeWithTag(StepsPaneTestTags.departureChipTag(placeB.id))
                        .assertDoesNotExist()
                }
            }

            `when`("the arrival chip is clicked") {
                then("the time picker should open") {
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = rows,
                                onNavigationBackClick = {},
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onTransportClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                            )
                        }
                        onNode(isDialog()).assertDoesNotExist()
                        onNodeWithTag(StepsPaneTestTags.arrivalChipTag(scheduled.id)).performClick()
                        onNode(isDialog()).assertExists()
                    }
                }
            }
        }

        given("StepsPaneContent backlog toggle") {
            // Both sides read the same resource in the same locale, so unlike matching wording
            // this stays valid whichever language the host machine runs in.
            `when`("the backlog pane is open") {
                then("the toggle should describe itself as closing the pane") {
                    val expected = getString(Res.string.planning_detail_cd_close_backlog)
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = persistentListOf(),
                                onNavigationBackClick = {},
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onTransportClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                                backlogOpen = true,
                            )
                        }
                        onNodeWithTag(StepsPaneTestTags.OPEN_BACKLOG_BUTTON)
                            .assertContentDescriptionEquals(expected)
                    }
                }
            }

            `when`("the backlog pane is closed") {
                then("the toggle should describe itself as opening the pane") {
                    val expected = getString(Res.string.planning_detail_cd_open_backlog)
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = persistentListOf(),
                                onNavigationBackClick = {},
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onTransportClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                                backlogOpen = false,
                            )
                        }
                        onNodeWithTag(StepsPaneTestTags.OPEN_BACKLOG_BUTTON)
                            .assertContentDescriptionEquals(expected)
                    }
                }
            }
        }

        given("StepsPaneContent top bar title") {
            `when`("a day date is provided") {
                then("the title should show the formatted day") {
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = persistentListOf(),
                                onNavigationBackClick = {},
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onTransportClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                                dayDate = LocalDate(2026, 5, 18),
                            )
                        }
                        onNodeWithTag(StepsPaneTestTags.TITLE)
                            .assertIsDisplayed()
                            .assertTextEquals("Monday 18-05-2026")
                    }
                }
            }

            `when`("no day date is provided") {
                then("the title should be absent") {
                    runComposeUiTest {
                        setContent {
                            StepsPaneContent(
                                rows = persistentListOf(),
                                onNavigationBackClick = {},
                                onOpenBacklogClick = {},
                                onStepClick = {},
                                onTransportClick = {},
                                onDeleteStepClick = {},
                                onMoveStepUpClick = {},
                                onMoveStepDownClick = {},
                                onAddTransportClick = { _, _ -> },
                                onSetArrivalTime = { _, _ -> },
                                onSetDepartureTime = { _, _ -> },
                            )
                        }
                        onNodeWithTag(StepsPaneTestTags.TITLE).assertDoesNotExist()
                    }
                }
            }
        }
    }
}
