package com.takaotech.ktravel.ui.planning.transport.options

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.presentation.planning.transport.options.RoadRouteOptionsEvent
import com.takaotech.ktravel.presentation.planning.transport.options.RoadRouteOptionsScreen
import com.takaotech.ktravel.presentation.planning.transport.options.RoadRouteOptionsUiState
import com.takaotech.ktravel.ui.planning.transport.ModeChip
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportTestTags
import com.takaotech.ktravel.ui.planning.transport.SectionLabel
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_transport_alternatives
import ktravel.composeapp.generated.resources.planning_transport_mode_section
import ktravel.composeapp.generated.resources.planning_transport_options_section
import org.jetbrains.compose.resources.stringResource

/**
 * The options of a road profile.
 *
 * Draws what does not depend on the vehicle and lets the vehicle draw the rest: [RoadModeExtrasUi]
 * arrives through a nested `CircuitContent`, so a vehicle that grows its own controls reaches this
 * screen without this file changing.
 */
@CircuitInject(RoadRouteOptionsScreen::class, AppScope::class)
@Composable
fun RoadRouteOptionsUi(state: RoadRouteOptionsUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    RoadRouteOptionsContent(
        modes = state.modes,
        selectedMode = state.selectedMode,
        alternatives = state.alternatives,
        maxAlternatives = state.maxAlternatives,
        modeExtrasScreen = state.modeExtrasScreen,
        modifier = modifier,
        onModeClick = { sink(RoadRouteOptionsEvent.SelectMode(it)) },
        onAlternativesChange = { sink(RoadRouteOptionsEvent.SetAlternatives(it)) },
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoadRouteOptionsContent(
    modes: ImmutableList<RoutingMode>,
    selectedMode: RoutingMode,
    alternatives: Int,
    maxAlternatives: Int,
    modeExtrasScreen: Screen?,
    onModeClick: (RoutingMode) -> Unit,
    onAlternativesChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Column {
            SectionLabel(stringResource(Res.string.planning_transport_mode_section))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                modes.forEach { mode ->
                    ModeChip(
                        mode = mode,
                        selected = mode == selectedMode,
                        enabled = true,
                        onClick = { onModeClick(mode) },
                    )
                }
            }
        }

        // Immediately after the vehicle, because everything in it follows from the vehicle.
        modeExtrasScreen?.let { CircuitContent(screen = it, onNavEvent = {}) }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel(stringResource(Res.string.planning_transport_options_section))

            Text(
                text = stringResource(Res.string.planning_transport_alternatives, alternatives),
                style = MaterialTheme.typography.bodyMedium,
            )

            Slider(
                modifier = Modifier.testTag(PlanningTransportTestTags.ALTERNATIVES),
                value = alternatives.toFloat(),
                onValueChange = { onAlternativesChange(it.toInt()) },
                valueRange = 1f..maxAlternatives.toFloat(),
                steps = (maxAlternatives - 2).coerceAtLeast(0),
            )
        }
    }
}

@Preview
@Composable
private fun RoadRouteOptionsContentPreview() = KTravelTheme {
    RoadRouteOptionsContent(
        modes = persistentListOf(RoutingMode("CAR"), RoutingMode("TRUCK"), RoutingMode("PEDESTRIAN")),
        selectedMode = RoutingMode("CAR"),
        alternatives = 2,
        maxAlternatives = 6,
        // Null so the preview needs no Circuit in composition; the extras have their own preview.
        modeExtrasScreen = null,
        onModeClick = {},
        onAlternativesChange = {},
    )
}
