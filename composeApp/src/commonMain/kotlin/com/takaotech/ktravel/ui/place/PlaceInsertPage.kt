package com.takaotech.ktravel.ui.place

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.presentation.place.PlaceInsertViewModel
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.close
import org.jetbrains.compose.resources.painterResource

/**
 *  Class for navigate to PlaceInsertPage
 *
 * @property dayId The identifier of the day where the place is to be inserted.
 * Pass null for insert place to root.
 */
@Serializable
data class PlaceInsertNavigation(
    val dayId: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInsertPage(
    viewModel: PlaceInsertViewModel,
    onExit: () -> Unit,
    onSaveClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                    onClick = {
                        viewModel.savePlace()
                        onSaveClicked()
                    }
                ) {
                    Text("Save")
                }
            }
        }
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
            searchQuery = uiState.searchQuery.value
        )
    }
}