package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.takaotech.ktravel.ui.common.formatClock
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_cd_end_time
import ktravel.composeapp.generated.resources.planning_detail_cd_start_time
import ktravel.composeapp.generated.resources.planning_detail_departure_before_arrival
import ktravel.composeapp.generated.resources.planning_detail_end_picker_title
import ktravel.composeapp.generated.resources.planning_detail_start_picker_title
import ktravel.composeapp.generated.resources.planning_detail_time_unset
import ktravel.composeapp.generated.resources.time_picker_cancel
import ktravel.composeapp.generated.resources.time_picker_confirm
import org.jetbrains.compose.resources.stringResource

/**
 * Presentation-agnostic handle exposed by [ScheduleTimeEditor] to its trigger [content]: the
 * display strings (formatted time or `--:--` placeholder), the accessibility descriptions, and the
 * actions that open each Material3 time picker. Callers decide how the two triggers look.
 */
@Stable
internal class ScheduleTimeEditorScope(
    val startDisplay: String,
    val endDisplay: String,
    val startContentDescription: String,
    val endContentDescription: String,
    val openStartPicker: () -> Unit,
    val openEndPicker: () -> Unit,
)

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

/**
 * Material3 time-picker dialog wrapper. Confirm is disabled and a warning is shown while the
 * currently selected time fails [isValid] (e.g. departure earlier than arrival).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleTimePickerDialog(
    initialTime: LocalTime?,
    title: String,
    isValid: (LocalTime) -> Boolean,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime?.hour ?: 0,
        initialMinute = initialTime?.minute ?: 0,
    )
    val selected = LocalTime(state.hour, state.minute)
    val valid = isValid(selected)

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onConfirm(LocalTime(state.hour, state.minute)) },
            ) {
                Text(stringResource(Res.string.time_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.time_picker_cancel))
            }
        },
        content = {
            TimePicker(state = state)
            // TODO Add TimeZone Selection
            if (!valid) {
                Text(
                    text = stringResource(Res.string.planning_detail_departure_before_arrival),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}
