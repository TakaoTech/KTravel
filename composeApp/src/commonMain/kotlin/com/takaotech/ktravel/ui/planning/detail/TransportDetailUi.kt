@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)

package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.PanelHorizontalDivided
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.RoutingSection
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.presentation.planning.detail.StepNotesEvent
import com.takaotech.ktravel.presentation.planning.detail.StepNotesUiState
import com.takaotech.ktravel.presentation.planning.detail.TransportDetailEvent
import com.takaotech.ktravel.presentation.planning.detail.TransportDetailScreen
import com.takaotech.ktravel.presentation.planning.detail.TransportDetailUiState
import com.takaotech.ktravel.ui.common.formatClock
import com.takaotech.ktravel.ui.common.formatDayMonthYearClockHere
import com.takaotech.ktravel.ui.common.toColorOrNull
import com.takaotech.ktravel.ui.planning.transport.labelOrNull
import com.takaotech.ktravel.ui.planning.transport.preview.RoutePreviewMap
import com.takaotech.ktravel.ui.planning.transport.preview.RoutePreviewPath
import com.takaotech.ktravel.ui.planning.transport.preview.RoutingStepSection
import com.takaotech.ktravel.ui.planning.transport.preview.TransitStepRow
import com.takaotech.ktravel.ui.planning.transport.preview.formatDistance
import com.takaotech.ktravel.ui.theme.KTravelTheme
import com.takaotech.navigator.api.geometry.PolylineEncoderDecoder
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.edit
import ktravel.composeapp.generated.resources.flag
import ktravel.composeapp.generated.resources.keyboard_arrow_down
import ktravel.composeapp.generated.resources.map
import ktravel.composeapp.generated.resources.place
import ktravel.composeapp.generated.resources.planning_detail_cd_back
import ktravel.composeapp.generated.resources.schedule
import ktravel.composeapp.generated.resources.transport_detail_arrival_label
import ktravel.composeapp.generated.resources.transport_detail_calculated_at
import ktravel.composeapp.generated.resources.transport_detail_cd_collapse_steps
import ktravel.composeapp.generated.resources.transport_detail_cd_expand_steps
import ktravel.composeapp.generated.resources.transport_detail_cd_recalculate
import ktravel.composeapp.generated.resources.transport_detail_departure_label
import ktravel.composeapp.generated.resources.transport_detail_distance_label
import ktravel.composeapp.generated.resources.transport_detail_duration_label
import ktravel.composeapp.generated.resources.transport_detail_note_empty
import ktravel.composeapp.generated.resources.transport_detail_note_label
import ktravel.composeapp.generated.resources.transport_detail_note_title
import ktravel.composeapp.generated.resources.transport_detail_steps_title
import ktravel.composeapp.generated.resources.transport_detail_time_unknown
import ktravel.composeapp.generated.resources.transport_detail_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import com.takaotech.ktravel.presentation.planning.detail.TransportDetailUi as TransportDetailUiModel

internal object TransportDetailTestTags {
    const val MAP = "transport_detail_map"
    const val BACK_BUTTON = "transport_detail_back"
    const val RECALCULATE = "transport_detail_recalculate"
    const val SEGMENT_HEAD = "transport_detail_segment"
    const val METRICS = "transport_detail_metrics"
    const val CALCULATED_AT = "transport_detail_calculated_at"
    const val STEPS_TOGGLE = "transport_detail_steps_toggle"
    const val MANOEUVRES = "transport_detail_manoeuvres"
    const val TRANSIT_TIMELINE = "transport_detail_transit_timeline"

    val NOTES = StepNotesTestTags(
        noteView = "transport_detail_note_view",
        noteEditor = "transport_detail_note_editor",
        editToggle = "transport_detail_edit_toggle",
        noteAlert = "transport_detail_note_alert",
        missingReferences = "transport_detail_missing_references",
        attachments = "transport_detail_attachments",
        attachmentAdd = "transport_detail_attachment_add",
        attachmentItem = "transport_detail_attachment_item",
    )
}

/** Zoom applied when framing a single point of the route, close enough to read the junction. */
private const val POINT_FOCUS_ZOOM = 16.0

/** Height of the map above the panel on a narrow window. */
private val COMPACT_MAP_HEIGHT = 220.dp

/**
 * Circuit `Ui` of the transport detail (registered through Metro `@CircuitInject`).
 *
 * Shows a leg the traveller already saved: where it starts and ends and when, how long and how far
 * it runs, the notes taken on it, and the way through — turn by turn for a road route, stop by stop
 * for a journey on scheduled services.
 */
@CircuitInject(TransportDetailScreen::class, AppScope::class)
@Composable
fun TransportDetailUi(state: TransportDetailUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    val transport = state.transport
    if (transport == null) {
        TransportDetailLoading(
            modifier = modifier,
            onBack = { sink(TransportDetailEvent.NavigateBack) },
        )
        return
    }

    val cameraState = rememberCameraState()
    val scope = rememberCoroutineScope()
    var focus by remember { mutableStateOf<PolylineEncoderDecoder.LatLngZ?>(null) }
    LaunchedEffect(transport.answer) { focus = null }

    val paths = remember(transport.answer) {
        transport.answer.paths
            .map { RoutePreviewPath(polyline = it.polyline, color = it.colorHex?.toColorOrNull()) }
            .toPersistentList()
    }

    TransportDetailContent(
        transport = transport,
        notes = state.notes,
        canRecalculate = state.canRecalculate,
        modifier = modifier,
        onBack = { sink(TransportDetailEvent.NavigateBack) },
        onRecalculate = { sink(TransportDetailEvent.Recalculate) },
        onNotesEvent = { sink(TransportDetailEvent.Notes(it)) },
        onPointClick = { point ->
            focus = point
            scope.launch {
                cameraState.animateTo(
                    CameraPosition(
                        target = Position(longitude = point.lng, latitude = point.lat),
                        zoom = POINT_FOCUS_ZOOM,
                    ),
                )
            }
        },
        map = { mapModifier ->
            RoutePreviewMap(
                modifier = mapModifier.testTag(TransportDetailTestTags.MAP),
                enabled = true,
                paths = paths,
                cameraState = cameraState,
                focus = focus,
            )
        },
    )
}

@Composable
private fun TransportDetailLoading(onBack: () -> Unit, modifier: Modifier = Modifier) {
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

/**
 * Stateless content of the screen.
 *
 * Map and panel sit side by side on a wide window and stacked on a narrow one, and the panel is a
 * single lazy list: the way through can be hundreds of rows, and it has to scroll with the metrics
 * above it rather than inside a box of its own.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun TransportDetailContent(
    transport: TransportDetailUiModel,
    notes: StepNotesUiState,
    canRecalculate: Boolean,
    onBack: () -> Unit,
    onRecalculate: () -> Unit,
    onNotesEvent: (StepNotesEvent) -> Unit,
    modifier: Modifier = Modifier,
    onPointClick: (PolylineEncoderDecoder.LatLngZ) -> Unit = {},
    map: @Composable (Modifier) -> Unit = {},
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val host = rememberStepNotesHost(
        state = notes,
        snackbarHostState = scaffoldState.snackbarHostState,
        onEvent = onNotesEvent,
    )

    val wide = currentWindowAdaptiveInfoV2().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.navigationBarsPadding(),
                hostState = it,
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.transport_detail_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag(TransportDetailTestTags.RECALCULATE),
                        enabled = canRecalculate,
                        onClick = onRecalculate,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.edit),
                            contentDescription = stringResource(Res.string.transport_detail_cd_recalculate),
                        )
                    }
                },
            )
        },
        sheetPeekHeight = ATTACHMENT_INVENTORY_PEEK_HEIGHT,
        sheetContent = {
            AttachmentInventorySection(
                modifier = Modifier.navigationBarsPadding(),
                attachments = notes.attachments,
                resolveFile = notes.resolveFile,
                isEditing = notes.isEditing,
                testTags = TransportDetailTestTags.NOTES,
                onAdd = host.pickAttachment,
                onInsert = host.insertReference,
                onOpen = { attachment -> host.openAttachment(attachment.relativePath) },
                onRemove = { attachment -> onNotesEvent(StepNotesEvent.RemoveAttachment(attachment.id)) },
            )
        },
    ) { padding ->
        if (wide) {
            // TODO To be explore if this graphic is okay
            PanelHorizontalDivided(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding()),
                mainPane = {
                    AnimatedPane {
                        TransportDetailPanel(
                            modifier = Modifier
                                .fillMaxHeight(),
                            contentPadding = padding,
                            transport = transport,
                            host = host,
                            notes = notes,
                            onPointClick = onPointClick,
                            onNotesEvent = onNotesEvent,
                            map = null,
                        )
                    }
                },
                supportingPane = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(),
                    ) { map(Modifier.fillMaxSize()) }
                },
            )
        } else {
            TransportDetailPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = padding.calculateTopPadding()),
                contentPadding = padding,
                transport = transport,
                host = host,
                notes = notes,
                onPointClick = onPointClick,
                onNotesEvent = onNotesEvent,
                map = {
                    map(
                        Modifier
                            .fillMaxWidth()
                            .height(COMPACT_MAP_HEIGHT)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                },
            )
        }
    }
}

@Composable
private fun TransportDetailPanel(
    modifier: Modifier,
    contentPadding: PaddingValues,
    transport: TransportDetailUiModel,
    host: StepNotesHost,
    notes: StepNotesUiState,
    map: (@Composable LazyItemScope.() -> Unit)?,
    onPointClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    onNotesEvent: (StepNotesEvent) -> Unit,
) {
    var stepsExpanded by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        if (map != null) {
            item(content = map)
        }

        item {
            SegmentHead(
                transport = transport,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
        item {
            Metrics(
                transport = transport,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item {
            StepNotesSection(
                host = host,
                state = notes,
                title = stringResource(Res.string.transport_detail_note_title),
                editorLabel = stringResource(Res.string.transport_detail_note_label),
                emptyText = stringResource(Res.string.transport_detail_note_empty),
                testTags = TransportDetailTestTags.NOTES,
                onEvent = onNotesEvent,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }
        stepsSection(
            answer = transport.answer,
            expanded = stepsExpanded,
            onToggleExpanded = { stepsExpanded = !stepsExpanded },
            onPointClick = onPointClick,
        )
        item { Spacer(Modifier.height(24.dp)) }
    }
}

/**
 * Where the leg starts and ends, and when.
 *
 * Two rows rather than one line: the times belong to the endpoints and reading them off a single
 * `A → B` row means guessing which one each belongs to. Between them, the vehicle that covers the
 * distance.
 */
@Composable
private fun SegmentHead(transport: TransportDetailUiModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.testTag(TransportDetailTestTags.SEGMENT_HEAD),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Endpoint(
            icon = Res.drawable.place,
            tint = MaterialTheme.colorScheme.primary,
            label = stringResource(Res.string.transport_detail_departure_label),
            name = transport.fromName,
            time = transport.departure,
        )

        Row(
            modifier = Modifier.padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            ModeChip(transport)
        }

        Endpoint(
            icon = Res.drawable.flag,
            tint = MaterialTheme.colorScheme.tertiary,
            label = stringResource(Res.string.transport_detail_arrival_label),
            name = transport.toName,
            time = transport.arrival,
        )
    }
}

@Composable
private fun Endpoint(icon: DrawableResource, tint: Color, label: String, name: String, time: LocalDateTime?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = time?.formatClock() ?: stringResource(Res.string.transport_detail_time_unknown),
            style = MaterialTheme.typography.titleMedium,
            color = if (time != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

/** The vehicle the leg is covered in, named the way the navigator named it when it can be. */
@Composable
private fun ModeChip(transport: TransportDetailUiModel) {
    val label = transport.answer.principalMode?.let { RoutingMode(it.uppercase()).labelOrNull() }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(transport.type.toIcon()),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = label?.let { stringResource(it) } ?: transport.type.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/** How long and how far, and — when the plan knows it — when the answer was computed. */
@Composable
private fun Metrics(transport: TransportDetailUiModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .testTag(TransportDetailTestTags.METRICS),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Metric(
                modifier = Modifier.weight(1f),
                icon = Res.drawable.schedule,
                value = transport.totalDuration.toString(),
                label = stringResource(Res.string.transport_detail_duration_label),
            )
            Metric(
                modifier = Modifier.weight(1f),
                icon = Res.drawable.map,
                value = transport.totalDistance.formatDistance(),
                label = stringResource(Res.string.transport_detail_distance_label),
            )
        }

        transport.calculatedAt?.let { calculatedAt ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 9.dp)
                    .testTag(TransportDetailTestTags.CALCULATED_AT),
                text = stringResource(
                    Res.string.transport_detail_calculated_at,
                    calculatedAt.formatDayMonthYearClockHere(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Metric(icon: DrawableResource, value: String, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column {
            Text(text = value, style = MaterialTheme.typography.titleMedium)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * The way through, collapsible, in whichever of the two forms the answer was given.
 *
 * A road answer is a list of manoeuvres, which can run to hundreds of rows and is therefore
 * contributed to the caller's list one section at a time. A journey has no manoeuvres at all: it is
 * drawn with the same rows the preview screen uses, a handful of them, so it fits in a single item.
 */
private fun LazyListScope.stepsSection(
    answer: TransportAnswer,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onPointClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
) {
    item {
        val stepCount = remember(answer) {
            when (answer) {
                is TransportAnswer.Routing -> answer.route.sections.sumOf { it.actions.size }
                is TransportAnswer.Transit -> answer.journey.steps.size
            }
        }
        StepsHeader(stepCount = stepCount, expanded = expanded, onToggle = onToggleExpanded)
    }

    if (expanded) {
        // TODO Change with AnimatedVisibility
        when (answer) {
            is TransportAnswer.Routing -> items(answer.route.sections) { section ->
                RoutingStepSection(
                    modifier = Modifier.testTag(TransportDetailTestTags.MANOEUVRES),
                    actions = section.actions,
                    polyline = section.polyline,
                    onActionClick = onPointClick,
                )
            }

            is TransportAnswer.Transit -> item {
                Column(modifier = Modifier.testTag(TransportDetailTestTags.TRANSIT_TIMELINE)) {
                    answer.journey.steps.forEach { step ->
                        TransitStepRow(step = step, onStopClick = onPointClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepsHeader(stepCount: Int, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag(TransportDetailTestTags.STEPS_TOGGLE)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = pluralStringResource(
                Res.plurals.transport_detail_steps_title,
                stepCount,
                stepCount,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val animatedRotation by animateFloatAsState(
            targetValue = if (expanded) 0f else -90f,
            label = "rotationExansionNotes",
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )

        Icon(
            modifier = Modifier
                .rotate(animatedRotation),
            painter = painterResource(Res.drawable.keyboard_arrow_down),
            contentDescription = stringResource(
                if (expanded) {
                    Res.string.transport_detail_cd_collapse_steps
                } else {
                    Res.string.transport_detail_cd_expand_steps
                },
            ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        modifier = Modifier.testTag(TransportDetailTestTags.BACK_BUTTON),
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
private fun TransportDetailRoadPreview() = KTravelTheme {
    TransportDetailContent(
        transport = TransportDetailUiModel(
            type = TransportType.CAR,
            fromName = "Binasco",
            toName = "Assago",
            answer = TransportAnswer.Routing(
                RoutingRoute(
                    summary = RouteSummary(849.seconds, 10400.0 * Length.meters),
                    sections = listOf(
                        RoutingSection(
                            summary = RouteSummary(849.seconds, 10400.0 * Length.meters),
                            mode = "car",
                        ),
                    ),
                ),
            ),
            totalDuration = 849.seconds,
            totalDistance = 10400.0 * Length.meters,
        ),
        notes = StepNotesUiState(note = "Pieno prima della A7, sosta pranzo a Binasco."),
        canRecalculate = true,
        onBack = {},
        onRecalculate = {},
        onNotesEvent = {},
    )
}

@PreviewScreenSizes
@Composable
private fun TransportDetailTransitPreview() = KTravelTheme {
    TransportDetailContent(
        transport = TransportDetailUiModel(
            type = TransportType.TRAIN,
            fromName = "Duomo",
            toName = "Sesto",
            answer = TransportAnswer.Transit(
                TransitJourney(
                    summary = RouteSummary(1800.seconds, 9000.0 * Length.meters),
                    steps = listOf(
                        TransitStep.Walk(
                            summary = RouteSummary(
                                480.seconds,
                                600.0 * Length.meters,
                            ),
                        ),
                        TransitStep.Ride(
                            summary = RouteSummary(1320.seconds, 8400.0 * Length.meters),
                            line = TransitLine(mode = "SUBWAY", name = "M1", color = "#FF0000"),
                        ),
                    ),
                ),
            ),
            totalDuration = 1800.seconds,
            totalDistance = 9000.0 * Length.meters,
        ),
        notes = StepNotesUiState(),
        canRecalculate = false,
        onBack = {},
        onRecalculate = {},
        onNotesEvent = {},
    )
}
