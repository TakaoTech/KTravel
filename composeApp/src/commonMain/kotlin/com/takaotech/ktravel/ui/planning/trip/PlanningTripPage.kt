package com.takaotech.ktravel.ui.planning.trip

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.takaotech.ktravel.core.ui.preview.TravelDayStepPreviewParameterProvider
import com.takaotech.ktravel.data.archive.TravelArchiveFormat
import com.takaotech.ktravel.presentation.planning.ExportUiState
import com.takaotech.ktravel.presentation.planning.PlaceUi
import com.takaotech.ktravel.presentation.planning.PlanHeader
import com.takaotech.ktravel.presentation.planning.PlanningViewModel
import com.takaotech.ktravel.presentation.planning.TravelDayUi
import com.takaotech.ktravel.ui.common.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.common.message
import com.takaotech.ktravel.ui.common.rememberDisruptiveOperationDialog
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
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
import ktravel.composeapp.generated.resources.file_export
import ktravel.composeapp.generated.resources.planning_trip_add_place
import ktravel.composeapp.generated.resources.planning_trip_cd_back
import ktravel.composeapp.generated.resources.planning_trip_cd_delete_place
import ktravel.composeapp.generated.resources.planning_trip_cd_export
import ktravel.composeapp.generated.resources.planning_trip_cd_export_animation
import ktravel.composeapp.generated.resources.planning_trip_cd_export_completed_animation
import ktravel.composeapp.generated.resources.planning_trip_cd_settings
import ktravel.composeapp.generated.resources.planning_trip_export_in_progress
import ktravel.composeapp.generated.resources.planning_trip_export_success
import ktravel.composeapp.generated.resources.planning_trip_export_success_partial
import ktravel.composeapp.generated.resources.planning_trip_itinerary_title
import ktravel.composeapp.generated.resources.planning_trip_places_title
import ktravel.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@Serializable
data class PlanningTripPageNavigation(val travelId: String)

internal object PlanningTripTestTags {
    const val EXPORT = "planning_trip_export"
}

/** Messaggio da mostrare all'utente, o null se non c'è nulla da comunicare. */
@Composable
private fun ExportUiState.message(): String? = when (this) {
    ExportUiState.Idle, ExportUiState.InProgress -> null

    is ExportUiState.Completed -> if (skippedAttachments == 0) {
        stringResource(Res.string.planning_trip_export_success)
    } else {
        stringResource(Res.string.planning_trip_export_success_partial, skippedAttachments)
    }

    is ExportUiState.Failed -> error.message()
}

/**
 * Nome file proposto dal saver: il nome del viaggio è testo libero e su alcune piattaforme un
 * separatore di percorso lo rende inutilizzabile.
 */
internal fun String.toArchiveFileName(): String = replace(Regex("""[^\p{L}\p{N} _-]"""), "").trim().ifEmpty { "travel" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningTripPage(
    viewModel: PlanningViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
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
        state = deleteDialogState,
    )

    // Il file saver appartiene alla pagina: la destinazione scelta va consegnata al ViewModel, che
    // è l'unico a sapere quale viaggio esportare.
    val exportLauncher = rememberFileSaverLauncher(FileKitDialogSettings.createDefault()) { file ->
        file?.let(viewModel::exportTravel)
    }

    PlanningTripPage(
        modifier = modifier,
        planHeader = planHeader,
        places = uiState.places,
        days = days,
        exportState = uiState.export,
        onBackClick = onBackClick,
        onExportClick = {
            exportLauncher.launch(
                suggestedName = planHeader.name.text.toArchiveFileName(),
                extension = TravelArchiveFormat.FILE_EXTENSION,
            )
        },
        onExportMessageShown = viewModel::onExportMessageShown,
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
        onSettingClicked = onSettingClicked,
    )
}

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlanningTripPage(
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
            ExportUiState.Idle, is ExportUiState.Completed -> Unit
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
                        modifier = Modifier.testTag(PlanningTripTestTags.EXPORT),
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
                    PlanningHeader(
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
                        TripPlaceRow(
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

/**
 * Native size of `files/lottie_train_export.json` and `files/lottie_train_station_export.json`, so the animations
 * render without empty margins.
 */
private const val EXPORT_ANIMATION_ASPECT_RATIO = 1920f / 651f

/**
 * Blocking feedback while the archive is being written: the export cannot be cancelled, so the
 * dialog ignores back press and outside taps.
 *
 * When [isCompleted] turns true the running train slides to the right and is replaced by the train
 * arriving at the station; [onCompletionAnimationEnd] fires once that arrival has played through,
 * so the caller can dismiss the dialog.
 */
@Composable
private fun ExportLoadingDialog(isCompleted: Boolean, onCompletionAnimationEnd: () -> Unit) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            // Both compositions are loaded upfront: the arrival must be ready when the transition
            // starts, otherwise the slide would carry an empty frame.
            val inProgressComposition by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/lottie_train_export.json").decodeToString(),
                )
            }
            val completedComposition by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/lottie_train_station_export.json").decodeToString(),
                )
            }

            AnimatedContent(
                targetState = isCompleted,
                transitionSpec = {
                    // The train keeps travelling to the right: the arrival enters from the left
                    // while the running train leaves through the right edge.
                    slideInHorizontally { width -> -width } togetherWith
                        slideOutHorizontally { width -> width }
                },
            ) { completed ->
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (completed) {
                        ExportTrainAnimation(
                            composition = completedComposition,
                            iterations = 1,
                            contentDescription = stringResource(
                                Res.string.planning_trip_cd_export_completed_animation,
                            ),
                            onAnimationEnd = onCompletionAnimationEnd,
                        )

                        Text(
                            text = stringResource(Res.string.planning_trip_export_success),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    } else {
                        ExportTrainAnimation(
                            composition = inProgressComposition,
                            iterations = Compottie.IterateForever,
                            contentDescription = stringResource(
                                Res.string.planning_trip_cd_export_animation,
                            ),
                        )

                        Text(
                            text = stringResource(Res.string.planning_trip_export_in_progress),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Plays [composition] for [iterations] and notifies [onAnimationEnd] when the playback is over.
 * The callback never fires for [Compottie.IterateForever] nor when the animation is cancelled by
 * leaving the composition.
 */
@Composable
private fun ExportTrainAnimation(
    composition: LottieComposition?,
    iterations: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = { },
) {
    val animatable = rememberLottieAnimatable()
    val currentOnAnimationEnd by rememberUpdatedState(onAnimationEnd)

    LaunchedEffect(composition, iterations) {
        animatable.animate(
            composition = composition ?: return@LaunchedEffect,
            iterations = iterations,
        )
        currentOnAnimationEnd()
    }

    Image(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(EXPORT_ANIMATION_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.large),
        painter = rememberLottiePainter(
            composition = composition,
            progress = animatable::value,
        ),
        contentDescription = contentDescription,
    )
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

/**
 * A place of the trip backlog, not assigned to any day yet. It is dragged onto a [PlanDayItem] to
 * plan it.
 */
@Composable
private fun TripPlaceRow(name: String, modifier: Modifier = Modifier, onDeleteClick: () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(start = 18.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = name,
                style = MaterialTheme.typography.bodyLarge,
            )

            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    contentDescription = stringResource(Res.string.planning_trip_cd_delete_place),
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

@PreviewLightDark
@Composable
private fun ExportLoadingDialogPreview() = KTravelTheme {
    ExportLoadingDialog(
        isCompleted = false,
        onCompletionAnimationEnd = {},
    )
}

@PreviewLightDark
@Composable
private fun ExportCompletedDialogPreview() = KTravelTheme {
    ExportLoadingDialog(
        isCompleted = true,
        onCompletionAnimationEnd = {},
    )
}
