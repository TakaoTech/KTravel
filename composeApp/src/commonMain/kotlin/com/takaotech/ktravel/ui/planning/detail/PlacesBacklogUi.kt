package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.planning.PlaceUi
import com.takaotech.ktravel.presentation.planning.detail.PlacesBacklogEvent
import com.takaotech.ktravel.presentation.planning.detail.PlacesBacklogScreen
import com.takaotech.ktravel.presentation.planning.detail.PlacesBacklogUiState
import com.takaotech.ktravel.ui.common.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.planning.common.AddPlaceButton
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.close
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.place_delete
import ktravel.composeapp.generated.resources.place_delete_permanent
import ktravel.composeapp.generated.resources.planning_detail_cd_close_backlog
import ktravel.composeapp.generated.resources.planning_detail_cd_delete_step
import ktravel.composeapp.generated.resources.planning_detail_cd_move_place_to_steps
import ktravel.composeapp.generated.resources.planning_detail_places_empty
import ktravel.composeapp.generated.resources.planning_detail_places_empty_hint
import ktravel.composeapp.generated.resources.planning_detail_places_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal object PlacesBacklogTestTags {
    const val LIST = "places_backlog_list"
    const val EMPTY = "places_backlog_empty"
    const val EMPTY_HINT = "places_backlog_empty_hint"
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
        onPermanentDeleteDismiss = { sink(PlacesBacklogEvent.PermanentDeleteDismissed) },
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

/** Title, how many places are still waiting, and the way out of the pane. */
@Composable
private fun BacklogHeader(count: Int, onCloseClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.planning_detail_places_title),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (count > 0) {
            CountBadge(count)
        }

        IconButton(
            modifier = Modifier.testTag(PlacesBacklogTestTags.CLOSE_BUTTON),
            onClick = onCloseClick,
        ) {
            Icon(
                painter = painterResource(Res.drawable.close),
                contentDescription = stringResource(Res.string.planning_detail_cd_close_backlog),
            )
        }
    }
}

/** How many places the backlog holds, as the accent pill of the design. */
@Composable
private fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Text(
            modifier = Modifier
                .defaultMinSize(minWidth = 22.dp)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}

/** Nothing waiting: say so, and say what puts something here. */
@Composable
private fun BacklogEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            modifier = Modifier.testTag(PlacesBacklogTestTags.EMPTY),
            text = stringResource(Res.string.planning_detail_places_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.testTag(PlacesBacklogTestTags.EMPTY_HINT),
            text = stringResource(Res.string.planning_detail_places_empty_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * A place waiting to enter the itinerary: the drag affordance of the design, the name, and the two
 * actions the backlog has always had — remove (with the permanent variant behind the menu) and
 * promote to a step of the day.
 */
@Composable
private fun BacklogPlaceRow(
    place: PlaceUi,
    onDeleteClick: () -> Unit,
    onPermanentDeleteClick: () -> Unit,
    onMoveToStepsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
//            //Add support D&D?
//            Icon(
//                modifier = Modifier.size(18.dp),
//                painter = painterResource(Res.drawable.drag_indicator),
//                contentDescription = stringResource(Res.string.planning_detail_cd_reorder_step),
//                tint = MaterialTheme.colorScheme.onSurfaceVariant,
//            )

            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(
//                        start = 10.dp,
                        top = 10.dp,
                        bottom = 10.dp,
                    ),
                text = place.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            var menuExpanded by remember { mutableStateOf(false) }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    contentDescription = stringResource(Res.string.planning_detail_cd_delete_step),
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DeleteMode.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(option.text),
                                color = if (option == DeleteMode.PERMANENT) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    Color.Unspecified
                                },
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            when (option) {
                                DeleteMode.GENERAL -> onDeleteClick()
                                DeleteMode.PERMANENT -> onPermanentDeleteClick()
                            }
                        },
                    )
                }
            }

            IconButton(
                modifier = Modifier.testTag(PlacesBacklogTestTags.moveToStepsTag(place.id)),
                onClick = onMoveToStepsClick,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.add),
                    contentDescription = stringResource(
                        Res.string.planning_detail_cd_move_place_to_steps,
                    ),
                )
            }
        }
    }
}

/**
 * How a place leaves the backlog.
 *
 * @property text Label of the entry in the delete menu.
 */
enum class DeleteMode(val text: StringResource) {
    /** Out of this trip: the place stays in the library and can be added back. */
    GENERAL(Res.string.place_delete),

    /** Out of the library for good, behind a confirmation. */
    PERMANENT(Res.string.place_delete_permanent),
}

@Preview
@Composable
private fun PlacesBacklogContentPreview() = KTravelTheme {
    PlacesBacklogContent(
        places = persistentListOf(
            PlaceUi(name = "Tokyo Tower", lat = 0.0, lng = 0.0),
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
