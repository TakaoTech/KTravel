package com.takaotech.ktravel.ui.planning.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.takaotech.ktravel.core.ui.preview.TravelDayStepPreviewParameterProvider
import com.takaotech.ktravel.presentation.planning.PlaceUi
import com.takaotech.ktravel.presentation.planning.PlanHeader
import com.takaotech.ktravel.presentation.planning.PlanningViewModel
import com.takaotech.ktravel.presentation.planning.TravelDayUi
import com.takaotech.ktravel.ui.common.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.common.rememberDisruptiveOperationDialog
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.planning_trip_add_place
import ktravel.composeapp.generated.resources.planning_trip_cd_back
import ktravel.composeapp.generated.resources.planning_trip_cd_delete_place
import ktravel.composeapp.generated.resources.planning_trip_cd_save
import ktravel.composeapp.generated.resources.planning_trip_cd_settings
import ktravel.composeapp.generated.resources.planning_trip_itinerary_title
import ktravel.composeapp.generated.resources.planning_trip_places_title
import ktravel.composeapp.generated.resources.save
import ktravel.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@Serializable
data class PlanningTripPageNavigation(val travelId: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningTripPage(
    viewModel: PlanningViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onSettingClicked: () -> Unit,
    onAddPlaceClicked: () -> Unit,
    onDateClicked: (id: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val planHeader = uiState.planHeader
    val days = uiState.days

    val deleteDialogState = rememberDisruptiveOperationDialog<String> { placeId ->
        viewModel.deletePlace(placeId)
    }

    DisruptiveOperationDialog(
        state = deleteDialogState
    )

    PlanningTripPage(
        modifier = modifier,
        planHeader = planHeader,
        places = uiState.places,
        days = days,
        onBackClick = onBackClick,
        onSaveClick = onSaveClick,
        onPlanNameChange = {
            viewModel.onPlanNameChanged(it)
        },
        onPlanDateRangeChanged = { start, end ->
            viewModel.onPlanDateChanged(start, end)
        },
        onAddPlaceClicked = onAddPlaceClicked,
        onDeletePermanentPlaceClick = {
            deleteDialogState.show(it)
        },
        onDateClicked = onDateClicked,
        onPlaceMovedToDay = { placeId, dayId ->
            viewModel.onPlaceMovedToDate(placeId, dayId)
        },
        onSettingClicked = onSettingClicked
    )
}

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlanningTripPage(
    planHeader: PlanHeader,
    places: PersistentList<PlaceUi>,
    days: ImmutableList<TravelDayUi>,
    modifier: Modifier = Modifier,

    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,

    onSettingClicked: () -> Unit,

    onPlanNameChange: (TextFieldValue) -> Unit,
    onPlanDateRangeChanged: (start: Long, end: Long) -> Unit,

    onAddPlaceClicked: () -> Unit,
    onDeletePermanentPlaceClick: (String) -> Unit,

    onDateClicked: (id: String) -> Unit,
    onPlaceMovedToDay: (placeId: String, dayId: String) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.planning_trip_cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSaveClick) {
                        Icon(
                            painter = painterResource(Res.drawable.save),
                            contentDescription = stringResource(Res.string.planning_trip_cd_save)
                        )
                    }
                    IconButton(onClick = onSettingClicked) {
                        Icon(
                            painter = painterResource(Res.drawable.settings),
                            contentDescription = stringResource(Res.string.planning_trip_cd_settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPlaceClicked,
                expanded = currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(
                    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                ),
                text = {
                    Text(
                        text = stringResource(Res.string.planning_trip_add_place)
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.add),
                        contentDescription = stringResource(Res.string.planning_trip_add_place)
                    )
                }
            )
        }
    ) {
        val dragAndDropState = rememberDragAndDropState<PlaceUi>()

        DragAndDropContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            state = dragAndDropState,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PlanningHeader(
                        name = planHeader.name,
                        onNameChange = onPlanNameChange,
                        startDateMillis = planHeader.period.start,
                        endDateMillis = planHeader.period.end,
                        onPlanDateRangeChanged = onPlanDateRangeChanged
                    )
                }

                if (places.isNotEmpty()) {
                    item {
                        SectionTitle(text = stringResource(Res.string.planning_trip_places_title))
                    }
                }

                itemsIndexed(
                    items = places,
                    key = { _, place ->
                        place.id
                    }
                ) { _, place ->
                    DraggableItem(
                        state = dragAndDropState,
                        key = place.id, // Unique key for each draggable item
                        data = place,
                    ) {
                        TripPlaceRow(
                            name = place.name,
                            onDeleteClick = {
                                onDeletePermanentPlaceClick(place.id)
                            }
                        )
                    }
                }

                item {
                    SectionTitle(text = stringResource(Res.string.planning_trip_itinerary_title))
                }

                itemsIndexed(
                    items = days,
                    key = { _: Int, day: TravelDayUi ->
                        day.date.toEpochDays()
                    }
                ) { _, day ->
                    PlanDayItem(
                        modifier = Modifier.dropTarget(
                            state = dragAndDropState,
                            key = day.id,
                            onDrop = { state ->
                                val place = state.data
                                onPlaceMovedToDay(place.id, day.id)
                                place
                            }
                        ),
                        day = day.date,
                        placeSteps = day.placeSteps,
                        onDateClicked = {
                            onDateClicked(day.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

/**
 * A place of the trip backlog, not assigned to any day yet. It is dragged onto a [PlanDayItem] to
 * plan it.
 */
@Composable
private fun TripPlaceRow(
    name: String,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(start = 18.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    contentDescription = stringResource(Res.string.planning_trip_cd_delete_place)
                )
            }
        }
    }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun PlanningPagePreview() = KTravelTheme {
    PlanningTripPage(
        planHeader = PlanHeader(
            name = TextFieldValue("Viaggio in Italia"),
        ),
        places = persistentListOf(
            PlaceUi(id = "1", name = "Binasco", lat = 45.3328, lng = 9.1010),
            PlaceUi(id = "2", name = "Assago", lat = 45.4030, lng = 9.1290)
        ),
        days = persistentListOf(
            TravelDayUi(
                date = LocalDate(2024, 6, 15),
                steps = TravelDayStepPreviewParameterProvider(8).values.toList().toPersistentList()
            ),
            TravelDayUi(
                date = LocalDate(2024, 6, 16)
            )
        ),
        onBackClick = {},
        onPlanNameChange = {},
        onDeletePermanentPlaceClick = {},
        onPlanDateRangeChanged = { start, end -> },
        onAddPlaceClicked = {},
        onDateClicked = {},
        onPlaceMovedToDay = { _, _ -> },
        onSettingClicked = {},
        onSaveClick = {}
    )
}
