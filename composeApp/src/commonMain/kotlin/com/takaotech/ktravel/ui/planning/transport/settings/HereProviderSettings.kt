package com.takaotech.ktravel.ui.planning.transport.settings

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HereProviderSettings(
    settings: RoutingProviderSettings.Here,
    onSettingsChange: (RoutingProviderSettings.Here) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = null
    )

    val timePickerState = rememberTimePickerState(
        initialHour = settings.departureTime?.hour ?: 9,
        initialMinute = settings.departureTime?.minute ?: 0
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Transport Mode")
        val transportModes = RoutingProviderSettings.Here.HereTransportMode.entries
        val selectedTransportIndex = transportModes.indexOf(settings.transportMode)

        SecondaryTabRow(
            selectedTabIndex = selectedTransportIndex,
            containerColor = TabRowDefaults.primaryContainerColor,
            contentColor = TabRowDefaults.primaryContentColor,
            tabs = {
                transportModes.forEachIndexed { index, mode ->
                    Tab(
                        selected = index == selectedTransportIndex,
                        onClick = { onSettingsChange(settings.copy(transportMode = mode)) },
                        text = { Text(text = mode.name) }
                    )
                }
            }
        )

        Text("Routing Mode")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            RoutingProviderSettings.Here.HereRoutingMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = settings.routingMode == mode,
                    onClick = { onSettingsChange(settings.copy(routingMode = mode)) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = RoutingProviderSettings.Here.HereRoutingMode.entries.size
                    )
                ) {
                    Text(mode.name)
                }
            }
        }

        HereDateTime(
            titleDate = "Departure Date",
            titleTime = "Departure Time",
            settings = settings,
            showTimePicker = showTimePicker,
            showDatePicker = showDatePicker,
            datePickerState = datePickerState,
            onSettingsChange = onSettingsChange,
            timePickerState = timePickerState,
            onShowDatePicker = {
                showDatePicker = it
            },
            onShowTimePicker = {
                showTimePicker = it
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HereDateTime(
    titleDate: String,
    titleTime: String,
    timePickerState: TimePickerState,
    datePickerState: DatePickerState,
    settings: RoutingProviderSettings.Here,
    showTimePicker: Boolean,
    showDatePicker: Boolean,
    modifier: Modifier = Modifier,
    onSettingsChange: (RoutingProviderSettings.Here) -> Unit,
    onShowDatePicker: (Boolean) -> Unit,
    onShowTimePicker: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        Row {
            val timeText = if (settings.departureTime != null) {
                "${settings.departureTime.hour.toString().padStart(2, '0')}:${
                    settings.departureTime.minute.toString().padStart(2, '0')
                }"
            } else {
                "Not set"
            }
            OutlinedTextField(
                value = timeText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .weight(1f),
                label = { Text(titleTime) },
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    onShowTimePicker(true)
                                }
                            }
                        }
                    }
            )

            Spacer(Modifier.width(16.dp))

            val dateText = settings.departureDate?.toString() ?: "Not set"
            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f),
                label = { Text(titleDate) },
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    onShowDatePicker(true)
                                }
                            }
                        }
                    }
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { onShowDatePicker(false) },
                    confirmButton = {
                        Button(
                            onClick = {
                                val selectedMillis = datePickerState.selectedDateMillis
                                val newDate = selectedMillis?.toLocalDate()
                                onSettingsChange(settings.copy(departureDate = newDate))
                                onShowDatePicker(false)
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                onSettingsChange(settings.copy(departureDate = null))
                                onShowDatePicker(false)
                            }
                        ) {
                            Text("Clear")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { onShowTimePicker(false) },
                    title = { Text("Select departure time") },
                    text = { TimePicker(state = timePickerState) },
                    confirmButton = {
                        Button(
                            onClick = {
                                val newTime = LocalTime(timePickerState.hour, timePickerState.minute)
                                onSettingsChange(settings.copy(departureTime = newTime))
                                onShowTimePicker(false)
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                onSettingsChange(settings.copy(departureTime = null))
                                onShowTimePicker(false)
                            }
                        ) {
                            Text("Clear")
                        }
                    }
                )
            }
        }

    }
}

@PreviewScreenSizes
@Composable
private fun HereProviderSettingsPreview() {
    HereProviderSettings(
        settings = RoutingProviderSettings.Here(),
        onSettingsChange = {}
    )
}
