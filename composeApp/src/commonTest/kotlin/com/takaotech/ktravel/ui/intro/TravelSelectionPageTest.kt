package com.takaotech.ktravel.ui.intro

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaotech.ktravel.presentation.intro.ImportUiState
import com.takaotech.ktravel.presentation.intro.TravelSummaryUiState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalTestApi::class)
class TravelSelectionPageTest : BehaviorSpec() {

    private val travel1 = TravelSummaryUiState(
        id = "id-1",
        name = "Viaggio a Tokyo",
        periodStart = LocalDate(2024, 3, 1),
        periodEnd = LocalDate(2024, 3, 15),
    )
    private val travel2 = TravelSummaryUiState(
        id = "id-2",
        name = "Weekend a Roma",
        periodStart = LocalDate(2024, 6, 10),
        periodEnd = LocalDate(2024, 6, 12),
    )

    init {
        given("TravelSelectionPage with an empty travel list") {
            then("the search bar should be visible") {
                runComposeUiTest {
                    setContent {
                        TravelSelectionPage(
                            travelList = persistentListOf(),
                            onTravelClick = {},
                            newTravelClick = {},
                        )
                    }
                    onNodeWithTag(TravelSelectionTestTags.SEARCH_BAR).assertIsDisplayed()
                }
            }

            then("the FAB should be visible") {
                runComposeUiTest {
                    setContent {
                        TravelSelectionPage(
                            travelList = persistentListOf(),
                            onTravelClick = {},
                            newTravelClick = {},
                        )
                    }
                    onNodeWithTag(TravelSelectionTestTags.FAB_NEW_TRAVEL).assertIsDisplayed()
                }
            }

            then("no travel item nodes should exist") {
                runComposeUiTest {
                    setContent {
                        TravelSelectionPage(
                            travelList = persistentListOf(),
                            onTravelClick = {},
                            newTravelClick = {},
                        )
                    }
                    onNodeWithTag(TravelSelectionTestTags.travelItemTag(travel1.id)).assertDoesNotExist()
                }
            }
        }

        given("TravelSelectionPage outside selection mode") {
            then("the import action should be displayed") {
                runComposeUiTest {
                    setContent {
                        TravelSelectionPage(
                            travelList = persistentListOf(),
                            onTravelClick = {},
                            newTravelClick = {},
                        )
                    }
                    onNodeWithTag(TravelSelectionTestTags.TOP_BAR_IMPORT).assertIsDisplayed()
                }
            }

            `when`("the import action is clicked") {
                then("onImportClick should be invoked") {
                    var clicked = 0
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(),
                                onTravelClick = {},
                                onImportClick = { clicked++ },
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.TOP_BAR_IMPORT).performClick()
                    }
                    clicked shouldBe 1
                }
            }

            `when`("an import is already running") {
                then("the import action should be disabled") {
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(),
                                importState = ImportUiState.Importing,
                                onTravelClick = {},
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.TOP_BAR_IMPORT).assertIsNotEnabled()
                    }
                }
            }
        }

        given("TravelSelectionPage with a single travel item") {
            then("the travel name should be displayed") {
                runComposeUiTest {
                    setContent {
                        TravelSelectionPage(
                            travelList = persistentListOf(travel1),
                            onTravelClick = {},
                            newTravelClick = {},
                        )
                    }
                    onNodeWithText(travel1.name).assertIsDisplayed()
                }
            }

            then("the travel date range should be displayed") {
                runComposeUiTest {
                    setContent {
                        TravelSelectionPage(
                            travelList = persistentListOf(travel1),
                            onTravelClick = {},
                            newTravelClick = {},
                        )
                    }
                    onNodeWithText("${travel1.periodStart} - ${travel1.periodEnd}").assertIsDisplayed()
                }
            }
        }

        given("TravelSelectionPage with multiple travel items") {
            then("all travel names should be displayed") {
                runComposeUiTest {
                    setContent {
                        TravelSelectionPage(
                            travelList = persistentListOf(travel1, travel2),
                            onTravelClick = {},
                            newTravelClick = {},
                        )
                    }
                    onNodeWithText(travel1.name).assertIsDisplayed()
                    onNodeWithText(travel2.name).assertIsDisplayed()
                }
            }
        }

        given("TravelSelectionPage when a travel item is clicked") {
            `when`("the item is clicked") {
                then("onTravelClick should be called with that item's id") {
                    var clickedId: String? = null
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(travel1, travel2),
                                onTravelClick = { clickedId = it },
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.travelItemTag(travel2.id)).performClick()
                    }
                    clickedId shouldBe travel2.id
                }
            }

            `when`("a different item is clicked") {
                then("onTravelClick should be called with only that item's id") {
                    var clickedId: String? = null
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(travel1, travel2),
                                onTravelClick = { clickedId = it },
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.travelItemTag(travel1.id)).performClick()
                    }
                    clickedId shouldBe travel1.id
                }
            }
        }

        given("TravelSelectionPage when the FAB is clicked") {
            `when`("the FAB button is clicked") {
                then("the newTravelClick callback should be invoked") {
                    var newTravelClicked = false
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(),
                                onTravelClick = {},
                                newTravelClick = { newTravelClicked = true },
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.FAB_NEW_TRAVEL).performClick()
                    }
                    newTravelClicked shouldBe true
                }
            }
        }

        given("TravelSelectionPage when a travel item is long pressed") {
            `when`("the long press gesture completes") {
                then("onTravelLongClick should be called with that item's id") {
                    var longClickedId: String? = null
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(travel1, travel2),
                                onTravelClick = {},
                                onTravelLongClick = { longClickedId = it },
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.travelItemTag(travel2.id))
                            .performTouchInput { longClick() }
                    }
                    longClickedId shouldBe travel2.id
                }
            }
        }

        given("TravelSelectionPage when a travel item is swiped") {
            `when`("the item is swiped towards the end of the layout") {
                then("onSwipeToDelete should be called with that item's id") {
                    var swipedId: String? = null
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(travel1, travel2),
                                onTravelClick = {},
                                onSwipeToDelete = { swipedId = it },
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.travelItemTag(travel1.id))
                            .performTouchInput { swipeLeft() }
                        waitForIdle()
                    }
                    swipedId shouldBe travel1.id
                }
            }

            `when`("the swipe is not confirmed") {
                then("the item should still be displayed") {
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(travel1),
                                onTravelClick = {},
                                onSwipeToDelete = {},
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.travelItemTag(travel1.id))
                            .performTouchInput { swipeLeft() }
                        waitForIdle()
                        onNodeWithText(travel1.name).assertIsDisplayed()
                    }
                }
            }
        }

        given("TravelSelectionPage in selection mode") {
            then("the selected item should be marked as selected") {
                runComposeUiTest {
                    setContent {
                        TravelSelectionPage(
                            travelList = persistentListOf(travel1, travel2),
                            isSelectionMode = true,
                            selectedIds = persistentSetOf(travel1.id),
                            onTravelClick = {},
                            newTravelClick = {},
                        )
                    }
                    onNodeWithTag(TravelSelectionTestTags.travelItemTag(travel1.id)).assertIsSelected()
                    onNodeWithTag(TravelSelectionTestTags.travelItemTag(travel2.id)).assertIsNotSelected()
                }
            }

            then("the FAB should be hidden") {
                runComposeUiTest {
                    setContent {
                        TravelSelectionPage(
                            travelList = persistentListOf(travel1),
                            isSelectionMode = true,
                            selectedIds = persistentSetOf(travel1.id),
                            onTravelClick = {},
                            newTravelClick = {},
                        )
                    }
                    onNodeWithTag(TravelSelectionTestTags.FAB_NEW_TRAVEL).assertDoesNotExist()
                }
            }

            `when`("the delete action is clicked") {
                then("onDeleteSelectedClick should be invoked") {
                    var deleteClicked = false
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(travel1, travel2),
                                isSelectionMode = true,
                                selectedIds = persistentSetOf(travel1.id),
                                onTravelClick = {},
                                onDeleteSelectedClick = { deleteClicked = true },
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.TOP_BAR_DELETE_SELECTED).performClick()
                    }
                    deleteClicked shouldBe true
                }
            }

            `when`("nothing is selected") {
                then("the delete action should be disabled") {
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(travel1),
                                isSelectionMode = true,
                                selectedIds = persistentSetOf(),
                                onTravelClick = {},
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.TOP_BAR_DELETE_SELECTED).assertIsNotEnabled()
                    }
                }
            }

            `when`("the exit action is clicked") {
                then("onExitSelectionMode should be invoked") {
                    var exitClicked = false
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(travel1),
                                isSelectionMode = true,
                                selectedIds = persistentSetOf(travel1.id),
                                onTravelClick = {},
                                onExitSelectionMode = { exitClicked = true },
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.TOP_BAR_EXIT_SELECTION).performClick()
                    }
                    exitClicked shouldBe true
                }
            }

            `when`("an item is clicked") {
                then("onTravelClick should still receive that item's id so the caller can toggle it") {
                    var clickedId: String? = null
                    runComposeUiTest {
                        setContent {
                            TravelSelectionPage(
                                travelList = persistentListOf(travel1, travel2),
                                isSelectionMode = true,
                                selectedIds = persistentSetOf(travel1.id),
                                onTravelClick = { clickedId = it },
                                newTravelClick = {},
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.travelItemTag(travel2.id)).performClick()
                    }
                    clickedId shouldBe travel2.id
                }
            }
        }
    }
}
