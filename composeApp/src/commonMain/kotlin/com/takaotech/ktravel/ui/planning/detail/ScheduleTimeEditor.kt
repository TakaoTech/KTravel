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
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_arrival_picker_title
import ktravel.composeapp.generated.resources.planning_detail_cd_arrival_time
import ktravel.composeapp.generated.resources.planning_detail_cd_departure_time
import ktravel.composeapp.generated.resources.planning_detail_departure_before_arrival
import ktravel.composeapp.generated.resources.planning_detail_departure_picker_title
import ktravel.composeapp.generated.resources.planning_detail_time_unset
import ktravel.composeapp.generated.resources.time_picker_cancel
import ktravel.composeapp.generated.resources.time_picker_confirm
import org.jetbrains.compose.resources.stringResource

/** Formats a [LocalTime] as `HH:mm`. */
internal fun formatScheduleTime(time: LocalTime): String =
    "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

/**
 * Presentation-agnostic handle exposed by [ScheduleTimeEditor] to its trigger [content]: the
 * display strings (formatted time or `--:--` placeholder), the accessibility descriptions, and the
 * actions that open each Material3 time picker. Callers decide how the two triggers look.
 */
@Stable
internal class ScheduleTimeEditorScope(
    val arrivalDisplay: String,
    val departureDisplay: String,
    val arrivalContentDescription: String,
    val departureContentDescription: String,
    val openArrivalPicker: () -> Unit,
    val openDeparturePicker: () -> Unit,
)

/**
 * Centralized editor of a place visit schedule (arrival/departure). Owns the two Material3
 * [TimePickerDialog]s, the `departure >= arrival` validation, the open/close state and the time
 * formatting; the caller only supplies the trigger layout via [content], keeping the logic single
 * sourced between the timeline column and the step detail section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleTimeEditor(
    arrivalTime: LocalTime?,
    departureTime: LocalTime?,
    onArrivalConfirm: (LocalTime) -> Unit,
    onDepartureConfirm: (LocalTime) -> Unit,
    content: @Composable (ScheduleTimeEditorScope) -> Unit,
) {
    var showArrivalPicker by remember { mutableStateOf(false) }
    var showDeparturePicker by remember { mutableStateOf(false) }

    val placeholder = stringResource(Res.string.planning_detail_time_unset)
    val scope = ScheduleTimeEditorScope(
        arrivalDisplay = arrivalTime?.let(::formatScheduleTime) ?: placeholder,
        departureDisplay = departureTime?.let(::formatScheduleTime) ?: placeholder,
        arrivalContentDescription = stringResource(Res.string.planning_detail_cd_arrival_time),
        departureContentDescription = stringResource(Res.string.planning_detail_cd_departure_time),
        openArrivalPicker = { showArrivalPicker = true },
        openDeparturePicker = { showDeparturePicker = true },
    )

    content(scope)

    if (showArrivalPicker) {
        ScheduleTimePickerDialog(
            initialTime = arrivalTime,
            title = stringResource(Res.string.planning_detail_arrival_picker_title),
            // Arrival cannot be later than an already set departure.
            isValid = { selected -> departureTime == null || selected <= departureTime },
            onConfirm = {
                onArrivalConfirm(it)
                showArrivalPicker = false
            },
            onDismiss = { showArrivalPicker = false }
        )
    }

    if (showDeparturePicker) {
        ScheduleTimePickerDialog(
            initialTime = departureTime,
            title = stringResource(Res.string.planning_detail_departure_picker_title),
            // Departure cannot be earlier than an already set arrival.
            isValid = { selected -> arrivalTime == null || selected >= arrivalTime },
            onConfirm = {
                onDepartureConfirm(it)
                showDeparturePicker = false
            },
            onDismiss = { showDeparturePicker = false }
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
        initialMinute = initialTime?.minute ?: 0
    )
    val selected = LocalTime(state.hour, state.minute)
    val valid = isValid(selected)

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onConfirm(LocalTime(state.hour, state.minute)) }
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
            if (!valid) {
                Text(
                    text = stringResource(Res.string.planning_detail_departure_before_arrival),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}
