package com.takaotech.ktravel.ui.plan.transport.options

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.presentation.plan.transport.options.MAX_CHANGES_LIMIT
import com.takaotech.ktravel.presentation.plan.transport.options.TransitRouteOptionsEvent
import com.takaotech.ktravel.presentation.plan.transport.options.TransitRouteOptionsScreen
import com.takaotech.ktravel.presentation.plan.transport.options.TransitRouteOptionsUiState
import com.takaotech.ktravel.presentation.plan.transport.options.WALK_DISTANCE_DEFAULT
import com.takaotech.ktravel.presentation.plan.transport.options.WALK_DISTANCE_MAX
import com.takaotech.ktravel.presentation.plan.transport.options.WALK_DISTANCE_MIN
import com.takaotech.ktravel.presentation.plan.transport.options.WALK_DISTANCE_STEP
import com.takaotech.ktravel.presentation.plan.transport.options.WalkingPace
import com.takaotech.ktravel.ui.plan.transport.component.RoutingModeChip
import com.takaotech.ktravel.ui.plan.transport.component.SectionLabel
import com.takaotech.ktravel.ui.plan.transport.component.ToggleRow
import com.takaotech.ktravel.ui.plan.transport.component.TransportPlanningTestTags
import com.takaotech.ktravel.ui.shared.format.label
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_transport_alternatives
import ktravel.composeapp.generated.resources.planning_transport_changes_count
import ktravel.composeapp.generated.resources.planning_transport_changes_limit
import ktravel.composeapp.generated.resources.planning_transport_changes_limit_hint
import ktravel.composeapp.generated.resources.planning_transport_mode_filter_hint
import ktravel.composeapp.generated.resources.planning_transport_mode_filter_section
import ktravel.composeapp.generated.resources.planning_transport_options_section
import ktravel.composeapp.generated.resources.planning_transport_walk_distance
import ktravel.composeapp.generated.resources.planning_transport_walk_distance_limit
import ktravel.composeapp.generated.resources.planning_transport_walk_distance_limit_hint
import ktravel.composeapp.generated.resources.planning_transport_walking_pace_section
import org.jetbrains.compose.resources.stringResource

/**
 * The options of a transit profile.
 *
 * No nested block for a chosen vehicle, because there is none: the modes are a filter over what the
 * answer may use, and transfers and walking apply to the journey as a whole.
 */
@CircuitInject(TransitRouteOptionsScreen::class, AppScope::class)
@Composable
fun TransitRouteOptionsUi(state: TransitRouteOptionsUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    TransitRouteOptionsContent(
        modes = state.modes,
        modeFilter = state.modeFilter,
        alternatives = state.alternatives,
        maxAlternatives = state.maxAlternatives,
        maxChanges = state.maxChanges,
        walkingPace = state.walkingPace,
        maxWalkingDistanceMeters = state.maxWalkingDistanceMeters,
        modifier = modifier,
        onModeClick = { sink(TransitRouteOptionsEvent.ToggleMode(it)) },
        onAlternativesChange = { sink(TransitRouteOptionsEvent.SetAlternatives(it)) },
        onMaxChangesChange = { sink(TransitRouteOptionsEvent.SetMaxChanges(it)) },
        onWalkingPaceChange = { sink(TransitRouteOptionsEvent.SetWalkingPace(it)) },
        onMaxWalkingDistanceChange = { sink(TransitRouteOptionsEvent.SetMaxWalkingDistance(it)) },
    )
}

@Suppress("LongParameterList", "LongMethod")
@Composable
internal fun TransitRouteOptionsContent(
    modes: ImmutableList<RoutingMode>,
    modeFilter: ImmutableSet<RoutingMode>,
    alternatives: Int,
    maxAlternatives: Int,
    maxChanges: Int?,
    walkingPace: WalkingPace,
    maxWalkingDistanceMeters: Int?,
    onModeClick: (RoutingMode) -> Unit,
    onAlternativesChange: (Int) -> Unit,
    onMaxChangesChange: (Int?) -> Unit,
    onWalkingPaceChange: (WalkingPace) -> Unit,
    onMaxWalkingDistanceChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        ModeFilter(modes = modes, modeFilter = modeFilter, onModeClick = onModeClick)

        WalkingPaceSelector(pace = walkingPace, onPaceChange = onWalkingPaceChange)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel(stringResource(Res.string.planning_transport_options_section))

            Text(
                text = stringResource(Res.string.planning_transport_alternatives, alternatives),
                style = MaterialTheme.typography.bodyMedium,
            )

            Slider(
                modifier = Modifier.testTag(TransportPlanningTestTags.ALTERNATIVES),
                value = alternatives.toFloat(),
                onValueChange = { onAlternativesChange(it.toInt()) },
                valueRange = 1f..maxAlternatives.toFloat(),
                steps = (maxAlternatives - 2).coerceAtLeast(0),
            )

            // A switch and then a slider, rather than a slider whose lowest position means "no
            // limit": zero transfers is a real thing to ask for, and it must not collide with not
            // having asked at all.
            LimitedNumber(
                testTag = TransportPlanningTestTags.CHANGES_LIMIT,
                sliderTestTag = TransportPlanningTestTags.CHANGES_SLIDER,
                title = stringResource(Res.string.planning_transport_changes_limit),
                subtitle = stringResource(Res.string.planning_transport_changes_limit_hint),
                value = maxChanges,
                valueLabel = { stringResource(Res.string.planning_transport_changes_count, it) },
                range = 0..MAX_CHANGES_LIMIT,
                step = 1,
                default = 1,
                onValueChange = onMaxChangesChange,
            )

            LimitedNumber(
                testTag = TransportPlanningTestTags.WALK_DISTANCE_LIMIT,
                sliderTestTag = TransportPlanningTestTags.WALK_DISTANCE_SLIDER,
                title = stringResource(Res.string.planning_transport_walk_distance_limit),
                subtitle = stringResource(Res.string.planning_transport_walk_distance_limit_hint),
                value = maxWalkingDistanceMeters,
                valueLabel = { stringResource(Res.string.planning_transport_walk_distance, it) },
                range = WALK_DISTANCE_MIN..WALK_DISTANCE_MAX,
                step = WALK_DISTANCE_STEP,
                default = WALK_DISTANCE_DEFAULT,
                onValueChange = onMaxWalkingDistanceChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModeFilter(
    modes: ImmutableList<RoutingMode>,
    modeFilter: ImmutableSet<RoutingMode>,
    onModeClick: (RoutingMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(stringResource(Res.string.planning_transport_mode_filter_section))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            modes.forEach { mode ->
                RoutingModeChip(
                    mode = mode,
                    selected = mode in modeFilter,
                    enabled = true,
                    onClick = { onModeClick(mode) },
                )
            }
        }

        // An empty filter is no restriction, which is not the same as nothing being allowed.
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(Res.string.planning_transport_mode_filter_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * How fast the traveller walks, as three named paces.
 *
 * The API takes metres per second, which nobody thinks in, and the parameter's only effect is on
 * which connections are catchable. Three words say that; a decimal slider would not.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WalkingPaceSelector(
    pace: WalkingPace,
    onPaceChange: (WalkingPace) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(stringResource(Res.string.planning_transport_walking_pace_section))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            WalkingPace.entries.forEachIndexed { index, entry ->
                SegmentedButton(
                    modifier = Modifier.testTag(TransportPlanningTestTags.walkingPaceTag(entry.name)),
                    selected = entry == pace,
                    onClick = { onPaceChange(entry) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = WalkingPace.entries.size,
                    ),
                ) {
                    Text(stringResource(entry.label()))
                }
            }
        }
    }
}

/** A number the traveller may or may not want to bound at all: a switch, and a slider under it. */
@Suppress("LongParameterList")
@Composable
internal fun LimitedNumber(
    testTag: String,
    sliderTestTag: String,
    title: String,
    subtitle: String,
    value: Int?,
    valueLabel: @Composable (Int) -> String,
    range: IntRange,
    step: Int,
    default: Int,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ToggleRow(
            modifier = Modifier.testTag(testTag),
            title = title,
            subtitle = subtitle,
            checked = value != null,
            enabled = true,
            onCheckedChange = { limited -> onValueChange(if (limited) default else null) },
        )

        if (value != null) {
            Text(text = valueLabel(value), style = MaterialTheme.typography.bodyMedium)

            Slider(
                modifier = Modifier.testTag(sliderTestTag),
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                steps = ((range.last - range.first) / step - 1).coerceAtLeast(0),
            )
        }
    }
}

//region Previews
@Preview
@Composable
private fun TransitRouteOptionsContentPreview() = KTravelTheme {
    TransitRouteOptionsContent(
        modes = persistentListOf(
            RoutingMode("SUBWAY"),
            RoutingMode("BUS"),
            RoutingMode("REGIONAL_TRAIN"),
            RoutingMode("FERRY"),
        ),
        modeFilter = persistentSetOf(RoutingMode("SUBWAY")),
        alternatives = 3,
        maxAlternatives = 5,
        maxChanges = 2,
        walkingPace = WalkingPace.NORMAL,
        maxWalkingDistanceMeters = null,
        onModeClick = {},
        onAlternativesChange = {},
        onMaxChangesChange = {},
        onWalkingPaceChange = {},
        onMaxWalkingDistanceChange = {},
    )
}
//endregion Previews
