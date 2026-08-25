package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
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
import kotlin.time.Duration.Companion.seconds

/** Opening zoom of the place map: close enough to show the surrounding streets. */
private const val MARKER_ZOOM = 14.0

/** Base style of the place map, hoisted so it is not reallocated on every recomposition. */
private val MAP_BASE_STYLE = BaseStyle.Uri(MAP_STYLE_URI)

/** The map is a still picture of the place: the itinerary is where the map is driven. */
private val MAP_OPTIONS = MapOptions(gestureOptions = GestureOptions.AllDisabled)

/**
 * How long the map waits for the navigation enter transition before giving up on it.
 *
 * Building the map costs a frame this screen cannot spare while it is being animated in, and the
 * entry being animated only reaches `RESUMED` once the transition ends. A host that never resumes
 * its content must not keep the map out forever, hence the bound.
 */
private val MAP_TRANSITION_TIMEOUT = 1.seconds

internal object StepDetailTestTags {
    const val MAP = "step_detail_map"
    const val BACK_BUTTON = "step_detail_back"
    const val START_TIME_FIELD = "step_detail_start_time"
    const val END_TIME_FIELD = "step_detail_end_time"

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
 *
 * Held back until the screen has finished being animated in: `MapLibre.getInstance` loads the native
 * library and `MapView` creates its surface, both on the main thread, and doing that mid transition
 * is what makes opening this page stutter. The placeholder keeps the space so nothing jumps.
 */
@Composable
private fun PlaceMap(lat: Double, lng: Double, modifier: Modifier = Modifier) {
    // Ready once this screen is resumed, and ready regardless after MAP_TRANSITION_TIMEOUT; never
    // goes back — a map that is up stays up.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var mapReady by remember { mutableStateOf(false) }
    LaunchedEffect(lifecycle) {
        if (!mapReady) {
            withTimeoutOrNull(MAP_TRANSITION_TIMEOUT) {
                lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            }
            mapReady = true
        }
    }

    if (LocalInspectionMode.current || !mapReady) {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant) {}
        return
    }

    val firstPosition = remember {
        CameraPosition(target = Position(longitude = lng, latitude = lat), zoom = MARKER_ZOOM)
    }
    val cameraState = rememberCameraState(firstPosition = firstPosition)

    LaunchedEffect(lat, lng) {
        val position = CameraPosition(
            target = Position(longitude = lng, latitude = lat),
            zoom = MARKER_ZOOM,
        )
        // The camera opens on the step already: only a later change of coordinates moves it.
        if (position != firstPosition) cameraState.position = position
    }

    MaplibreMap(
        modifier = modifier,
        baseStyle = MAP_BASE_STYLE,
        cameraState = cameraState,
        options = MAP_OPTIONS,
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

/** Gap between the two schedule fields, both when they sit side by side and when they stack. */
private val SCHEDULE_FIELD_SPACING = 16.dp

/** Horizontal content padding of a field, on both sides. */
private val SCHEDULE_FIELD_HORIZONTAL_PADDING = 16.dp

/** Gap between the label and the clock value inside a field. */
private val SCHEDULE_FIELD_INNER_SPACING = 8.dp

/**
 * Widths a schedule field needs: [inline] renders the label and the clock value on one line,
 * [wrapped] puts the value underneath the label.
 */
@Immutable
private data class ScheduleFieldWidths(val inline: Dp, val wrapped: Dp)

/**
 * Section that lets the user set the place arrival/departure times. Reuses the shared
 * [ScheduleTimeEditor] (Material3 time pickers + `departure >= arrival` validation); the unset
 * value shows the `--:--` placeholder.
 *
 * Layout degrades in two steps as the width per field shrinks — which happens both with a larger
 * font size and with a larger display size: the two fields first put their clock value under their
 * label, and only then stop sharing the row and stack full width. Without it a squeezed field
 * breaks its label and its clock value character by character.
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
            val widths = measureScheduleFieldWidths(scope)

            Text(
                text = stringResource(Res.string.planning_detail_schedule_title),
                style = MaterialTheme.typography.titleMedium,
            )

            BoxWithConstraints {
                val widthPerField = (maxWidth - SCHEDULE_FIELD_SPACING) / 2

                val stacked = widthPerField < widths.wrapped

                ScheduleFields(
                    scope = scope,
                    stacked = stacked,
                    fillWidth = !windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
                        WIDTH_DP_MEDIUM_LOWER_BOUND,
                    ),
                    // Both fields wrap together, so that they keep the same height.
                    stackContent = if (stacked) {
                        maxWidth < widths.inline
                    } else {
                        widthPerField < widths.inline
                    },
                )
            }
        }
    }
}

/**
 * Widths the widest of the two fields needs, measured rather than guessed: they depend on the
 * translation in use and on the current font scale.
 */
@Composable
private fun measureScheduleFieldWidths(scope: ScheduleTimeEditorScope): ScheduleFieldWidths {
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelLarge
    val valueStyle = MaterialTheme.typography.titleMedium
    val startLabel = stringResource(Res.string.planning_detail_start_time_label)
    val endLabel = stringResource(Res.string.planning_detail_end_time_label)
    val density = LocalDensity.current

    return remember(
        measurer,
        labelStyle,
        valueStyle,
        startLabel,
        endLabel,
        scope.startDisplay,
        scope.endDisplay,
        density,
    ) {
        fun labelWidth(text: String): Int = measurer.measure(text, labelStyle).size.width
        fun valueWidth(text: String): Int = measurer.measure(text, valueStyle).size.width

        val inline = maxOf(
            labelWidth(startLabel) + valueWidth(scope.startDisplay),
            labelWidth(endLabel) + valueWidth(scope.endDisplay),
        )
        val wrapped = maxOf(
            labelWidth(startLabel),
            labelWidth(endLabel),
            valueWidth(scope.startDisplay),
            valueWidth(scope.endDisplay),
        )

        with(density) {
            ScheduleFieldWidths(
                inline = inline.toDp() +
                    SCHEDULE_FIELD_INNER_SPACING +
                    SCHEDULE_FIELD_HORIZONTAL_PADDING * 2,
                wrapped = wrapped.toDp() + SCHEDULE_FIELD_HORIZONTAL_PADDING * 2,
            )
        }
    }
}

/**
 * The two schedule fields, side by side (each taking half of the row when [fillWidth] is set) or
 * [stacked] full width.
 */
@Composable
private fun ScheduleFields(
    scope: ScheduleTimeEditorScope,
    stacked: Boolean,
    fillWidth: Boolean,
    stackContent: Boolean,
) {
    val startField = @Composable { fieldModifier: Modifier ->
        ScheduleField(
            modifier = fieldModifier.testTag(StepDetailTestTags.START_TIME_FIELD),
            label = stringResource(Res.string.planning_detail_start_time_label),
            value = scope.startDisplay,
            stackContent = stackContent,
            onClick = scope.openStartPicker,
        )
    }
    val endField = @Composable { fieldModifier: Modifier ->
        ScheduleField(
            modifier = fieldModifier.testTag(StepDetailTestTags.END_TIME_FIELD),
            label = stringResource(Res.string.planning_detail_end_time_label),
            value = scope.endDisplay,
            stackContent = stackContent,
            onClick = scope.openEndPicker,
        )
    }

    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            startField(Modifier.fillMaxWidth())
            endField(Modifier.fillMaxWidth())
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(SCHEDULE_FIELD_SPACING)) {
            // On wider windows the fields keep their natural width, as before.
            val fieldModifier = if (fillWidth) Modifier.weight(1f) else Modifier
            startField(fieldModifier)
            endField(fieldModifier)
        }
    }
}

/**
 * Single schedule field. The corner radius is fixed rather than the percentage shape Material3
 * gives a button by default: that one degenerates into an ellipse as soon as the content wraps and
 * the button grows taller.
 */
@Composable
private fun ScheduleField(
    label: String,
    value: String,
    stackContent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelText = @Composable {
        Text(text = label, maxLines = 2, textAlign = TextAlign.Center)
    }
    // The clock value is atomic: it must never break into "09:" / "30".
    val valueText = @Composable {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            softWrap = false,
        )
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonDefaults.MinHeight),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(
            horizontal = SCHEDULE_FIELD_HORIZONTAL_PADDING,
            vertical = 8.dp,
        ),
    ) {
        if (stackContent) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                labelText()
                valueText()
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SCHEDULE_FIELD_INNER_SPACING),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                labelText()
                valueText()
            }
        }
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
@PreviewFontScale
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
@PreviewFontScale
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
