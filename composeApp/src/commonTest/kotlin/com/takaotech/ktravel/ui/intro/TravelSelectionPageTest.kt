package com.takaotech.ktravel.ui.intro

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.takaotech.ktravel.presentation.intro.TravelSummaryUiState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalTestApi::class)
class TravelSelectionPageTest : BehaviorSpec() {

    private val travel1 = TravelSummaryUiState(
        id = "id-1",
        name = "Viaggio a Tokyo",
        periodStart = LocalDate(2024, 3, 1),
        periodEnd = LocalDate(2024, 3, 15)
    )
    private val travel2 = TravelSummaryUiState(
        id = "id-2",
        name = "Weekend a Roma",
        periodStart = LocalDate(2024, 6, 10),
        periodEnd = LocalDate(2024, 6, 12)
    )

    init {
        given("TravelSelectionPage with an empty travel list") {
            then("the search bar should be visible") {
                runComposeUiTest {
                    setContent {
                        TravelSelectionPage(
                            travelList = persistentListOf(),
                            onTravelClick = {},
                            newTravelClick = {}
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
                            newTravelClick = {}
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
                            newTravelClick = {}
                        )
                    }
                    onNodeWithTag(TravelSelectionTestTags.travelItemTag(travel1.id)).assertDoesNotExist()
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
                            newTravelClick = {}
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
                            newTravelClick = {}
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
                            newTravelClick = {}
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
                                newTravelClick = {}
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
                                newTravelClick = {}
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
                                newTravelClick = { newTravelClicked = true }
                            )
                        }
                        onNodeWithTag(TravelSelectionTestTags.FAB_NEW_TRAVEL).performClick()
                    }
                    newTravelClicked shouldBe true
                }
            }
        }
    }
}
