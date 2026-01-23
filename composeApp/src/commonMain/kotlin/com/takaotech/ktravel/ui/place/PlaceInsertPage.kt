package com.takaotech.ktravel.ui.place

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.close
import org.jetbrains.compose.resources.painterResource

@Serializable
object PlaceInsertNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInsertPage(
    onExit: () -> Unit,
    onSaveClicked: () -> Unit
) {
    var placeName by remember { mutableStateOf(TextFieldValue()) }
    var placeLat by remember { mutableStateOf(TextFieldValue()) }
    var placeLng by remember { mutableStateOf(TextFieldValue()) }
    val timePickerState = rememberTimePickerState()

    //TODO Adapt Layout cross devices

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Place Insert") },
                navigationIcon = {
                    IconButton(
                        onClick = onExit
                    ) {
                        Icon(painter = painterResource(Res.drawable.close), contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSaveClicked
                ) {
                    Text("Save")
                }
            }
        }
    ) {
        LatLngPlaceInsert(
            placeName = placeName,
            placeLat = placeLat,
            placeLng = placeLng,
            timePickerState = timePickerState,
            onPlaceNameChange = {
                placeName = it
            },
            onPlaceLatChange = {
                placeLat = it
            },
            onPlaceLngChange = {
                placeLng = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
        )
    }
}