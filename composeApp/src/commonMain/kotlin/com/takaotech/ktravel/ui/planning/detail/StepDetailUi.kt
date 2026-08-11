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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.mikepenz.markdown.m3.Markdown
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.model.AttachmentReference
import com.takaotech.ktravel.presentation.planning.AttachmentUi
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.VisitScheduleUi
import com.takaotech.ktravel.presentation.planning.detail.StepDetailEvent
import com.takaotech.ktravel.presentation.planning.detail.StepDetailScreen
import com.takaotech.ktravel.presentation.planning.detail.StepDetailUiState
import com.takaotech.ktravel.ui.common.MAP_STYLE_URI
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.openFileWithDefaultApplication
import io.github.vinceglb.filekit.exists
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.io.files.Path
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.check
import ktravel.composeapp.generated.resources.edit
import ktravel.composeapp.generated.resources.error
import ktravel.composeapp.generated.resources.planning_detail_attachment_open_error
import ktravel.composeapp.generated.resources.planning_detail_attachments_missing
import ktravel.composeapp.generated.resources.planning_detail_cd_back
import ktravel.composeapp.generated.resources.planning_detail_cd_done_note
import ktravel.composeapp.generated.resources.planning_detail_cd_edit_note
import ktravel.composeapp.generated.resources.planning_detail_cd_note_invalid
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
    const val NOTE_VIEW = "step_detail_note_view"
    const val NOTE_EDITOR = "step_detail_note_editor"
    const val EDIT_TOGGLE = "step_detail_edit_toggle"
    const val NOTE_ALERT = "step_detail_note_alert"
    const val MISSING_REFERENCES = "step_detail_missing_references"
    const val BACK_BUTTON = "step_detail_back"
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
            isEditing = state.isEditing,
            noteInvalid = state.noteInvalid,
            missingReferences = state.missingReferences,
            resolveFile = state.resolveFile,
            modifier = modifier,
            onBack = { sink(StepDetailEvent.NavigateBack) },
            onToggleEdit = { sink(StepDetailEvent.ToggleEdit(it)) },
            onNoteChanged = { sink(StepDetailEvent.NoteChanged(it)) },
            onAddAttachment = { sink(StepDetailEvent.AddAttachment(it)) },
            onRemoveAttachment = { sink(StepDetailEvent.RemoveAttachment(it)) },
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
    isEditing: Boolean,
    noteInvalid: Boolean,
    missingReferences: List<String>,
    resolveFile: (String) -> PlatformFile,
    onBack: () -> Unit,
    onToggleEdit: (Boolean) -> Unit,
    onNoteChanged: (String) -> Unit,
    onAddAttachment: (PlatformFile) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onSetStartTime: (LocalTime) -> Unit,
    onSetEndTime: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Controller condiviso: pilota l'editor e riceve gli inserimenti al cursore dall'inventario.
    val controller = rememberMarkdownEditorController(place.note)
    LaunchedEffect(controller) {
        controller.markdownFlow.collect { onNoteChanged(it) }
    }

    val attachmentPicker = rememberFilePickerLauncher(type = FileKitType.File()) { file ->
        file?.let(onAddAttachment)
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val openErrorMessage = stringResource(Res.string.planning_detail_attachment_open_error)

    // OS-delegated opening (OS chooses app). A missing file or a platform failure such as
    // ActivityNotFoundException/FileNotFoundException surfaces as a timed snackbar.
    val openAttachment: (String) -> Unit =
        remember(resolveFile, scope, scaffoldState, openErrorMessage) {
            { relativePath ->
                try {
                    val file = resolveFile(relativePath)
                    if (file.exists()) {
                        FileKit.openFileWithDefaultApplication(file)
                    } else {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(openErrorMessage)
                        }
                    }
                } catch (e: Exception) {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(openErrorMessage)
                    }
                }
            }
        }

    val errorPainter = painterResource(Res.drawable.error)
    val imageTransformer = remember(resolveFile, errorPainter) {
        AttachmentImageTransformer(resolveFile = resolveFile, errorPainter = errorPainter)
    }

    // Handler custom: i link agli allegati aprono nativamente, gli altri seguono il comportamento di default.
    val defaultUriHandler = LocalUriHandler.current
    val uriHandler = remember(defaultUriHandler, openAttachment) {
        object : UriHandler {
            override fun openUri(uri: String) {
                val relativePath = AttachmentReference.relativePathOf(uri)
                if (relativePath != null) {
                    openAttachment(relativePath)
                } else {
                    defaultUriHandler.openUri(uri)
                }
            }
        }
    }

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
        sheetContent = {
            AttachmentInventorySection(
                modifier = Modifier
                    .navigationBarsPadding(),
                attachments = place.attachments,
                resolveFile = resolveFile,
                isEditing = isEditing,
                onAdd = { attachmentPicker.launch() },
                onInsert = { attachment ->
                    controller.insertAtCursor(attachment.toMarkdownReference())
                    onToggleEdit(true)
                },
                onOpen = { attachment -> openAttachment(attachment.relativePath) },
                onRemove = { attachment -> onRemoveAttachment(attachment.id) },
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

            NotesSection(
                note = place.note,
                isEditing = isEditing,
                noteInvalid = noteInvalid,
                missingReferences = missingReferences,
                controller = controller,
                imageTransformer = imageTransformer,
                uriHandler = uriHandler,
                onToggleEdit = onToggleEdit,
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

@Composable
private fun NotesSection(
    note: String,
    isEditing: Boolean,
    noteInvalid: Boolean,
    missingReferences: List<String>,
    controller: MarkdownEditorController,
    imageTransformer: AttachmentImageTransformer,
    uriHandler: UriHandler,
    onToggleEdit: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.planning_detail_step_note_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (noteInvalid) {
                Icon(
                    modifier = Modifier.testTag(StepDetailTestTags.NOTE_ALERT),
                    painter = painterResource(Res.drawable.error),
                    contentDescription = stringResource(Res.string.planning_detail_cd_note_invalid),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            IconButton(
                modifier = Modifier.testTag(StepDetailTestTags.EDIT_TOGGLE),
                onClick = { onToggleEdit(!isEditing) },
            ) {
                if (isEditing) {
                    Icon(
                        painter = painterResource(Res.drawable.check),
                        contentDescription = stringResource(Res.string.planning_detail_cd_done_note),
                    )
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.edit),
                        contentDescription = stringResource(Res.string.planning_detail_cd_edit_note),
                    )
                }
            }
        }

        if (missingReferences.isNotEmpty()) {
            Text(
                modifier = Modifier.testTag(StepDetailTestTags.MISSING_REFERENCES),
                text = stringResource(Res.string.planning_detail_attachments_missing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (isEditing) {
            MarkdownNoteEditor(
                controller = controller,
                label = stringResource(Res.string.planning_detail_step_note_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(StepDetailTestTags.NOTE_EDITOR),
            )
        } else if (note.isBlank()) {
            Text(
                modifier = Modifier.testTag(StepDetailTestTags.NOTE_VIEW),
                text = stringResource(Res.string.planning_detail_step_note_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                Markdown(
                    content = note,
                    imageTransformer = imageTransformer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(StepDetailTestTags.NOTE_VIEW),
                )
            }
        }
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

/** Snippet Markdown per referenziare l'allegato: immagine inline o link a file. */
private fun AttachmentUi.toMarkdownReference(): String = if (isImage) {
    AttachmentReference.imageMarkdown(relativePath, altText = originalName)
} else {
    AttachmentReference.fileMarkdown(relativePath, label = originalName)
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
        isEditing = false,
        noteInvalid = false,
        missingReferences = emptyList(),
        resolveFile = { PlatformFile(Path(it)) },
        onBack = {},
        onToggleEdit = {},
        onNoteChanged = {},
        onAddAttachment = {},
        onRemoveAttachment = {},
        onSetStartTime = {},
        onSetEndTime = {},
    )
}

@PreviewScreenSizes
@Composable
private fun StepDetailPlaceContentEmptyNotePreview() = KTravelTheme {
    StepDetailPlaceContent(
        place = StepUi.Place(name = "Shibuya Crossing", lat = 35.6595, lng = 139.7005),
        isEditing = false,
        noteInvalid = true,
        missingReferences = listOf("t1/s1/missing.jpg"),
        resolveFile = { PlatformFile(Path(it)) },
        onBack = {},
        onToggleEdit = {},
        onNoteChanged = {},
        onAddAttachment = {},
        onRemoveAttachment = {},
        onSetStartTime = {},
        onSetEndTime = {},
    )
}
