package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.core.ui.preview.TravelDayStepPreviewParameterProvider
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.detail.StepRow
import com.takaotech.ktravel.presentation.planning.detail.StepsPaneEvent
import com.takaotech.ktravel.presentation.planning.detail.StepsPaneScreen
import com.takaotech.ktravel.presentation.planning.detail.StepsPaneUiState
import com.takaotech.ktravel.presentation.planning.detail.buildStepRows
import com.takaotech.ktravel.ui.common.formatWeekdayDayMonthYear
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.flag
import ktravel.composeapp.generated.resources.flight
import ktravel.composeapp.generated.resources.place
import ktravel.composeapp.generated.resources.planning_detail_cd_back
import ktravel.composeapp.generated.resources.planning_detail_cd_open_backlog
import ktravel.composeapp.generated.resources.planning_detail_steps_empty
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal object StepsPaneTestTags {
    const val LIST = "steps_pane_list"
    const val EMPTY = "steps_pane_empty"
    const val BACK_BUTTON = "steps_pane_back"
    const val TITLE = "steps_pane_title"
    const val OPEN_BACKLOG_BUTTON = "steps_pane_open_backlog"
    const val TRANSPORT_DURATION = "steps_pane_transport_duration"
    fun addTransportTag(startPlaceId: String, endPlaceId: String) =
        "steps_pane_add_transport_${startPlaceId}_$endPlaceId"

    fun deleteStepTag(stepId: String) = "steps_pane_delete_step_$stepId"
    fun moveStepUpTag(stepId: String) = "steps_pane_move_step_up_$stepId"
    fun moveStepDownTag(stepId: String) = "steps_pane_move_step_down_$stepId"
}

// Timeline sizing (gutter with vertical line and nodes).
private val TimeColumnWidth = 64.dp
private val GutterWidth = 40.dp
private val NodeSize = 32.dp
private val NodeCenterY = 28.dp
private val LineThickness = 2.dp

/**
 * Circuit `Ui` of the itinerary pane (registered through Metro `@CircuitInject`): renders the state
 * and forwards events via `eventSink`, reusing the stateless [StepsPaneContent].
 */
@CircuitInject(StepsPaneScreen::class, AppScope::class)
@Composable
fun StepsPaneUi(state: StepsPaneUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    StepsPaneContent(
        rows = state.rows,
        dayDate = state.date,
        modifier = modifier,
        onNavigationBackClick = { sink(StepsPaneEvent.NavigateBack) },
        onOpenBacklogClick = { sink(StepsPaneEvent.OpenBacklog) },
        onStepClick = { sink(StepsPaneEvent.OpenStepDetail(it)) },
        onTransportClick = { sink(StepsPaneEvent.OpenTransportDetail(it)) },
        onDeleteStepClick = { sink(StepsPaneEvent.DeleteStep(it)) },
        onMoveStepUpClick = { sink(StepsPaneEvent.MoveStepUp(it)) },
        onMoveStepDownClick = { sink(StepsPaneEvent.MoveStepDown(it)) },
        onAddTransportClick = { startPlaceId, endPlaceId ->
            sink(StepsPaneEvent.AddTransport(startPlaceId, endPlaceId))
        },
        onSetArrivalTime = { stepId, time -> sink(StepsPaneEvent.SetStartTime(stepId, time)) },
        onSetDepartureTime = { stepId, time -> sink(StepsPaneEvent.SetEndTime(stepId, time)) },
    )
}

/**
 * Stateless content of the itinerary pane: renders the [rows] pre-computed by the presenter as a
 * vertical timeline, without any adjacency logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StepsPaneContent(
    rows: ImmutableList<StepRow>,
    onNavigationBackClick: () -> Unit,
    onOpenBacklogClick: () -> Unit,
    onStepClick: (String) -> Unit,
    onTransportClick: (String) -> Unit,
    onDeleteStepClick: (StepUi) -> Unit,
    onMoveStepUpClick: (String) -> Unit,
    onMoveStepDownClick: (String) -> Unit,
    onAddTransportClick: (startPlaceId: String, endPlaceId: String) -> Unit,
    onSetArrivalTime: (stepId: String, time: LocalTime) -> Unit,
    onSetDepartureTime: (stepId: String, time: LocalTime) -> Unit,
    dayDate: LocalDate? = null,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (dayDate != null) {
                        Text(
                            modifier = Modifier
                                .testTag(StepsPaneTestTags.TITLE)
                                .basicMarquee(),
                            text = dayDate.formatWeekdayDayMonthYear(),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag(StepsPaneTestTags.BACK_BUTTON),
                        onClick = onNavigationBackClick,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.planning_detail_cd_back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag(StepsPaneTestTags.OPEN_BACKLOG_BUTTON),
                        onClick = onOpenBacklogClick,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.flight),
                            contentDescription = stringResource(Res.string.planning_detail_cd_open_backlog),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (rows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier.testTag(StepsPaneTestTags.EMPTY),
                    text = stringResource(Res.string.planning_detail_steps_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            // Row of the last place: marked as the final destination only when there is an actual
            // journey (at least two places).
            val destinationIndex = remember(rows) {
                val placeCount = rows.count { it is StepRow.Step && it.step is StepUi.Place }
                if (placeCount >= 2) {
                    rows.indexOfLast { it is StepRow.Step && it.step is StepUi.Place }
                } else {
                    -1
                }
            }
            LazyColumn(
                modifier = Modifier.padding(padding).testTag(StepsPaneTestTags.LIST),
            ) {
                itemsIndexed(items = rows, key = { _, row -> row.key }) { index, row ->
                    val isFirst = index == 0
                    val isLast = index == rows.lastIndex
                    when (row) {
                        is StepRow.Step -> when (val step = row.step) {
                            is StepUi.Place -> {
                                // The final destination is the arrival point of the journey: it
                                // cannot hold a visit schedule, so no editable time column is shown.
                                val isDestination = index == destinationIndex
                                TimelineRow(
                                    isFirst = isFirst,
                                    isLast = isLast,
                                    timeColumn = {
                                        ScheduleTimeColumn(
                                            arrivalTime = step.schedule?.startTime,
                                            departureTime = step.schedule?.endTime,
                                            onArrivalConfirm = {
                                                onSetArrivalTime(
                                                    step.id,
                                                    it,
                                                )
                                            },
                                            onDepartureConfirm = {
                                                onSetDepartureTime(
                                                    step.id,
                                                    it,
                                                )
                                            },
                                        )
                                    },
                                    node = {
                                        if (isDestination) DestinationNode() else PlaceNode()
                                    },
                                ) {
                                    TravelStepPlace(
                                        step = step,
                                        onStepClick = onStepClick,
                                        onStepDeleteClicked = { onDeleteStepClick(step) },
                                        onStepMoveUp = onMoveStepUpClick,
                                        onStepMoveDown = onMoveStepDownClick,
                                    )
                                }
                            }

                            is StepUi.Transport -> TimelineRow(
                                isFirst = isFirst,
                                isLast = isLast,
                                node = { TransportNode(step) },
                            ) {
                                TravelStepTransport(
                                    modifier = Modifier.fillMaxWidth(),
                                    step = step,
                                    onStepClick = onTransportClick,
                                    onStepDeleteClicked = { onDeleteStepClick(step) },
                                )
                            }
                        }

                        is StepRow.AddTransportSlot -> TimelineRow(
                            isFirst = isFirst,
                            isLast = isLast,
                            node = { AddTransportNode() },
                        ) {
                            TravelTransportStepAdd(
                                modifier = Modifier.testTag(
                                    StepsPaneTestTags.addTransportTag(
                                        row.startPlaceId,
                                        row.endPlaceId,
                                    ),
                                ),
                                onClick = { onAddTransportClick(row.startPlaceId, row.endPlaceId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Timeline row: optional [timeColumn] slot on the left, gutter with a continuous vertical line and
 * the centered [node], and [content] on the right. The line is trimmed at the node when [isFirst] /
 * [isLast].
 */
@Composable
private fun TimelineRow(
    isFirst: Boolean,
    isLast: Boolean,
    node: @Composable BoxScope.() -> Unit,
    timeColumn: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
    ) {
        Box(modifier = Modifier.width(TimeColumnWidth).fillMaxHeight()) {
            if (timeColumn != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = NodeCenterY - 16.dp,
                            end = 4.dp,
                        ),
                ) {
                    timeColumn()
                }
            }
        }

        Box(modifier = Modifier.width(GutterWidth).fillMaxHeight()) {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(LineThickness)
                    .align(Alignment.TopCenter),
            ) {
                val centerY = NodeCenterY.toPx()
                val top = if (isFirst) centerY else 0f
                val bottom = if (isLast) centerY else size.height
                drawLine(
                    color = lineColor,
                    start = Offset(size.width / 2f, top),
                    end = Offset(size.width / 2f, bottom),
                    strokeWidth = size.width,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = NodeCenterY - NodeSize / 2)
                    .size(NodeSize),
                contentAlignment = Alignment.Center,
                content = node,
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
                .padding(end = 16.dp),
        ) {
            content()
        }
    }
}

/** Node of a place: filled circle with a "place" icon. */
@Composable
private fun BoxScope.PlaceNode() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(Res.drawable.place),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

/** Node of the final destination: accented circle with a flag icon. */
@Composable
private fun BoxScope.DestinationNode() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(Res.drawable.flag),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiary,
        )
    }
}

/** Node of a transport: the vehicle icon over a `surface` background that punches through the line. */
@Composable
private fun TransportNode(step: StepUi.Transport) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(step.type.toIcon()),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Node of the "add transport" slot: a small, unobtrusive dot on the line. */
@Composable
private fun AddTransportNode() {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

/**
 * Editable time column of a place row: arrival above, departure below. Compact presentation of the
 * shared [ScheduleTimeEditor]: each time is a clickable label opening the Material3 time picker; an
 * unset time shows the `--:--` placeholder yet stays clickable.
 */
@Composable
private fun ScheduleTimeColumn(
    arrivalTime: LocalTime?,
    departureTime: LocalTime?,
    onArrivalConfirm: (LocalTime) -> Unit,
    onDepartureConfirm: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScheduleTimeEditor(
        startTime = arrivalTime,
        endTime = departureTime,
        onStartConfirm = onArrivalConfirm,
        onEndConfirm = onDepartureConfirm,
    ) { scope ->
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                modifier = Modifier
                    .clickable(onClick = scope.openStartPicker)
                    .minimumInteractiveComponentSize()
                    .basicMarquee()
                    .semantics { contentDescription = scope.startContentDescription },
                text = scope.startDisplay,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                modifier = Modifier
                    .clickable(onClick = scope.openEndPicker)
                    .minimumInteractiveComponentSize()
                    .basicMarquee()
                    .semantics { contentDescription = scope.endContentDescription },
                text = scope.endDisplay,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun StepsPaneContentPreview() = KTravelTheme {
    StepsPaneContent(
        rows = buildStepRows(TravelDayStepPreviewParameterProvider(8).values.toList()),
        onNavigationBackClick = {},
        onOpenBacklogClick = {},
        onStepClick = {},
        onTransportClick = {},
        onDeleteStepClick = {},
        onMoveStepUpClick = {},
        onMoveStepDownClick = {},
        onAddTransportClick = { _, _ -> },
        onSetArrivalTime = { _, _ -> },
        onSetDepartureTime = { _, _ -> },
        dayDate = LocalDate(2026, 5, 18),
    )
}
