package com.takaotech.ktravel.ui.shared.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.ui.shared.format.formatIso
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.date_range
import ktravel.composeapp.generated.resources.travel_creation_period_label
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/**
 * A read-only field showing the period of a trip, and the sheet that changes it.
 *
 * The field never takes typed input: a range is picked, not written, so the text is derived from
 * the state and the only way to change it is the picker behind the trailing icon. The new range is
 * reported when the sheet is dismissed rather than on every tap, because picking a range takes two
 * taps and the first one on its own is not an answer.
 *
 * @param showDateRangePicker Whether the picker sheet is open.
 * @param dateRangePickerState The state the picker reads and writes the range through.
 * @param modifier The modifier applied to the field.
 * @param onShowDateRangePicker Called with whether the sheet should be open.
 * @param onPlanDateRangeChanged Called with the range once the sheet closes on a complete
 *   selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelDateRangePicker(
    showDateRangePicker: Boolean,
    dateRangePickerState: DateRangePickerState,
    modifier: Modifier = Modifier,
    onShowDateRangePicker: (Boolean) -> Unit,
    onPlanDateRangeChanged: (start: Long, end: Long) -> Unit,
) {
    val startDateMillis = dateRangePickerState.selectedStartDateMillis
    val endDateMillis = dateRangePickerState.selectedEndDateMillis

    val dateText by remember(
        dateRangePickerState.selectedStartDateMillis,
        dateRangePickerState.selectedEndDateMillis,
    ) {
        derivedStateOf {
            buildString {
                val formatMillis = { millis: Long ->
                    Instant.fromEpochMilliseconds(millis)
                        .toLocalDate()
                        .formatIso()
                        .let { append(it) }
                }

                if (startDateMillis != null) {
                    formatMillis(startDateMillis)
                    if (endDateMillis != null) {
                        if (startDateMillis != endDateMillis) {
                            append(" - ")
                            formatMillis(endDateMillis)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier,
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = stringResource(Res.string.travel_creation_period_label),
                )
            },
            value = dateText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        onShowDateRangePicker(!showDateRangePicker)
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.date_range),
                        contentDescription = null,
                    )
                }
            },
        )

        if (showDateRangePicker) {
            // TODO Change implementation by platform, use popup for desktop

            ModalBottomSheet(
                onDismissRequest = {
                    onShowDateRangePicker(false)
                    val startDateSelected = dateRangePickerState.selectedStartDateMillis
                    val endDateSelected = dateRangePickerState.selectedEndDateMillis

//                    if (startDateSelected == startDateMillis && endDateSelected == endDateMillis) {
//                        return@ModalBottomSheet
//                    }

                    if (startDateSelected != null && endDateSelected != null) {
                        onPlanDateRangeChanged(startDateSelected, endDateSelected)
                    }
                },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    DateRangePicker(state = dateRangePickerState)
                }
            }
        }
    }
}
