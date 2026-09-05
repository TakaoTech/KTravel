package com.takaotech.ktravel.ui.plan.day.placestep.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.plan.day.placestep.PlaceStepTestTags
import com.takaotech.ktravel.ui.shared.time.ScheduleTimeEditorScope
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_cd_end_time
import ktravel.composeapp.generated.resources.planning_detail_cd_start_time
import ktravel.composeapp.generated.resources.planning_detail_end_time_label
import ktravel.composeapp.generated.resources.planning_detail_start_time_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Gap between the two schedule fields, both when they sit side by side and when they stack. */
internal val SCHEDULE_FIELD_SPACING = 16.dp

/** Horizontal content padding of a field, on both sides. */
internal val SCHEDULE_FIELD_HORIZONTAL_PADDING = 16.dp

/** Gap between the label and the clock value inside a field. */
internal val SCHEDULE_FIELD_INNER_SPACING = 8.dp

/**
 * The two schedule fields, side by side (each taking half of the row when [fillWidth] is set) or
 * [stacked] full width.
 */
@Composable
internal fun ScheduleFields(
    scope: ScheduleTimeEditorScope,
    stacked: Boolean,
    fillWidth: Boolean,
    stackContent: Boolean,
) {
    val startField = @Composable { fieldModifier: Modifier ->
        ScheduleField(
            modifier = fieldModifier.testTag(PlaceStepTestTags.START_TIME_FIELD),
            label = stringResource(Res.string.planning_detail_start_time_label),
            value = scope.startDisplay,
            stackContent = stackContent,
            onClick = scope.openStartPicker,
        )
    }
    val endField = @Composable { fieldModifier: Modifier ->
        ScheduleField(
            modifier = fieldModifier.testTag(PlaceStepTestTags.END_TIME_FIELD),
            label = stringResource(Res.string.planning_detail_end_time_label),
            value = scope.endDisplay,
            stackContent = stackContent,
            onClick = scope.openEndPicker,
        )
    }

    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            startField(Modifier.fillMaxWidth())
            endField(Modifier.fillMaxWidth())
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(SCHEDULE_FIELD_SPACING)) {
            // On wider windows the fields keep their natural width, as before.
            val fieldModifier = if (fillWidth) Modifier.weight(1f) else Modifier
            startField(fieldModifier)
            endField(fieldModifier)
        }
    }
}

/**
 * Single schedule field. The corner radius is fixed rather than the percentage shape Material3
 * gives a button by default: that one degenerates into an ellipse as soon as the content wraps and
 * the button grows taller.
 */
@Composable
internal fun ScheduleField(
    label: String,
    value: String,
    stackContent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelText = @Composable {
        Text(text = label, maxLines = 2, textAlign = TextAlign.Center)
    }
    // The clock value is atomic: it must never break into "09:" / "30".
    val valueText = @Composable {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            softWrap = false,
        )
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonDefaults.MinHeight),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(
            horizontal = SCHEDULE_FIELD_HORIZONTAL_PADDING,
            vertical = 8.dp,
        ),
    ) {
        if (stackContent) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                labelText()
                valueText()
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SCHEDULE_FIELD_INNER_SPACING),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                labelText()
                valueText()
            }
        }
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun ScheduleFieldsPreview(
    @PreviewParameter(ScheduleFieldsPreviewParams::class) state: ScheduleFieldsPreviewState,
) = KTravelTheme {
    Surface {
        Column(
            modifier = Modifier
                .width(state.width)
                .padding(12.dp),
        ) {
            ScheduleFields(
                scope = ScheduleTimeEditorScope(
                    startDisplay = "09:30",
                    endDisplay = "11:00",
                    startContentDescription = stringResource(Res.string.planning_detail_cd_start_time),
                    endContentDescription = stringResource(Res.string.planning_detail_cd_end_time),
                    openStartPicker = {},
                    openEndPicker = {},
                ),
                stacked = state.stacked,
                fillWidth = state.fillWidth,
                stackContent = state.stackContent,
            )
        }
    }
}

/** One step of the degradation: the room the two fields are given, and how they answer to it. */
internal data class ScheduleFieldsPreviewState(
    val width: Dp,
    val stacked: Boolean,
    val fillWidth: Boolean,
    val stackContent: Boolean,
)

/**
 * The four steps the two fields go through as the room per field shrinks.
 *
 * They are in the order [ScheduleSection] walks: the fields first take half of the row each, then
 * put the clock value under the label, and only when even that stops fitting do they give up the
 * shared row. Each step has to look deliberate next to the one before it, which is only visible
 * reading them in this order — the flags are passed in here, so the widths are what makes each one
 * plausible rather than what decides it.
 */
internal class ScheduleFieldsPreviewParams : PreviewParameterProvider<ScheduleFieldsPreviewState> {
    override val values = sequenceOf(
        ScheduleFieldsPreviewState(
            width = 480.dp,
            stacked = false,
            fillWidth = false,
            stackContent = false,
        ),
        ScheduleFieldsPreviewState(
            width = 400.dp,
            stacked = false,
            fillWidth = true,
            stackContent = false,
        ),
        ScheduleFieldsPreviewState(
            width = 280.dp,
            stacked = false,
            fillWidth = true,
            stackContent = true,
        ),
        ScheduleFieldsPreviewState(
            width = 180.dp,
            stacked = true,
            fillWidth = true,
            stackContent = true,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleFieldPreview(
    @PreviewParameter(ScheduleFieldPreviewParams::class) state: ScheduleFieldPreviewState,
) = KTravelTheme {
    Surface {
        ScheduleField(
            label = stringResource(state.label),
            value = state.value,
            stackContent = state.stackContent,
            onClick = {},
            modifier = Modifier
                .padding(12.dp)
                .widthIn(max = state.maxWidth),
        )
    }
}

/** One field to draw: its two texts, the arrangement they take, and the room they are given. */
internal data class ScheduleFieldPreviewState(
    val label: StringResource,
    val value: String,
    val stackContent: Boolean,
    val maxWidth: Dp = Dp.Unspecified,
)

/**
 * The field inline, stacked, without a time yet, and squeezed.
 *
 * The squeezed one is the reason the shape is a fixed radius rather than the Material3 percentage:
 * it is the state where the label wraps onto a second line and the button grows tall enough for a
 * percentage corner to round it into an ellipse. It also has to keep the clock value on one line.
 */
internal class ScheduleFieldPreviewParams : PreviewParameterProvider<ScheduleFieldPreviewState> {
    override val values = sequenceOf(
        ScheduleFieldPreviewState(
            label = Res.string.planning_detail_start_time_label,
            value = "09:30",
            stackContent = false,
        ),
        ScheduleFieldPreviewState(
            label = Res.string.planning_detail_end_time_label,
            value = "--:--",
            stackContent = false,
        ),
        ScheduleFieldPreviewState(
            label = Res.string.planning_detail_start_time_label,
            value = "09:30",
            stackContent = true,
        ),
        ScheduleFieldPreviewState(
            label = Res.string.planning_detail_start_time_label,
            value = "09:30",
            stackContent = true,
            maxWidth = 96.dp,
        ),
    )
}

//endregion Previews
