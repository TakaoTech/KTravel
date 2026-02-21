package com.takaotech.ktravel.ui.planning.trip

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.core.toLocalDate
import kotlinx.datetime.LocalDate
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.date_range
import org.jetbrains.compose.resources.painterResource
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
        initialSelectedEndDateMillis = endDateMillis
    )

    val dateText by remember(startDateMillis, endDateMillis) {
        derivedStateOf {
            buildString {
                val formatMillis = { millis: Long ->
                    Instant.fromEpochMilliseconds(millis)
                        .toLocalDate()
                        .let { date -> LocalDate.Formats.ISO.format(date) }
                        .let { append(it) }
                }

                formatMillis(startDateMillis)
                if (startDateMillis != endDateMillis) {
                    append(" - ")
                    formatMillis(endDateMillis)
                }
            }
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        showDateRangePicker = !showDateRangePicker
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.date_range),
                        contentDescription = null,
                    )
                }
            }
        )
    }

    if (showDateRangePicker) {
        //TODO Change implementation by platform, use popup for desktop

        ModalBottomSheet(
            onDismissRequest = {
                showDateRangePicker = false
                val startDateSelected = dateRangePickerState.selectedStartDateMillis
                val endDateSelected = dateRangePickerState.selectedEndDateMillis

                if (startDateSelected == startDateMillis && endDateSelected == endDateMillis) {
                    return@ModalBottomSheet
                }

                if (startDateSelected != null && endDateSelected != null) {
                    onPlanDateRangeChanged(startDateSelected, endDateSelected)
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                DateRangePicker(state = dateRangePickerState)
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview(showBackground = true)
@Composable
private fun PlannerHeaderPreview() {
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

        }
    )
}