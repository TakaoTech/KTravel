package com.takaotech.ktravel.ui.plan.transport.composer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.presentation.plan.transport.TransportPlanningViewModel

@Composable
fun TransportPlanningPage(
    viewModel: TransportPlanningViewModel,
    modifier: Modifier = Modifier,
    onNavigationBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TransportPlanningContent(
        modifier = modifier,
        uiState = uiState,
        onNavigationBackClick = onNavigationBackClick,
        onNavigatorChange = viewModel::selectNavigator,
        onRetryCatalog = viewModel::retryCatalog,
        onProfileChange = viewModel::selectProfile,
        onTimeModeChange = viewModel::setTimeMode,
        onTimeChange = viewModel::setTime,
        onCalculateClick = viewModel::calculateTransport,
        onFailureDismiss = viewModel::dismissFailure,
    )
}
