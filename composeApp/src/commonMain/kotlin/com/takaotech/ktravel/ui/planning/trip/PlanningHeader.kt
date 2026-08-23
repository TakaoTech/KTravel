package com.takaotech.ktravel.ui.planning.trip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.ui.common.formatIso
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.date_range
import ktravel.composeapp.generated.resources.edit
import ktravel.composeapp.generated.resources.planning_page_trip_name_label
import ktravel.composeapp.generated.resources.planning_trip_cd_edit_name
import ktravel.composeapp.generated.resources.travel_creation_period_label
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
fun PlanningHeader(
    name: TextFieldValue,
    startDateMillis: Long,
    endDateMillis: Long,
    modifier: Modifier = Modifier,
    onNameChange: (TextFieldValue) -> Unit,
    onPlanDateRangeChanged: (start: Long, end: Long) -> Unit,
) {
    var showDateRangePicker by remember { mutableStateOf(false) }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDateMillis,
        initialSelectedEndDateMillis = endDateMillis,
    )

    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.titleMedium,
            label = {
                Text(
                    text = stringResource(Res.string.planning_page_trip_name_label),
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.edit),
                    contentDescription = stringResource(Res.string.planning_trip_cd_edit_name),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            // The field reads as a plain surface row: the container carries the affordance, not an
            // underline.
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//                focusedIndicatorColor = Color.Transparent,
//                unfocusedIndicatorColor = Color.Transparent,
//                disabledIndicatorColor = Color.Transparent,
//            ),
        )

        Spacer(Modifier.height(16.dp))

        TravelDateRangePicker(
            showDateRangePicker = showDateRangePicker,
            dateRangePickerState = dateRangePickerState,
            onShowDateRangePicker = {
                showDateRangePicker = it
            },
            onPlanDateRangeChanged = onPlanDateRangeChanged,
        )
    }
}

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

    val dateText by remember(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis) {
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

@OptIn(ExperimentalTime::class)
@Preview(showSystemUi = true)
@Composable
private fun PlannerHeaderPreview() = KTravelTheme {
    val loremIpsum = LoremIpsum(10).values.first()

    var pickedDate by remember {
        val time = Clock.System.now().toEpochMilliseconds()
        mutableStateOf(time to time + 1.days.toLong(DurationUnit.MILLISECONDS))
    }

    PlanningHeader(
        name = TextFieldValue(loremIpsum),
        startDateMillis = pickedDate.first,
        endDateMillis = pickedDate.second,
        modifier = Modifier.fillMaxWidth(),
        onNameChange = {},
        onPlanDateRangeChanged = { start, end ->
        },
    )
}
