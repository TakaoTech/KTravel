package com.takaotech.ktravel.ui.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.takaotech.ktravel.presentation.settings.SettingsViewModel
import com.takaotech.ktravel.ui.settings.SettingsNavigation
import com.takaotech.ktravel.ui.settings.SettingsPage
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.settingsNavGraph(navController: NavHostController) {
    composable<SettingsNavigation> {
        val viewModel = koinViewModel<SettingsViewModel>()

        SettingsPage(
            viewModel = viewModel,
            onNavigationBackClick = {
                navController.navigateUp()
            }
        )
    }
}
