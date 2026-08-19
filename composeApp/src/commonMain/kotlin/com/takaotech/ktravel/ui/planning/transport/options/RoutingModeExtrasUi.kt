package com.takaotech.ktravel.ui.planning.transport.options

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.presentation.planning.transport.options.RoutingModeExtrasEvent
import com.takaotech.ktravel.presentation.planning.transport.options.RoutingModeExtrasScreen
import com.takaotech.ktravel.presentation.planning.transport.options.RoutingModeExtrasUiState
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportTestTags
import com.takaotech.ktravel.ui.planning.transport.SectionLabel
import com.takaotech.ktravel.ui.planning.transport.ToggleRow
import com.takaotech.ktravel.ui.planning.transport.label
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_transport_avoid_hint
import ktravel.composeapp.generated.resources.planning_transport_avoid_section
import ktravel.composeapp.generated.resources.planning_transport_avoid_tolls_unsupported
import ktravel.composeapp.generated.resources.planning_transport_shortest
import ktravel.composeapp.generated.resources.planning_transport_shortest_hint
import ktravel.composeapp.generated.resources.planning_transport_shortest_unsupported
import org.jetbrains.compose.resources.stringResource

/** The options that belong to the vehicle currently chosen, drawn inside the routing family's block. */
@CircuitInject(RoutingModeExtrasScreen::class, AppScope::class)
@Composable
fun RoutingModeExtrasUi(state: RoutingModeExtrasUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    RoutingModeExtrasContent(
        avoidable = state.avoidable,
        avoided = state.avoided,
        tollsUnsupported = state.tollsUnsupported,
        supportsShortest = state.supportsShortest,
        shortestDistance = state.shortestDistance,
        modifier = modifier,
        onAvoidClick = { sink(RoutingModeExtrasEvent.ToggleAvoid(it)) },
        onShortestChange = { sink(RoutingModeExtrasEvent.SetShortestDistance(it)) },
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutingModeExtrasContent(
    avoidable: ImmutableSet<RouteFeature>,
    avoided: ImmutableSet<RouteFeature>,
    tollsUnsupported: Boolean,
    supportsShortest: Boolean,
    shortestDistance: Boolean,
    onAvoidClick: (RouteFeature) -> Unit,
    onShortestChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        if (avoidable.isNotEmpty() || tollsUnsupported) {
            Column {
                SectionLabel(stringResource(Res.string.planning_transport_avoid_section))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    avoidable.forEach { feature ->
                        FilterChipFor(
                            feature = feature,
                            selected = feature in avoided,
                            onClick = { onAvoidClick(feature) },
                        )
                    }
                }

                // Said rather than left to be inferred: avoidance is a preference upstream, and a
                // route that still crosses what was excluded is the provider working as documented.
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(Res.string.planning_transport_avoid_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // A chip that is absent because the vehicle is never asked reads the same as one
                // absent because the profile cannot answer, unless the second one is spelled out.
                if (tollsUnsupported) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = stringResource(Res.string.planning_transport_avoid_tolls_unsupported),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        ToggleRow(
            modifier = Modifier.testTag(PlanningTransportTestTags.SHORTEST),
            title = stringResource(Res.string.planning_transport_shortest),
            subtitle = if (supportsShortest) {
                stringResource(Res.string.planning_transport_shortest_hint)
            } else {
                stringResource(Res.string.planning_transport_shortest_unsupported)
            },
            checked = shortestDistance,
            enabled = supportsShortest,
            onCheckedChange = onShortestChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipFor(feature: RouteFeature, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        modifier = Modifier.testTag(PlanningTransportTestTags.avoidTag(feature.name)),
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(feature.label())) },
    )
}

@Preview
@Composable
private fun RoutingModeExtrasContentPreview() = KTravelTheme {
    RoutingModeExtrasContent(
        avoidable = persistentSetOf(
            RouteFeature.TOLL_ROAD,
            RouteFeature.CONTROLLED_ACCESS_HIGHWAY,
            RouteFeature.FERRY,
            RouteFeature.TUNNEL,
            RouteFeature.DIRT_ROAD,
            RouteFeature.CAR_SHUTTLE_TRAIN,
        ),
        avoided = persistentSetOf(RouteFeature.FERRY),
        tollsUnsupported = false,
        supportsShortest = true,
        shortestDistance = false,
        onAvoidClick = {},
        onShortestChange = {},
    )
}

@Preview
@Composable
private fun RoutingModeExtrasContentScooterPreview() = KTravelTheme {
    RoutingModeExtrasContent(
        avoidable = persistentSetOf(RouteFeature.FERRY, RouteFeature.TUNNEL, RouteFeature.DIRT_ROAD),
        avoided = persistentSetOf(),
        tollsUnsupported = false,
        // A scooter is one of the vehicles HERE refuses to optimize for distance.
        supportsShortest = false,
        shortestDistance = false,
        onAvoidClick = {},
        onShortestChange = {},
    )
}
