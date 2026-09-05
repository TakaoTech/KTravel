package com.takaotech.ktravel.ui.plan.day.stepspane.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.plan.VisitScheduleUi
import com.takaotech.ktravel.ui.plan.day.stepspane.StepsPaneTestTags
import com.takaotech.ktravel.ui.shared.component.PillChip
import com.takaotech.ktravel.ui.shared.component.PillChipStyle
import com.takaotech.ktravel.ui.shared.time.ScheduleTimeEditor
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.north_east
import ktravel.composeapp.generated.resources.south_east
import org.jetbrains.compose.resources.DrawableResource

/** Arrival and departure of the visit, side by side, each opening its own time picker. */
@Composable
internal fun ScheduleChips(
    stepId: String,
    schedule: VisitScheduleUi?,
    onSetArrivalTime: (LocalTime) -> Unit,
    onSetDepartureTime: (LocalTime) -> Unit,
) {
    ScheduleTimeEditor(
        startTime = schedule?.startTime,
        endTime = schedule?.endTime,
        onStartConfirm = onSetArrivalTime,
        onEndConfirm = onSetDepartureTime,
    ) { scope ->
        // Wrapping keeps both chips readable when the actions squeeze the card on a narrow phone.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            TimeChip(
                modifier = Modifier.testTag(StepsPaneTestTags.arrivalChipTag(stepId)),
                display = scope.startDisplay,
                isSet = schedule?.startTime != null,
                icon = Res.drawable.south_east,
                contentDescription = scope.startContentDescription,
                onClick = scope.openStartPicker,
            )
            TimeChip(
                modifier = Modifier.testTag(StepsPaneTestTags.departureChipTag(stepId)),
                display = scope.endDisplay,
                isSet = schedule?.endTime != null,
                icon = Res.drawable.north_east,
                contentDescription = scope.endContentDescription,
                onClick = scope.openEndPicker,
            )
        }
    }
}

/**
 * Compact time pill: filled once the time is set, dashed outline while it still reads `--:--`, so
 * an unplanned stop is visible at a glance without another label.
 */
@Composable
internal fun TimeChip(
    display: String,
    isSet: Boolean,
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PillChip(
        modifier = modifier,
        text = display,
        icon = icon,
        onClick = onClick,
        style = if (isSet) PillChipStyle.Filled else PillChipStyle.Dashed,
        contentColor = if (isSet) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        contentDescription = contentDescription,
    )
}

//region Previews
@PreviewLightDark
@Composable
private fun ScheduleChipsPreview(@PreviewParameter(ScheduleChipsPreviewParams::class) schedule: VisitScheduleUi?) =
    KTravelTheme {
        Surface {
            Box(modifier = Modifier.padding(12.dp)) {
                ScheduleChips(
                    stepId = "preview-step",
                    schedule = schedule,
                    onSetArrivalTime = {},
                    onSetDepartureTime = {},
                )
            }
        }
    }
//endregion Previews
