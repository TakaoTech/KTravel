package com.takaotech.ktravel.ui.place

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatLngPlaceInsert(
    placeName: TextFieldValue,
    placeLat: TextFieldValue,
    placeLng: TextFieldValue,
    timePickerState: TimePickerState? = null,

    onPlaceNameChange: (TextFieldValue) -> Unit,
    onPlaceLatChange: (TextFieldValue) -> Unit,
    onPlaceLngChange: (TextFieldValue) -> Unit,

    modifier: Modifier = Modifier,
) {

    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        Row {
            OutlinedTextField(
                label = { Text("Place name") },
                value = placeName,
                onValueChange = onPlaceNameChange
            )

            //TODO Image load

//            IconButton(
//                onClick = {
//
//                }
//            ){
//                Icon()
//            }
        }

        Row {
            OutlinedTextField(
                label = { Text("Lat") },
                value = placeLat,
                onValueChange = onPlaceLatChange
            )

            OutlinedTextField(
                label = { Text("Lng") },
                value = placeLng,
                onValueChange = onPlaceLngChange
            )
        }

        if (timePickerState != null) {
            OutlinedTextField(
                label = { Text("Hour") },
                value = "${timePickerState.hour}:${timePickerState.minute}",
                onValueChange = {},
                readOnly = true,
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    showTimePicker = true
                                }
                            }
                        }
                    }
            )

            if (showTimePicker) {
                TimePickerDialog(
                    onDismissRequest = {
                        showTimePicker = false
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showTimePicker = false
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    title = {
                        Text("Select time")
                    },
                    content = { TimePicker(state = timePickerState) }
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
private fun LatLngPlaceInsertPreview() {
    LatLngPlaceInsert(
        placeName = TextFieldValue(),
        placeLat = TextFieldValue(),
        placeLng = TextFieldValue(),
        timePickerState = rememberTimePickerState(),
        onPlaceNameChange = { },
        onPlaceLatChange = { },
        onPlaceLngChange = { },
    )
}