package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.core.ui.preview.TravelDayStepPreviewParameterProvider
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.detail.StepRow
import com.takaotech.ktravel.presentation.planning.detail.StepsPaneEvent
import com.takaotech.ktravel.presentation.planning.detail.StepsPaneScreen
import com.takaotech.ktravel.presentation.planning.detail.StepsPaneUiState
import com.takaotech.ktravel.presentation.planning.detail.buildStepRows
import kotlinx.collections.immutable.ImmutableList
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.flight
import ktravel.composeapp.generated.resources.planning_detail_cd_back
import ktravel.composeapp.generated.resources.planning_detail_cd_open_backlog
import ktravel.composeapp.generated.resources.planning_detail_steps_empty
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal object StepsPaneTestTags {
    const val LIST = "steps_pane_list"
    const val EMPTY = "steps_pane_empty"
    const val BACK_BUTTON = "steps_pane_back"
    const val OPEN_BACKLOG_BUTTON = "steps_pane_open_backlog"
    fun addTransportTag(startPlaceId: String, endPlaceId: String) =
        "steps_pane_add_transport_${startPlaceId}_$endPlaceId"
}

/**
 * `Ui` Circuit del pannello itinerario (registrata via Metro `@CircuitInject`): renderizza lo stato
 * e inoltra gli eventi via `eventSink`, riusando il contenuto stateless [StepsPaneContent].
 */
@CircuitInject(StepsPaneScreen::class, AppScope::class)
@Composable
fun StepsPaneUi(state: StepsPaneUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    StepsPaneContent(
        rows = state.rows,
        modifier = modifier,
        onNavigationBackClick = { sink(StepsPaneEvent.NavigateBack) },
        onOpenBacklogClick = { sink(StepsPaneEvent.OpenBacklog) },
        onStepClick = { sink(StepsPaneEvent.OpenStepDetail(it)) },
        onDeleteStepClick = { sink(StepsPaneEvent.DeleteStep(it)) },
        onMoveStepUpClick = { sink(StepsPaneEvent.MoveStepUp(it)) },
        onMoveStepDownClick = { sink(StepsPaneEvent.MoveStepDown(it)) },
        onAddTransportClick = { startPlaceId, endPlaceId ->
            sink(StepsPaneEvent.AddTransport(startPlaceId, endPlaceId))
        }
    )
}

/**
 * Contenuto stateless del pannello itinerario: renderizza le [rows] pre-calcolate dal presenter,
 * senza alcuna logica di adiacenza.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StepsPaneContent(
    rows: ImmutableList<StepRow>,
    onNavigationBackClick: () -> Unit,
    onOpenBacklogClick: () -> Unit,
    onStepClick: (String) -> Unit,
    onDeleteStepClick: (StepUi) -> Unit,
    onMoveStepUpClick: (String) -> Unit,
    onMoveStepDownClick: (String) -> Unit,
    onAddTransportClick: (startPlaceId: String, endPlaceId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag(StepsPaneTestTags.BACK_BUTTON),
                        onClick = onNavigationBackClick
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.planning_detail_cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag(StepsPaneTestTags.OPEN_BACKLOG_BUTTON),
                        onClick = onOpenBacklogClick
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.flight),
                            contentDescription = stringResource(Res.string.planning_detail_cd_open_backlog)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (rows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.testTag(StepsPaneTestTags.EMPTY),
                    text = stringResource(Res.string.planning_detail_steps_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).testTag(StepsPaneTestTags.LIST)
            ) {
                items(items = rows, key = { it.key }) { row ->
                    when (row) {
                        is StepRow.Step -> when (val step = row.step) {
                            is StepUi.Place -> TravelStepPlace(
                                step = step,
                                onStepClick = onStepClick,
                                onStepDeleteClicked = { onDeleteStepClick(step) },
                                onStepMoveUp = onMoveStepUpClick,
                                onStepMoveDown = onMoveStepDownClick
                            )

                            is StepUi.Transport -> TravelStepTransport(
                                modifier = Modifier.fillMaxWidth(),
                                step = step,
                                onStepDeleteClicked = { onDeleteStepClick(step) }
                            )
                        }

                        is StepRow.AddTransportSlot -> TravelTransportStepAdd(
                            modifier = Modifier.testTag(
                                StepsPaneTestTags.addTransportTag(row.startPlaceId, row.endPlaceId)
                            ),
                            onClick = { onAddTransportClick(row.startPlaceId, row.endPlaceId) }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun StepsPaneContentPreview() {
    StepsPaneContent(
        rows = buildStepRows(TravelDayStepPreviewParameterProvider(8).values.toList()),
        onNavigationBackClick = {},
        onOpenBacklogClick = {},
        onStepClick = {},
        onDeleteStepClick = {},
        onMoveStepUpClick = {},
        onMoveStepDownClick = {},
        onAddTransportClick = { _, _ -> }
    )
}
