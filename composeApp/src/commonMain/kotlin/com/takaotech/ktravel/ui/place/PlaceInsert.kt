package com.takaotech.ktravel.ui.place

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.takaotech.ktravel.presentation.place.PlaceInputMode
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInsert(
    placeName: TextFieldValue,

    inputMode: PlaceInputMode,

    placeLat: TextFieldValue,
    placeLng: TextFieldValue,

    searchQuery: TextFieldValue,

    onPlaceNameChange: (TextFieldValue) -> Unit,
    onInputModeChange: (PlaceInputMode) -> Unit,
    onPlaceLatChange: (TextFieldValue) -> Unit,
    onPlaceLngChange: (TextFieldValue) -> Unit,

    modifier: Modifier = Modifier,
    timePickerState: TimePickerState? = null,
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

        val options = PlaceInputMode.entries
        var expanded by remember { mutableStateOf(false) }
        val textFieldState = rememberTextFieldState(stringResource(options[0].label))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                state = textFieldState,
                readOnly = true,
                lineLimits = TextFieldLineLimits.SingleLine,
                label = { Text("Label") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEachIndexed { index, option ->
                    val text = stringResource(option.label)

                    DropdownMenuItem(
                        text = {
                            Text(text, style = MaterialTheme.typography.bodyLarge)
                        },
                        onClick = {
                            expanded = false
                            textFieldState.setTextAndPlaceCursorAtEnd(text)
                            onInputModeChange(option)
                        },
                    )
                }
            }
        }



        when (inputMode) {
            PlaceInputMode.LAT_LNG -> {
                Row {
                    OutlinedTextField(
                        label = { Text("Lat") },
                        value = placeLat,
                        onValueChange = onPlaceLatChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        label = { Text("Lng") },
                        value = placeLng,
                        onValueChange = onPlaceLngChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)

                    )
                }
            }

            PlaceInputMode.SEARCH -> {
                SearchPlaceInsert(
                    searchQuery = searchQuery,
                    onSearchQueryChange = {

                    },
                    onPlaceSelected = { name, lat, lng ->

                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
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

@Composable
fun SearchPlaceInsert(
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onPlaceSelected: (name: String, lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            label = { Text("Cerca luogo") },
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth()
        )

        // TODO: Implementare lista risultati ricerca
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
private fun PlaceInsertPreview() {
    PlaceInsert(
        placeName = TextFieldValue(),
        placeLat = TextFieldValue(),
        placeLng = TextFieldValue(),
        searchQuery = TextFieldValue(),
        timePickerState = rememberTimePickerState(),
        onPlaceNameChange = { },
        onPlaceLatChange = { },
        onPlaceLngChange = { },
        inputMode = PlaceInputMode.LAT_LNG,
        onInputModeChange = {}
    )
}

@Composable
@Preview(showBackground = true)
private fun SearchPlaceInsertPreview() {
    SearchPlaceInsert(
        searchQuery = TextFieldValue(),
        onSearchQueryChange = { },
        onPlaceSelected = { _, _, _ -> },
    )
}