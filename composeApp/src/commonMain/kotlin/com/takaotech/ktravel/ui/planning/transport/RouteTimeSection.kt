package com.takaotech.ktravel.ui.planning.transport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.RouteTimeChoice
import com.takaotech.ktravel.presentation.planning.transport.RouteTimeMode
import com.takaotech.ktravel.presentation.planning.transport.mode
import com.takaotech.ktravel.presentation.planning.transport.timeOrNull
import com.takaotech.ktravel.ui.common.formatClock
import com.takaotech.ktravel.ui.common.formatDayMonthYear
import com.takaotech.ktravel.ui.planning.detail.ScheduleTimePickerDialog
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.info
import ktravel.composeapp.generated.resources.planning_transport_cd_time
import ktravel.composeapp.generated.resources.planning_transport_cd_time_now_info
import ktravel.composeapp.generated.resources.planning_transport_time_arrive_by
import ktravel.composeapp.generated.resources.planning_transport_time_arrive_by_unsupported
import ktravel.composeapp.generated.resources.planning_transport_time_depart_at
import ktravel.composeapp.generated.resources.planning_transport_time_now
import ktravel.composeapp.generated.resources.planning_transport_time_now_explanation
import ktravel.composeapp.generated.resources.planning_transport_time_on_day
import ktravel.composeapp.generated.resources.planning_transport_time_section
import ktravel.composeapp.generated.resources.schedule
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * When the leg should happen: now, leaving at an hour, or arriving by one.
 *
 * A single-choice selector because the three are exclusive all the way down — the contract carries
 * them as a sealed `RouteTime` and the provider takes either a departure or an arrival, never both.
 *
 * Only the hour is picked here. The date is the day the leg belongs to and is shown rather than
 * chosen: a leg between two stops of the same day has nowhere else to be.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RouteTimeSection(
    choice: RouteTimeChoice,
    dayDate: LocalDate?,
    supportsArriveBy: Boolean,
    onModeChange: (RouteTimeMode) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val selectedMode = choice.mode
    val time = choice.timeOrNull

    Column(modifier = modifier) {
        SectionLabel(stringResource(Res.string.planning_transport_time_section))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            RouteTimeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    modifier = Modifier.testTag(mode.testTag()),
                    selected = mode == selectedMode,
                    onClick = { onModeChange(mode) },
                    // Offered only where the profile can honour it: a navigator refuses an arrival it
                    // cannot plan backwards from, and that refusal is nothing the traveller can act on.
                    enabled = mode != RouteTimeMode.ARRIVE_BY || supportsArriveBy,
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = RouteTimeMode.entries.size,
                    ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(mode.label()))
                        if (mode == RouteTimeMode.NOW) {
                            NowInfoTooltip(modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }

        if (time != null) {
            SelectedTime(
                modifier = Modifier.padding(top = 10.dp),
                label = stringResource(selectedMode.label()),
                time = time,
                dayDate = dayDate,
                onClick = { showPicker = true },
            )
        }

        if (!supportsArriveBy) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = stringResource(Res.string.planning_transport_time_arrive_by_unsupported),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showPicker && time != null) {
        ScheduleTimePickerDialog(
            initialTime = time,
            title = stringResource(selectedMode.label()),
            isValid = { true },
            onConfirm = {
                onTimeChange(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

/**
 * The caveat behind "now", reachable by hovering the icon with a mouse or long pressing it.
 *
 * It is a tooltip rather than a line under the row because it answers a question only whoever stops
 * to ask what "now" is measured from actually has, and the row already carries one caption.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowInfoTooltip(modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.planning_transport_cd_time_now_info)

    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        state = rememberTooltipState(isPersistent = true),
        tooltip = {
            PlainTooltip(modifier = Modifier.testTag(PlanningTransportTestTags.TIME_NOW_TOOLTIP)) {
                Text(stringResource(Res.string.planning_transport_time_now_explanation))
            }
        },
    ) {
        Icon(
            modifier = Modifier
                .testTag(PlanningTransportTestTags.TIME_NOW_INFO)
                .size(16.dp),
            painter = painterResource(Res.drawable.info),
            contentDescription = description,
        )
    }
}

/** The hour that has been chosen, and the day it falls on. */
@Composable
private fun SelectedTime(
    label: String,
    time: LocalTime,
    dayDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.planning_transport_cd_time)

    TransportCard(
        modifier = modifier
            .testTag(PlanningTransportTestTags.TIME_VALUE)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    modifier = Modifier,
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                )

                if (dayDate != null) {
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = stringResource(
                            Res.string.planning_transport_time_on_day,
                            dayDate.formatDayMonthYear(),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.large)
                    .border(
                        1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.large,
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = time.formatClock(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                )
                Icon(
                    painter = painterResource(Res.drawable.schedule),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null,
                )
            }
        }
    }
}

private fun RouteTimeMode.label(): StringResource = when (this) {
    RouteTimeMode.NOW -> Res.string.planning_transport_time_now
    RouteTimeMode.DEPART_AT -> Res.string.planning_transport_time_depart_at
    RouteTimeMode.ARRIVE_BY -> Res.string.planning_transport_time_arrive_by
}

private fun RouteTimeMode.testTag(): String = when (this) {
    RouteTimeMode.NOW -> PlanningTransportTestTags.TIME_NOW
    RouteTimeMode.DEPART_AT -> PlanningTransportTestTags.TIME_DEPART_AT
    RouteTimeMode.ARRIVE_BY -> PlanningTransportTestTags.TIME_ARRIVE_BY
}

@Preview
@Composable
private fun RouteTimeSectionPreview() = KTravelTheme {
    Surface {
        RouteTimeSection(
            choice = RouteTimeChoice.DepartAt(LocalTime(hour = 10, minute = 30)),
            dayDate = LocalDate(year = 2026, month = Month.MAY, day = 18),
            supportsArriveBy = true,
            onModeChange = {},
            onTimeChange = {},
        )
    }
}
