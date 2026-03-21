package com.takaotech.ktravel.ui.planning.trip

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.takaotech.ktravel.core.ui.preview.TravelDayStepPreviewParameterProvider
import com.takaotech.ktravel.presentation.planning.Place
import com.takaotech.ktravel.presentation.planning.PlanHeader
import com.takaotech.ktravel.presentation.planning.PlanningViewModel
import com.takaotech.ktravel.presentation.planning.TravelDay
import com.takaotech.ktravel.ui.common.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.common.rememberDisruptiveOperationDialog
import com.takaotech.ktravel.ui.place.PlaceItem
import com.takaotech.ktravel.ui.planning.common.AddPlaceButton
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.save
import ktravel.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.painterResource
import kotlin.time.ExperimentalTime

@Serializable
object PlanningTripPageNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningTripPage(
    viewModel: PlanningViewModel,
    modifier: Modifier = Modifier,
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
    places: PersistentList<Place>,
    days: ImmutableList<TravelDay>,
    modifier: Modifier = Modifier,

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
                actions = {
                    IconButton(onClick = onSettingClicked) {
                        Icon(painter = painterResource(Res.drawable.settings), contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick
            ) {
                Icon(
                    painter = painterResource(Res.drawable.save),
                    contentDescription = null
                )
            }
        }
    ) {
        val dragAndDropState = rememberDragAndDropState<Place>()

        DragAndDropContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            state = dragAndDropState,
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    PlanningHeader(
                        name = planHeader.name,
                        onNameChange = onPlanNameChange,
                        startDateMillis = planHeader.period.start,
                        endDateMillis = planHeader.period.end,
                        onPlanDateRangeChanged = onPlanDateRangeChanged
                    )
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
                        PlaceItem(
                            modifier = Modifier.padding(16.dp),
                            name = place.name,
                            onPermanentDeleteClick = {
                                onDeletePermanentPlaceClick(place.id)
                            }
                        )
                    }
                }

                item {
                    AddPlaceButton(
                        onClick = onAddPlaceClicked
                    )
                }

                item {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "Itinerario",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                itemsIndexed(
                    items = days,
                    key = { _: Int, day: TravelDay ->
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
                        onDateClicked = {
                            onDateClicked(day.id)
                        }
                    )
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun PlanningPagePreview() {
    LoremIpsum(10).values.first()
    PlanningTripPage(
        planHeader = PlanHeader(
            name = TextFieldValue("Viaggio in Italia"),
        ),
        places = persistentListOf(),
        days = persistentListOf(
            TravelDay(
                date = LocalDate(2024, 6, 15),
                steps = TravelDayStepPreviewParameterProvider(8).values.toList().toPersistentList()
            )
        ),
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
