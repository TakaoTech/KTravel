package com.takaotech.ktravel.ui.shared.time

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.takaotech.ktravel.ui.shared.format.formatClock
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_cd_end_time
import ktravel.composeapp.generated.resources.planning_detail_cd_start_time
import ktravel.composeapp.generated.resources.planning_detail_end_picker_title
import ktravel.composeapp.generated.resources.planning_detail_start_picker_title
import ktravel.composeapp.generated.resources.planning_detail_time_unset
import org.jetbrains.compose.resources.stringResource

/**
 * Centralized editor of a place visit schedule (start/end). Owns the two Material3
 * [TimePickerDialog]s, the `start >= ebd` validation, the open/close state and the time
 * formatting; the caller only supplies the trigger layout via [content], keeping the logic single
 * sourced between the timeline column and the step detail section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleTimeEditor(
    startTime: LocalTime?,
    endTime: LocalTime?,
    onStartConfirm: (LocalTime) -> Unit,
    onEndConfirm: (LocalTime) -> Unit,
    content: @Composable (ScheduleTimeEditorScope) -> Unit,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val placeholder = stringResource(Res.string.planning_detail_time_unset)
    val scope = ScheduleTimeEditorScope(
        startDisplay = startTime?.formatClock() ?: placeholder,
        endDisplay = endTime?.formatClock() ?: placeholder,
        startContentDescription = stringResource(Res.string.planning_detail_cd_start_time),
        endContentDescription = stringResource(Res.string.planning_detail_cd_end_time),
        openStartPicker = { showStartPicker = true },
        openEndPicker = { showEndPicker = true },
    )

    content(scope)

    if (showStartPicker) {
        ScheduleTimePickerDialog(
            initialTime = startTime,
            title = stringResource(Res.string.planning_detail_start_picker_title),
            // Arrival cannot be later than an already set departure.
            isValid = { selected -> endTime == null || selected <= endTime },
            onConfirm = {
                onStartConfirm(it)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false },
        )
    }

    if (showEndPicker) {
        ScheduleTimePickerDialog(
            initialTime = endTime,
            title = stringResource(Res.string.planning_detail_end_picker_title),
            // Departure cannot be earlier than an already set arrival.
            isValid = { selected -> startTime == null || selected >= startTime },
            onConfirm = {
                onEndConfirm(it)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false },
        )
    }
}
