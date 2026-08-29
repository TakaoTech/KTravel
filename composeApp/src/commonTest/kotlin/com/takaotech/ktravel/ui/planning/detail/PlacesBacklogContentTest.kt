package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaotech.ktravel.presentation.planning.PlaceUi
import com.takaotech.ktravel.ui.common.DisruptiveOperationDialogTestTags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf

/**
 * The delete dialog is queried by test tag rather than by label: its labels come from
 * [org.jetbrains.compose.resources.stringResource], which resolves in the JVM default locale, so
 * matching their wording would tie the test to whichever language the host machine runs in. Place
 * names are plain data, so those are still matched by text.
 */
@OptIn(ExperimentalTestApi::class)
class PlacesBacklogContentTest : BehaviorSpec() {

    private val place1 = PlaceUi(id = "place-1", name = "Tokyo Tower", lat = 0.0, lng = 0.0)
    private val place2 = PlaceUi(id = "place-2", name = "Shibuya Crossing", lat = 0.0, lng = 0.0)

    init {
        given("PlacesBacklogContent with an empty place list") {
            then("the empty state should be visible") {
                runComposeUiTest {
                    setContent {
                        PlacesBacklogContent(
                            places = persistentListOf(),
                            pendingPermanentDelete = null,
                            onCloseClick = {},
                            onAddPlaceClick = {},
                            onMovePlaceToStepsClick = {},
                            onMovePlaceToBacklogClick = {},
                            onPermanentDeleteRequest = {},
                            onPermanentDeleteConfirm = {},
                            onPermanentDeleteDismiss = {},
                        )
                    }
                    onNodeWithTag(PlacesBacklogTestTags.EMPTY).assertIsDisplayed()
                }
            }

            then("the empty state should explain what fills the backlog") {
                runComposeUiTest {
                    setContent {
                        PlacesBacklogContent(
                            places = persistentListOf(),
                            pendingPermanentDelete = null,
                            onCloseClick = {},
                            onAddPlaceClick = {},
                            onMovePlaceToStepsClick = {},
                            onMovePlaceToBacklogClick = {},
                            onPermanentDeleteRequest = {},
                            onPermanentDeleteConfirm = {},
                            onPermanentDeleteDismiss = {},
                        )
                    }
                    onNodeWithTag(PlacesBacklogTestTags.EMPTY_HINT).assertIsDisplayed()
                }
            }
        }

        given("PlacesBacklogContent with places") {
            then("the place names should be displayed") {
                runComposeUiTest {
                    setContent {
                        PlacesBacklogContent(
                            places = persistentListOf(place1, place2),
                            pendingPermanentDelete = null,
                            onCloseClick = {},
                            onAddPlaceClick = {},
                            onMovePlaceToStepsClick = {},
                            onMovePlaceToBacklogClick = {},
                            onPermanentDeleteRequest = {},
                            onPermanentDeleteConfirm = {},
                            onPermanentDeleteDismiss = {},
                        )
                    }
                    onNodeWithText(place1.name).assertIsDisplayed()
                    onNodeWithText(place2.name).assertIsDisplayed()
                }
            }

            `when`("the move-to-steps button of a place is clicked") {
                then("onMovePlaceToStepsClick should be called with that place id") {
                    var movedId: String? = null
                    runComposeUiTest {
                        setContent {
                            PlacesBacklogContent(
                                places = persistentListOf(place1, place2),
                                pendingPermanentDelete = null,
                                onCloseClick = {},
                                onAddPlaceClick = {},
                                onMovePlaceToStepsClick = { movedId = it },
                                onMovePlaceToBacklogClick = {},
                                onPermanentDeleteRequest = {},
                                onPermanentDeleteConfirm = {},
                                onPermanentDeleteDismiss = {},
                            )
                        }
                        onNodeWithTag(
                            PlacesBacklogTestTags.moveToStepsTag(place2.id),
                        ).performClick()
                    }
                    movedId shouldBe place2.id
                }
            }
        }

        given("PlacesBacklogContent header actions") {
            `when`("the close button is clicked") {
                then("onCloseClick should be invoked") {
                    var closed = false
                    runComposeUiTest {
                        setContent {
                            PlacesBacklogContent(
                                places = persistentListOf(),
                                pendingPermanentDelete = null,
                                onCloseClick = { closed = true },
                                onAddPlaceClick = {},
                                onMovePlaceToStepsClick = {},
                                onMovePlaceToBacklogClick = {},
                                onPermanentDeleteRequest = {},
                                onPermanentDeleteConfirm = {},
                                onPermanentDeleteDismiss = {},
                            )
                        }
                        onNodeWithTag(PlacesBacklogTestTags.CLOSE_BUTTON).performClick()
                    }
                    closed shouldBe true
                }
            }

            `when`("the add-place button is clicked") {
                then("onAddPlaceClick should be invoked") {
                    var addClicked = false
                    runComposeUiTest {
                        setContent {
                            PlacesBacklogContent(
                                places = persistentListOf(),
                                pendingPermanentDelete = null,
                                onCloseClick = {},
                                onAddPlaceClick = { addClicked = true },
                                onMovePlaceToStepsClick = {},
                                onMovePlaceToBacklogClick = {},
                                onPermanentDeleteRequest = {},
                                onPermanentDeleteConfirm = {},
                                onPermanentDeleteDismiss = {},
                            )
                        }
                        onNodeWithTag(PlacesBacklogTestTags.ADD_PLACE_BUTTON).performClick()
                    }
                    addClicked shouldBe true
                }
            }
        }

        given("PlacesBacklogContent and the permanent-delete dialog") {
            then("the dialog should not exist when there is no pending delete") {
                runComposeUiTest {
                    setContent {
                        PlacesBacklogContent(
                            places = persistentListOf(place1),
                            pendingPermanentDelete = null,
                            onCloseClick = {},
                            onAddPlaceClick = {},
                            onMovePlaceToStepsClick = {},
                            onMovePlaceToBacklogClick = {},
                            onPermanentDeleteRequest = {},
                            onPermanentDeleteConfirm = {},
                            onPermanentDeleteDismiss = {},
                        )
                    }
                    onNodeWithTag(DisruptiveOperationDialogTestTags.DIALOG).assertDoesNotExist()
                }
            }

            then("the dialog should be displayed when a pending delete is set") {
                runComposeUiTest {
                    setContent {
                        PlacesBacklogContent(
                            places = persistentListOf(place1),
                            pendingPermanentDelete = place1,
                            onCloseClick = {},
                            onAddPlaceClick = {},
                            onMovePlaceToStepsClick = {},
                            onMovePlaceToBacklogClick = {},
                            onPermanentDeleteRequest = {},
                            onPermanentDeleteConfirm = {},
                            onPermanentDeleteDismiss = {},
                        )
                    }
                    onNodeWithTag(DisruptiveOperationDialogTestTags.DIALOG).assertIsDisplayed()
                }
            }

            `when`("the dialog confirm button is clicked") {
                then("onPermanentDeleteConfirm should be invoked") {
                    var confirmed = false
                    runComposeUiTest {
                        setContent {
                            PlacesBacklogContent(
                                places = persistentListOf(place1),
                                pendingPermanentDelete = place1,
                                onCloseClick = {},
                                onAddPlaceClick = {},
                                onMovePlaceToStepsClick = {},
                                onMovePlaceToBacklogClick = {},
                                onPermanentDeleteRequest = {},
                                onPermanentDeleteConfirm = { confirmed = true },
                                onPermanentDeleteDismiss = {},
                            )
                        }
                        onNodeWithTag(DisruptiveOperationDialogTestTags.CONFIRM).performClick()
                    }
                    confirmed shouldBe true
                }
            }

            `when`("the dialog cancel button is clicked") {
                then("onPermanentDeleteDismiss should be invoked") {
                    var dismissed = false
                    runComposeUiTest {
                        setContent {
                            PlacesBacklogContent(
                                places = persistentListOf(place1),
                                pendingPermanentDelete = place1,
                                onCloseClick = {},
                                onAddPlaceClick = {},
                                onMovePlaceToStepsClick = {},
                                onMovePlaceToBacklogClick = {},
                                onPermanentDeleteRequest = {},
                                onPermanentDeleteConfirm = {},
                                onPermanentDeleteDismiss = { dismissed = true },
                            )
                        }
                        onNodeWithTag(DisruptiveOperationDialogTestTags.CANCEL).performClick()
                    }
                    dismissed shouldBe true
                }
            }
        }
    }
}
