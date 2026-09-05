package com.takaotech.ktravel.ui.plan.day.stepspane

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.takaotech.ktravel.presentation.plan.StepUi
import com.takaotech.ktravel.presentation.plan.day.StepRow
import com.takaotech.ktravel.presentation.plan.day.buildStepRows
import com.takaotech.ktravel.ui.plan.day.stepspane.component.AddTransportNode
import com.takaotech.ktravel.ui.plan.day.stepspane.component.BacklogToggle
import com.takaotech.ktravel.ui.plan.day.stepspane.component.DestinationNode
import com.takaotech.ktravel.ui.plan.day.stepspane.component.PlaceNode
import com.takaotech.ktravel.ui.plan.day.stepspane.component.TimelineRow
import com.takaotech.ktravel.ui.plan.day.stepspane.component.TransportNode
import com.takaotech.ktravel.ui.plan.day.stepspane.component.TravelStepPlace
import com.takaotech.ktravel.ui.plan.day.stepspane.component.TravelStepTransport
import com.takaotech.ktravel.ui.plan.day.stepspane.component.TravelTransportStepAdd
import com.takaotech.ktravel.ui.plan.preview.TravelDayStepPreviewParameterProvider
import com.takaotech.ktravel.ui.shared.format.formatWeekdayDayMonthYear
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.planning_detail_cd_back
import ktravel.composeapp.generated.resources.planning_detail_steps_empty
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    modifier: Modifier = Modifier,
    dayDate: LocalDate? = null,
    backlogOpen: Boolean = false,
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
                    BacklogToggle(open = backlogOpen, onClick = onOpenBacklogClick)
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
                                    node = {
                                        if (isDestination) DestinationNode() else PlaceNode()
                                    },
                                ) {
                                    TravelStepPlace(
                                        step = step,
                                        isDestination = isDestination,
                                        onStepClick = onStepClick,
                                        onStepDeleteClicked = { onDeleteStepClick(step) },
                                        onStepMoveUp = onMoveStepUpClick,
                                        onStepMoveDown = onMoveStepDownClick,
                                        onSetArrivalTime = { onSetArrivalTime(step.id, it) },
                                        onSetDepartureTime = { onSetDepartureTime(step.id, it) },
                                    )
                                }
                            }

                            is StepUi.Transport -> TimelineRow(
                                isFirst = isFirst,
                                isLast = isLast,
                                node = { TransportNode(step) },
                            ) {
                                TravelStepTransport(
                                    modifier = Modifier,
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

//region Previews
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
//endregion Previews
