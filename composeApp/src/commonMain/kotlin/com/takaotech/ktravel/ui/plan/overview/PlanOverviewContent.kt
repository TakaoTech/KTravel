package com.takaotech.ktravel.ui.plan.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.takaotech.ktravel.presentation.plan.ExportUiState
import com.takaotech.ktravel.presentation.plan.PlaceUi
import com.takaotech.ktravel.presentation.plan.PlanHeader
import com.takaotech.ktravel.presentation.plan.TravelDayUi
import com.takaotech.ktravel.ui.plan.overview.component.ExportLoadingDialog
import com.takaotech.ktravel.ui.plan.overview.component.PlanDayItem
import com.takaotech.ktravel.ui.plan.overview.component.PlanOverviewHeader
import com.takaotech.ktravel.ui.plan.overview.component.PlanPlaceRow
import com.takaotech.ktravel.ui.plan.overview.component.SectionTitle
import com.takaotech.ktravel.ui.plan.preview.TravelDayStepPreviewParameterProvider
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.file_export
import ktravel.composeapp.generated.resources.planning_trip_add_place
import ktravel.composeapp.generated.resources.planning_trip_cd_back
import ktravel.composeapp.generated.resources.planning_trip_cd_export
import ktravel.composeapp.generated.resources.planning_trip_cd_settings
import ktravel.composeapp.generated.resources.planning_trip_itinerary_title
import ktravel.composeapp.generated.resources.planning_trip_places_title
import ktravel.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
internal fun PlanOverviewContent(
    planHeader: PlanHeader,
    places: PersistentList<PlaceUi>,
    days: ImmutableList<TravelDayUi>,
    modifier: Modifier = Modifier,
    exportState: ExportUiState = ExportUiState.Idle,
    onBackClick: () -> Unit,
    onExportClick: () -> Unit,
    onExportMessageShown: () -> Unit,
    onSettingClicked: () -> Unit,
    onPlanNameChange: (TextFieldValue) -> Unit,
    onPlanDateRangeChanged: (start: Long, end: Long) -> Unit,
    onAddPlaceClicked: () -> Unit,
    onDeletePermanentPlaceClick: (String) -> Unit,
    onDateClicked: (id: String) -> Unit,
    onPlaceMovedToDay: (placeId: String, dayId: String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val exportMessage = exportState.message()

    // The dialog outlives the InProgress state: on success it stays up until the arrival animation
    // has played through. A failure has nothing to celebrate, so it closes right away.
    var isExportDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportUiState.InProgress -> isExportDialogVisible = true
            is ExportUiState.Failed -> isExportDialogVisible = false
            ExportUiState.Idle, ExportUiState.AwaitingSecretsChoice, is ExportUiState.Completed -> Unit
        }
    }

    // The snackbar would sit behind the dialog scrim, so the outcome is announced only once the
    // dialog is gone.
    LaunchedEffect(exportMessage, isExportDialogVisible) {
        if (isExportDialogVisible) return@LaunchedEffect

        exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            onExportMessageShown()
        }
    }

    if (isExportDialogVisible) {
        ExportLoadingDialog(
            isCompleted = exportState is ExportUiState.Completed,
            onCompletionAnimationEnd = { isExportDialogVisible = false },
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.planning_trip_cd_back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag(PlanOverviewTestTags.EXPORT),
                        onClick = onExportClick,
                        enabled = exportState !is ExportUiState.InProgress,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.file_export),
                            contentDescription = stringResource(Res.string.planning_trip_cd_export),
                        )
                    }
                    IconButton(onClick = onSettingClicked) {
                        Icon(
                            painter = painterResource(Res.drawable.settings),
                            contentDescription = stringResource(Res.string.planning_trip_cd_settings),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPlaceClicked,
                expanded = currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(
                    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
                ),
                text = {
                    Text(
                        text = stringResource(Res.string.planning_trip_add_place),
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.add),
                        contentDescription = stringResource(Res.string.planning_trip_add_place),
                    )
                },
            )
        },
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    PlanOverviewHeader(
                        name = planHeader.name,
                        onNameChange = onPlanNameChange,
                        startDateMillis = planHeader.period.start,
                        endDateMillis = planHeader.period.end,
                        onPlanDateRangeChanged = onPlanDateRangeChanged,
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
                    },
                ) { _, place ->
                    DraggableItem(
                        state = dragAndDropState,
                        key = place.id, // Unique key for each draggable item
                        data = place,
                    ) {
                        PlanPlaceRow(
                            name = place.name,
                            onDeleteClick = {
                                onDeletePermanentPlaceClick(place.id)
                            },
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
                    },
                ) { _, day ->
                    PlanDayItem(
                        modifier = Modifier.dropTarget(
                            state = dragAndDropState,
                            key = day.id,
                            onDrop = { state ->
                                val place = state.data
                                onPlaceMovedToDay(place.id, day.id)
                                place
                            },
                        ),
                        day = day.date,
                        placeSteps = day.placeSteps,
                        onDateClicked = {
                            onDateClicked(day.id)
                        },
                    )
                }
            }
        }
    }
}

//region Previews
@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun PlanOverviewPagePreview() = KTravelTheme {
    PlanOverviewContent(
        planHeader = PlanHeader(
            name = TextFieldValue("Viaggio in Italia"),
        ),
        places = persistentListOf(
            PlaceUi(id = "1", name = "Binasco", lat = 45.3328, lng = 9.1010),
            PlaceUi(id = "2", name = "Assago", lat = 45.4030, lng = 9.1290),
        ),
        days = persistentListOf(
            TravelDayUi(
                date = LocalDate(2024, 6, 15),
                steps = TravelDayStepPreviewParameterProvider(8).values.toList().toPersistentList(),
            ),
            TravelDayUi(
                date = LocalDate(2024, 6, 16),
            ),
        ),
        onBackClick = {},
        onPlanNameChange = {},
        onDeletePermanentPlaceClick = {},
        onPlanDateRangeChanged = { start, end -> },
        onAddPlaceClicked = {},
        onDateClicked = {},
        onPlaceMovedToDay = { _, _ -> },
        onSettingClicked = {},
        onExportClick = {},
        onExportMessageShown = {},
    )
}
//endregion Previews
