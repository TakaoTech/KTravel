package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.planning.PlaceUi
import com.takaotech.ktravel.presentation.planning.detail.PlacesBacklogEvent
import com.takaotech.ktravel.presentation.planning.detail.PlacesBacklogScreen
import com.takaotech.ktravel.presentation.planning.detail.PlacesBacklogUiState
import com.takaotech.ktravel.ui.common.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.place.PlaceItem
import com.takaotech.ktravel.ui.planning.common.AddPlaceButton
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.close
import ktravel.composeapp.generated.resources.planning_detail_cd_close_backlog
import ktravel.composeapp.generated.resources.planning_detail_cd_move_place_to_steps
import ktravel.composeapp.generated.resources.planning_detail_places_empty
import ktravel.composeapp.generated.resources.planning_detail_places_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal object PlacesBacklogTestTags {
    const val LIST = "places_backlog_list"
    const val EMPTY = "places_backlog_empty"
    const val CLOSE_BUTTON = "places_backlog_close"
    const val ADD_PLACE_BUTTON = "places_backlog_add_place"
    fun placeTag(id: String) = "places_backlog_place_$id"
    fun moveToStepsTag(id: String) = "places_backlog_move_to_steps_$id"
}

/**
 * `Ui` Circuit del pannello backlog posti (registrata via Metro `@CircuitInject`): renderizza lo
 * stato e inoltra gli eventi via `eventSink`, riusando il contenuto stateless [PlacesBacklogContent].
 */
@CircuitInject(PlacesBacklogScreen::class, AppScope::class)
@Composable
fun PlacesBacklogUi(state: PlacesBacklogUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    PlacesBacklogContent(
        places = state.places,
        pendingPermanentDelete = state.pendingPermanentDelete,
        modifier = modifier,
        onCloseClick = { sink(PlacesBacklogEvent.Close) },
        onAddPlaceClick = { sink(PlacesBacklogEvent.AddPlace) },
        onMovePlaceToStepsClick = { sink(PlacesBacklogEvent.MovePlaceToSteps(it)) },
        onMovePlaceToBacklogClick = { sink(PlacesBacklogEvent.MovePlaceToBacklog(it)) },
        onPermanentDeleteRequest = { sink(PlacesBacklogEvent.PermanentDeleteRequested(it)) },
        onPermanentDeleteConfirm = { sink(PlacesBacklogEvent.PermanentDeleteConfirmed) },
        onPermanentDeleteDismiss = { sink(PlacesBacklogEvent.PermanentDeleteDismissed) }
    )
}

/**
 * Contenuto stateless del pannello backlog: il dialog di conferma eliminazione è guidato da
 * [pendingPermanentDelete] (stato del presenter), la UI si limita a renderizzarlo.
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
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
    ) {
        if (pendingPermanentDelete != null) {
            DisruptiveOperationDialog(
                onConfirm = onPermanentDeleteConfirm,
                onDismiss = onPermanentDeleteDismiss
            )
        }

        Column {
            IconButton(
                modifier = Modifier.testTag(PlacesBacklogTestTags.CLOSE_BUTTON),
                onClick = onCloseClick
            ) {
                Icon(
                    painter = painterResource(Res.drawable.close),
                    contentDescription = stringResource(Res.string.planning_detail_cd_close_backlog)
                )
            }

            Text(
                text = stringResource(Res.string.planning_detail_places_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
                    .padding(horizontal = 8.dp)
            )

            AddPlaceButton(
                modifier = Modifier.testTag(PlacesBacklogTestTags.ADD_PLACE_BUTTON),
                onClick = onAddPlaceClick
            )

            LazyColumn(
                modifier = Modifier.testTag(PlacesBacklogTestTags.LIST),
                contentPadding = PaddingValues(8.dp),
            ) {
                items(items = places, key = { it.id }) { place ->
                    // TODO Add Hours
                    // TODO Add image
                    PlaceItem(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .testTag(PlacesBacklogTestTags.placeTag(place.id)),
                        name = place.name,
                        onDeleteClick = {
                            onMovePlaceToBacklogClick(place.id)
                        },
                        actions = {
                            IconButton(
                                modifier = Modifier.testTag(
                                    PlacesBacklogTestTags.moveToStepsTag(place.id)
                                ),
                                onClick = {
                                    onMovePlaceToStepsClick(place.id)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.add),
                                    contentDescription = stringResource(
                                        Res.string.planning_detail_cd_move_place_to_steps
                                    )
                                )
                            }
                        },
                        onPermanentDeleteClick = {
                            onPermanentDeleteRequest(place.id)
                        }
                    )
                }

                if (places.isEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.testTag(PlacesBacklogTestTags.EMPTY),
                            text = stringResource(Res.string.planning_detail_places_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlacesBacklogContentPreview() = KTravelTheme {
    PlacesBacklogContent(
        places = persistentListOf(
            PlaceUi(name = "Tokyo Tower", lat = 0.0, lng = 0.0),
            PlaceUi(name = "Shibuya Crossing", lat = 0.0, lng = 0.0)
        ),
        pendingPermanentDelete = null,
        onCloseClick = {},
        onAddPlaceClick = {},
        onMovePlaceToStepsClick = {},
        onMovePlaceToBacklogClick = {},
        onPermanentDeleteRequest = {},
        onPermanentDeleteConfirm = {},
        onPermanentDeleteDismiss = {}
    )
}
