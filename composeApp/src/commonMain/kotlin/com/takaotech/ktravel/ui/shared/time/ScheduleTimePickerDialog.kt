package com.takaotech.ktravel.ui.shared.time

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_departure_before_arrival
import ktravel.composeapp.generated.resources.time_picker_cancel
import ktravel.composeapp.generated.resources.time_picker_confirm
import org.jetbrains.compose.resources.stringResource

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
