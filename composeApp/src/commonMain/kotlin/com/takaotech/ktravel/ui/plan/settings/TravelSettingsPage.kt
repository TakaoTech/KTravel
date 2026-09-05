package com.takaotech.ktravel.ui.plan.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.presentation.settings.TravelSettingsViewModel
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.settings_saved
import org.jetbrains.compose.resources.stringResource

/**
 * The trip settings, wired to [viewModel].
 *
 * Holds the two things [TravelSettingsContent] cannot: the state collection, and the confirmation
 * posted once the plan has been written, which is acknowledged so a configuration change does not
 * announce itself twice.
 *
 * @param onNavigationBackClick What leaving the screen means to the caller.
 */
@Composable
fun TravelSettingsPage(
    viewModel: TravelSettingsViewModel,
    onNavigationBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(Res.string.settings_saved)

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.onSavedMessageShown()
        }
    }

    TravelSettingsContent(
        uiState = uiState,
        onNavigationBackClick = onNavigationBackClick,
        onHereApiKeyChange = viewModel::onHereApiKeyChanged,
        onApiKeyVisibilityToggle = viewModel::onApiKeyVisibilityToggled,
        onNavigatorPreferenceChange = viewModel::onNavigatorPreferenceChanged,
        onNavigatorBaseUrlChange = viewModel::onNavigatorBaseUrlChanged,
        onTestConnectionClick = viewModel::checkNavigatorReachability,
        onSaveClick = viewModel::saveSettings,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}
