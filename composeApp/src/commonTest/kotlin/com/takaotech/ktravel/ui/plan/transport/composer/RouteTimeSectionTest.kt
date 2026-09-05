package com.takaotech.ktravel.ui.plan.transport.composer

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaotech.ktravel.domain.routing.RouteTimeChoice
import com.takaotech.ktravel.presentation.plan.transport.RouteTimeMode
import com.takaotech.ktravel.ui.plan.transport.component.TransportPlanningTestTags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month

private val DAY = LocalDate(year = 2026, month = Month.MAY, day = 18)

private val TEN_THIRTY = LocalTime(hour = 10, minute = 30)

/**
 * The selector, without a theme or a Circuit around it.
 *
 * Segments are found by tag rather than by label: the host resolves strings in whatever locale it
 * runs under, and the three labels are the one thing on this block that is translated.
 */
@OptIn(ExperimentalTestApi::class)
class RouteTimeSectionTest :
    BehaviorSpec({

        given("a leg that leaves now") {
            `when`("the traveller asks to leave at an hour instead") {
                then("the mode is reported and nothing is chosen on the screen's behalf") {
                    runComposeUiTest {
                        var requested: RouteTimeMode? = null

                        setContent {
                            RouteTimeSection(
                                choice = RouteTimeChoice.Now,
                                dayDate = DAY,
                                supportsArriveBy = true,
                                onModeChange = { requested = it },
                                onTimeChange = {},
                            )
                        }

                        onNodeWithTag(TransportPlanningTestTags.TIME_NOW).assertIsSelected()
                        onNodeWithTag(TransportPlanningTestTags.TIME_DEPART_AT).performClick()

                        requested shouldBe RouteTimeMode.DEPART_AT
                    }
                }
            }

            then("no hour is shown, because leaving now has none") {
                runComposeUiTest {
                    setContent {
                        RouteTimeSection(
                            choice = RouteTimeChoice.Now,
                            dayDate = DAY,
                            supportsArriveBy = true,
                            onModeChange = {},
                            onTimeChange = {},
                        )
                    }

                    onNodeWithTag(TransportPlanningTestTags.TIME_VALUE).assertDoesNotExist()
                }
            }
        }

        given("a leg with a departure fixed at an hour") {
            then("that hour is shown on the day the leg belongs to") {
                runComposeUiTest {
                    setContent {
                        RouteTimeSection(
                            choice = RouteTimeChoice.DepartAt(TEN_THIRTY),
                            dayDate = DAY,
                            supportsArriveBy = true,
                            onModeChange = {},
                            onTimeChange = {},
                        )
                    }

                    onNodeWithTag(TransportPlanningTestTags.TIME_DEPART_AT).assertIsSelected()
                    onNodeWithTag(TransportPlanningTestTags.TIME_VALUE).assertIsDisplayed()
                    onNodeWithText("10:30").assertIsDisplayed()
                    onNodeWithText("18/05/2026", substring = true).assertIsDisplayed()
                }
            }
        }

        given("a leg with an arrival to be on time for") {
            then("the arrival segment is the selected one") {
                runComposeUiTest {
                    setContent {
                        RouteTimeSection(
                            choice = RouteTimeChoice.ArriveBy(TEN_THIRTY),
                            dayDate = DAY,
                            supportsArriveBy = true,
                            onModeChange = {},
                            onTimeChange = {},
                        )
                    }

                    onNodeWithTag(TransportPlanningTestTags.TIME_ARRIVE_BY).assertIsSelected()
                }
            }
        }

        given("a profile that cannot plan backwards from an arrival") {
            then("the arrival segment is not offered, while the other two still are") {
                runComposeUiTest {
                    setContent {
                        RouteTimeSection(
                            choice = RouteTimeChoice.Now,
                            dayDate = DAY,
                            supportsArriveBy = false,
                            onModeChange = {},
                            onTimeChange = {},
                        )
                    }

                    onNodeWithTag(TransportPlanningTestTags.TIME_ARRIVE_BY).assertIsNotEnabled()
                    onNodeWithTag(TransportPlanningTestTags.TIME_NOW).assertIsEnabled()
                    onNodeWithTag(TransportPlanningTestTags.TIME_DEPART_AT).assertIsEnabled()
                }
            }
        }

        given("a traveller wondering what now is measured from") {
            then("the explanation stays behind the info icon until it is asked for") {
                runComposeUiTest {
                    setContent {
                        RouteTimeSection(
                            choice = RouteTimeChoice.Now,
                            dayDate = DAY,
                            supportsArriveBy = true,
                            onModeChange = {},
                            onTimeChange = {},
                        )
                    }

                    onNodeWithTag(TransportPlanningTestTags.TIME_NOW_INFO, useUnmergedTree = true)
                        .assertIsDisplayed()
                    onNodeWithTag(TransportPlanningTestTags.TIME_NOW_TOOLTIP).assertDoesNotExist()
                }
            }

            `when`("the info icon is long pressed") {
                then("the explanation is shown") {
                    runComposeUiTest {
                        setContent {
                            RouteTimeSection(
                                choice = RouteTimeChoice.Now,
                                dayDate = DAY,
                                supportsArriveBy = true,
                                onModeChange = {},
                                onTimeChange = {},
                            )
                        }

                        onNodeWithTag(TransportPlanningTestTags.TIME_NOW_INFO, useUnmergedTree = true)
                            .performTouchInput { longClick() }

                        onNodeWithTag(TransportPlanningTestTags.TIME_NOW_TOOLTIP).assertIsDisplayed()
                    }
                }
            }

            `when`("the mouse hovers the info icon") {
                then("the explanation is shown, and it goes away once the mouse leaves") {
                    runComposeUiTest {
                        setContent {
                            RouteTimeSection(
                                choice = RouteTimeChoice.Now,
                                dayDate = DAY,
                                supportsArriveBy = true,
                                onModeChange = {},
                                onTimeChange = {},
                            )
                        }

                        onNodeWithTag(TransportPlanningTestTags.TIME_NOW_INFO, useUnmergedTree = true)
                            .performMouseInput { moveTo(center) }

                        onNodeWithTag(TransportPlanningTestTags.TIME_NOW_TOOLTIP).assertIsDisplayed()

                        onNodeWithTag(TransportPlanningTestTags.TIME_NOW_INFO, useUnmergedTree = true)
                            .performMouseInput { exit() }

                        onNodeWithTag(TransportPlanningTestTags.TIME_NOW_TOOLTIP).assertDoesNotExist()
                    }
                }
            }
        }

        given("a leg on a plan whose day has not loaded yet") {
            then("the hour is still shown, without a date under it") {
                runComposeUiTest {
                    setContent {
                        RouteTimeSection(
                            choice = RouteTimeChoice.DepartAt(TEN_THIRTY),
                            dayDate = null,
                            supportsArriveBy = true,
                            onModeChange = {},
                            onTimeChange = {},
                        )
                    }

                    onNodeWithText("10:30").assertIsDisplayed()
                    onNodeWithText("2026", substring = true).assertDoesNotExist()
                }
            }
        }
    })
