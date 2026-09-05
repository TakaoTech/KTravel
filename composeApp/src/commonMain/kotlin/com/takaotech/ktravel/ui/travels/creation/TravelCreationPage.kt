package com.takaotech.ktravel.ui.travels.creation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.presentation.travels.TravelCreationViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelCreationPage(
    onBackClick: () -> Unit,
    onNavigateToPlanning: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: TravelCreationViewModel = metroViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdTravelId) {
        uiState.createdTravelId?.let { id ->
            onNavigateToPlanning(id)
        }
    }

    TravelCreationContent(
        modifier = modifier,
        travelName = uiState.travelName,
        startDateMillis = uiState.startDateMillis,
        endDateMillis = uiState.endDateMillis,
        error = uiState.error,
        onNameChange = { viewModel.onNameChange(it) },
        onPlanDateRangeChanged = { start, end ->
            viewModel.onDateRangeChange(start, end)
        },
        onBackClick = onBackClick,
        onConfirmClick = { viewModel.createTravelPlan() },
        onDismissError = { viewModel.clearError() },
    )
}
