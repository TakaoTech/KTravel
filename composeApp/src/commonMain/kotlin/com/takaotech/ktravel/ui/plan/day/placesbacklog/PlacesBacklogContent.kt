package com.takaotech.ktravel.ui.plan.day.placesbacklog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.plan.PlaceUi
import com.takaotech.ktravel.ui.plan.day.placesbacklog.component.AddPlaceButton
import com.takaotech.ktravel.ui.plan.day.placesbacklog.component.BacklogEmptyState
import com.takaotech.ktravel.ui.plan.day.placesbacklog.component.BacklogHeader
import com.takaotech.ktravel.ui.plan.day.placesbacklog.component.BacklogPlaceRow
import com.takaotech.ktravel.ui.shared.dialog.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The backlog pane, drawn from plain values.
 *
 * The confirmation dialog is not opened here: [pendingPermanentDelete] says whether one is waiting
 * to be answered, and this only draws it, so what the user is being asked stays testable without a
 * screen.
 */
@Composable
internal fun PlacesBacklogContent(
    places: ImmutableList<PlaceUi>,
    pendingPermanentDelete: PlaceUi?,
    onCloseClick: () -> Unit,
    onAddPlaceClick: () -> Unit,
    onMovePlaceToStepsClick: (String) -> Unit,
    onMovePlaceToBacklogClick: (String) -> Unit,
    onPermanentDeleteRequest: (String) -> Unit,
    onPermanentDeleteConfirm: () -> Unit,
    onPermanentDeleteDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        if (pendingPermanentDelete != null) {
            DisruptiveOperationDialog(
                onConfirm = onPermanentDeleteConfirm,
                onDismiss = onPermanentDeleteDismiss,
            )
        }

        Column(
            modifier = Modifier.safeDrawingPadding(),
        ) {
            BacklogHeader(
                count = places.size,
                onCloseClick = onCloseClick,
            )

            Box(modifier = Modifier.weight(1f)) {
                if (places.isEmpty()) {
                    BacklogEmptyState(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.testTag(PlacesBacklogTestTags.LIST),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items = places, key = { it.id }) { place ->
                            // TODO Add Hours
                            // TODO Add image
                            BacklogPlaceRow(
                                modifier = Modifier
                                    .animateItem()
                                    .testTag(PlacesBacklogTestTags.placeTag(place.id)),
                                place = place,
                                onDeleteClick = { onMovePlaceToBacklogClick(place.id) },
                                onPermanentDeleteClick = { onPermanentDeleteRequest(place.id) },
                                onMoveToStepsClick = { onMovePlaceToStepsClick(place.id) },
                            )
                        }
                    }
                }
            }

            AddPlaceButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag(PlacesBacklogTestTags.ADD_PLACE_BUTTON),
                onClick = onAddPlaceClick,
            )
        }
    }
}

//region Previews
@Preview
@Composable
private fun PlacesBacklogContentPreview() = KTravelTheme {
    PlacesBacklogContent(
        places = persistentListOf(
            PlaceUi(
                name = "Tokyo Tower",
                lat = 0.0,
                lng = 0.0,
                hasNote = true,
                attachmentCount = 3,
            ),
            PlaceUi(name = "Shibuya Crossing", lat = 0.0, lng = 0.0),
        ),
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
//endregion Previews
