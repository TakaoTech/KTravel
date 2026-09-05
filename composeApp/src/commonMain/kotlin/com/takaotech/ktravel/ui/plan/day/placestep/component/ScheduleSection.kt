package com.takaotech.ktravel.ui.plan.day.placestep.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.takaotech.ktravel.presentation.plan.VisitScheduleUi
import com.takaotech.ktravel.ui.shared.time.ScheduleTimeEditor
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_schedule_title
import org.jetbrains.compose.resources.stringResource

/**
 * Section that lets the user set the place arrival/departure times. Reuses the shared
 * [ScheduleTimeEditor] (Material3 time pickers + `departure >= arrival` validation); the unset
 * value shows the `--:--` placeholder.
 *
 * Layout degrades in two steps as the width per field shrinks — which happens both with a larger
 * font size and with a larger display size: the two fields first put their clock value under their
 * label, and only then stop sharing the row and stack full width. Without it a squeezed field
 * breaks its label and its clock value character by character.
 */
@Composable
internal fun ScheduleSection(
    schedule: VisitScheduleUi?,
    onSetStartTime: (LocalTime) -> Unit,
    onSetEndTime: (LocalTime) -> Unit,
) {
    ScheduleTimeEditor(
        startTime = schedule?.startTime,
        endTime = schedule?.endTime,
        onStartConfirm = onSetStartTime,
        onEndConfirm = onSetEndTime,
    ) { scope ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
            val widths = measureScheduleFieldWidths(scope)

            Text(
                text = stringResource(Res.string.planning_detail_schedule_title),
                style = MaterialTheme.typography.titleMedium,
            )

            BoxWithConstraints {
                val widthPerField = (maxWidth - SCHEDULE_FIELD_SPACING) / 2

                val stacked = widthPerField < widths.wrapped

                ScheduleFields(
                    scope = scope,
                    stacked = stacked,
                    fillWidth = !windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
                        WIDTH_DP_MEDIUM_LOWER_BOUND,
                    ),
                    // Both fields wrap together, so that they keep the same height.
                    stackContent = if (stacked) {
                        maxWidth < widths.inline
                    } else {
                        widthPerField < widths.inline
                    },
                )
            }
        }
    }
}

//region Previews

/**
 * The section at a fixed width, where [PreviewFontScale] is what shrinks the room per field.
 *
 * Both degradation steps stay the section's own decision rather than the preview's: the width is
 * held at a phone content width and only the font grows, which is the one axis a static preview
 * can walk on its own.
 */
@PreviewFontScale
@Composable
private fun ScheduleSectionPreview(@PreviewParameter(ScheduleSectionPreviewParams::class) schedule: VisitScheduleUi) =
    KTravelTheme {
        Surface {
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .padding(12.dp),
            ) {
                ScheduleSection(schedule = schedule, onSetStartTime = {}, onSetEndTime = {})
            }
        }
    }

/**
 * A schedule with nothing set, with the arrival alone, and with both times.
 *
 * A null schedule is not among them because the section reads it as an empty [VisitScheduleUi]
 * anyway. The half set one earns the place instead: the widths are measured over the widest of the
 * two fields, so a placeholder next to a real time is where the two of them disagree.
 */
internal class ScheduleSectionPreviewParams : PreviewParameterProvider<VisitScheduleUi> {
    override val values = sequenceOf(
        VisitScheduleUi(),
        VisitScheduleUi(startTime = LocalTime(9, 30)),
        VisitScheduleUi(startTime = LocalTime(9, 30), endTime = LocalTime(11, 0)),
    )
}
//endregion Previews
