package com.takaotech.ktravel.ui.plan.day.placestep.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import com.takaotech.ktravel.ui.shared.time.ScheduleTimeEditorScope
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_end_time_label
import ktravel.composeapp.generated.resources.planning_detail_start_time_label
import org.jetbrains.compose.resources.stringResource

/**
 * Widths a schedule field needs: [inline] renders the label and the clock value on one line,
 * [wrapped] puts the value underneath the label.
 */
@Immutable
internal data class ScheduleFieldWidths(val inline: Dp, val wrapped: Dp)

/**
 * Widths the widest of the two fields needs, measured rather than guessed: they depend on the
 * translation in use and on the current font scale.
 */
@Composable
internal fun measureScheduleFieldWidths(scope: ScheduleTimeEditorScope): ScheduleFieldWidths {
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelLarge
    val valueStyle = MaterialTheme.typography.titleMedium
    val startLabel = stringResource(Res.string.planning_detail_start_time_label)
    val endLabel = stringResource(Res.string.planning_detail_end_time_label)
    val density = LocalDensity.current

    return remember(
        measurer,
        labelStyle,
        valueStyle,
        startLabel,
        endLabel,
        scope.startDisplay,
        scope.endDisplay,
        density,
    ) {
        fun labelWidth(text: String): Int = measurer.measure(text, labelStyle).size.width
        fun valueWidth(text: String): Int = measurer.measure(text, valueStyle).size.width

        val inline = maxOf(
            labelWidth(startLabel) + valueWidth(scope.startDisplay),
            labelWidth(endLabel) + valueWidth(scope.endDisplay),
        )
        val wrapped = maxOf(
            labelWidth(startLabel),
            labelWidth(endLabel),
            valueWidth(scope.startDisplay),
            valueWidth(scope.endDisplay),
        )

        with(density) {
            ScheduleFieldWidths(
                inline = inline.toDp() +
                    SCHEDULE_FIELD_INNER_SPACING +
                    SCHEDULE_FIELD_HORIZONTAL_PADDING * 2,
                wrapped = wrapped.toDp() + SCHEDULE_FIELD_HORIZONTAL_PADDING * 2,
            )
        }
    }
}
