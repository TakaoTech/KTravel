package com.takaotech.ktravel.ui.plan.place

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.presentation.place.PlaceInsertViewModel
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.close
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInsertPage(
    viewModel: PlaceInsertViewModel,
    onExit: () -> Unit,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // TODO Adapt Layout cross devices

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Place Insert") },
                navigationIcon = {
                    IconButton(
                        onClick = onExit,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.close),
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.savePlace()
                        if (!uiState.isBulk) {
                            onSaveClicked()
                        }
                    },
                ) {
                    Text("Save")
                }
            }
        },
    ) {
        PlaceInsert(
            placeName = uiState.placeName,
            placeLat = uiState.placeLat,
            placeLng = uiState.placeLng,
            onPlaceNameChange = {
                viewModel.onPlaceNameChanged(it)
            },
            onPlaceLatChange = {
                viewModel.onPlaceLatChanged(it)
            },
            onPlaceLngChange = {
                viewModel.onPlaceLngChanged(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(it),
            inputMode = uiState.inputMode,
            onInputModeChange = { viewModel.onInputModeChanged(it) },
            searchQuery = uiState.searchQuery.value,
            isBulk = uiState.isBulk,
            onBulkChanged = { isBulk -> viewModel.onBulkChanged(isBulk) },
        )
    }
}
