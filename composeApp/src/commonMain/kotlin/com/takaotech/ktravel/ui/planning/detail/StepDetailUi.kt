package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.VisitScheduleUi
import com.takaotech.ktravel.presentation.planning.detail.StepDetailEvent
import com.takaotech.ktravel.presentation.planning.detail.StepDetailScreen
import com.takaotech.ktravel.presentation.planning.detail.StepDetailUiState
import com.takaotech.ktravel.presentation.planning.detail.StepNotesEvent
import com.takaotech.ktravel.presentation.planning.detail.StepNotesUiState
import com.takaotech.ktravel.ui.common.MAP_STYLE_URI
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.planning_detail_cd_back
import ktravel.composeapp.generated.resources.planning_detail_end_time_label
import ktravel.composeapp.generated.resources.planning_detail_schedule_title
import ktravel.composeapp.generated.resources.planning_detail_start_time_label
import ktravel.composeapp.generated.resources.planning_detail_step_note_empty
import ktravel.composeapp.generated.resources.planning_detail_step_note_label
import ktravel.composeapp.generated.resources.planning_detail_step_note_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/** Opening zoom of the place map: close enough to show the surrounding streets. */
private const val MARKER_ZOOM = 14.0

internal object StepDetailTestTags {
    const val MAP = "step_detail_map"
    const val BACK_BUTTON = "step_detail_back"

    val NOTES = StepNotesTestTags(
        noteView = "step_detail_note_view",
        noteEditor = "step_detail_note_editor",
        editToggle = "step_detail_edit_toggle",
        noteAlert = "step_detail_note_alert",
        missingReferences = "step_detail_missing_references",
        attachments = "step_detail_attachments",
        attachmentAdd = "step_detail_attachment_add",
        attachmentItem = "step_detail_attachment_item",
    )
}

/**
 * `Ui` Circuit del dettaglio step (registrata via Metro `@CircuitInject`). Sviluppa la grafica per
 * [StepUi.Place]: nome, mappa con marker, note in Markdown (editor Hyphen + renderer mikepenz) e
 * inventario file dello step con riferimenti inseribili nel Markdown.
 */
@CircuitInject(StepDetailScreen::class, AppScope::class)
@Composable
fun StepDetailUi(state: StepDetailUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    val place = state.place
    if (place == null) {
        StepDetailLoading(modifier = modifier, onBack = { sink(StepDetailEvent.NavigateBack) })
    } else {
        StepDetailPlaceContent(
            place = place,
            notes = state.notes,
            modifier = modifier,
            onBack = { sink(StepDetailEvent.NavigateBack) },
            onNotesEvent = { sink(StepDetailEvent.Notes(it)) },
            onSetStartTime = { sink(StepDetailEvent.SetStartTime(it)) },
            onSetEndTime = { sink(StepDetailEvent.SetEndTime(it)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepDetailLoading(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = {}, navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StepDetailPlaceContent(
    place: StepUi.Place,
    notes: StepNotesUiState,
    onBack: () -> Unit,
    onNotesEvent: (StepNotesEvent) -> Unit,
    onSetStartTime: (LocalTime) -> Unit,
    onSetEndTime: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val host = rememberStepNotesHost(
        state = notes,
        snackbarHostState = scaffoldState.snackbarHostState,
        onEvent = onNotesEvent,
    )

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier
                    .navigationBarsPadding(),
                hostState = it,
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = place.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    BackButton(onClick = onBack)
                },
            )
        },
        sheetPeekHeight = ATTACHMENT_INVENTORY_PEEK_HEIGHT,
        sheetContent = {
            AttachmentInventorySection(
                modifier = Modifier
                    .navigationBarsPadding(),
                attachments = notes.attachments,
                resolveFile = notes.resolveFile,
                isEditing = notes.isEditing,
                testTags = StepDetailTestTags.NOTES,
                onAdd = host.pickAttachment,
                onInsert = host.insertReference,
                onOpen = { attachment -> host.openAttachment(attachment.relativePath) },
                onRemove = { attachment -> onNotesEvent(StepNotesEvent.RemoveAttachment(attachment.id)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlaceMap(
                lat = place.lat,
                lng = place.lng,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .testTag(StepDetailTestTags.MAP),
            )

            ScheduleSection(
                schedule = place.schedule,
                onSetStartTime = onSetStartTime,
                onSetEndTime = onSetEndTime,
            )

            StepNotesSection(
                host = host,
                state = notes,
                title = stringResource(Res.string.planning_detail_step_note_title),
                editorLabel = stringResource(Res.string.planning_detail_step_note_label),
                emptyText = stringResource(Res.string.planning_detail_step_note_empty),
                testTags = StepDetailTestTags.NOTES,
                onEvent = onNotesEvent,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Map of the place: a single marker, centred on first composition and re-centred whenever the
 * step's coordinates change.
 */
@Composable
private fun PlaceMap(lat: Double, lng: Double, modifier: Modifier = Modifier) {
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(longitude = lng, latitude = lat),
            zoom = MARKER_ZOOM,
        ),
    )

    LaunchedEffect(lat, lng) {
        cameraState.position = CameraPosition(
            target = Position(longitude = lng, latitude = lat),
            zoom = MARKER_ZOOM,
        )
    }

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri(MAP_STYLE_URI),
        cameraState = cameraState,
        options = MapOptions(
            gestureOptions = GestureOptions.AllDisabled,
        ),
    ) {
        val markerSource = rememberGeoJsonSource(
            data = GeoJsonData.Features(Point(Position(longitude = lng, latitude = lat))),
        )
        CircleLayer(
            id = "marker",
            source = markerSource,
            radius = const(8.dp),
            color = const(Color.Red),
            strokeColor = const(Color.White),
            strokeWidth = const(2.dp),
        )
    }
}

/**
 * Section that lets the user set the place arrival/departure times. Reuses the shared
 * [ScheduleTimeEditor] (Material3 time pickers + `departure >= arrival` validation), rendering two
 * full-width fields; the unset value shows the `--:--` placeholder.
 */
@Composable
private fun ScheduleSection(
    schedule: VisitScheduleUi?,
    onSetStartTime: (LocalTime) -> Unit,
    onSetEndTime: (LocalTime) -> Unit,
) {
    ScheduleTimeEditor(
        startTime = schedule?.startTime,
        endTime = schedule?.endTime,
        onStartConfirm = onSetStartTime,
        onEndConfirm = onSetEndTime,
    ) { scope ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()

            Text(
                text = stringResource(Res.string.planning_detail_schedule_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val modifier = if (windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
                        WIDTH_DP_MEDIUM_LOWER_BOUND,
                    )
                ) {
                    Modifier
                } else {
                    Modifier.weight(1f)
                }

                ScheduleField(
                    modifier = modifier,
                    label = stringResource(Res.string.planning_detail_start_time_label),
                    value = scope.startDisplay,
                    onClick = scope.openStartPicker,
                )
                ScheduleField(
                    modifier = modifier,
                    label = stringResource(Res.string.planning_detail_end_time_label),
                    value = scope.endDisplay,
                    onClick = scope.openEndPicker,
                )
            }
        }
    }
}

@Composable
private fun ScheduleField(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(text = label)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        modifier = Modifier.testTag(StepDetailTestTags.BACK_BUTTON),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(Res.drawable.arrow_back),
            contentDescription = stringResource(Res.string.planning_detail_cd_back),
        )
    }
}

@PreviewScreenSizes
@Composable
private fun StepDetailPlaceContentPreview() = KTravelTheme {
    StepDetailPlaceContent(
        place = StepUi.Place(
            name = "Tokyo Tower",
            lat = 35.6586,
            lng = 139.7454,
            schedule = VisitScheduleUi(
                startTime = LocalTime(9, 30),
                endTime = LocalTime(11, 0),
            ),
            note = "# Cose da vedere\n- Osservatorio principale\n- **Foto** al tramonto",
        ),
        notes = StepNotesUiState(note = "# Cose da vedere\n- Osservatorio principale\n- **Foto** al tramonto"),
        onBack = {},
        onNotesEvent = {},
        onSetStartTime = {},
        onSetEndTime = {},
    )
}

@PreviewScreenSizes
@Composable
private fun StepDetailPlaceContentEmptyNotePreview() = KTravelTheme {
    StepDetailPlaceContent(
        place = StepUi.Place(name = "Shibuya Crossing", lat = 35.6595, lng = 139.7005),
        notes = StepNotesUiState(
            noteInvalid = true,
            missingReferences = persistentListOf("t1/s1/missing.jpg"),
        ),
        onBack = {},
        onNotesEvent = {},
        onSetStartTime = {},
        onSetEndTime = {},
    )
}
