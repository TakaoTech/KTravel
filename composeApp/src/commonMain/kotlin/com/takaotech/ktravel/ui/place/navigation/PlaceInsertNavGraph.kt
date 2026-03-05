package com.takaotech.ktravel.ui.place.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.takaotech.ktravel.presentation.place.PlaceInsertViewModel
import com.takaotech.ktravel.ui.place.PlaceInsertNavigation
import com.takaotech.ktravel.ui.place.PlaceInsertPage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.placeInsertNavGraph(navController: NavHostController) {
    composable<PlaceInsertNavigation> { backStackEntry ->
        val args = backStackEntry.toRoute<PlaceInsertNavigation>()

        val viewModel = koinViewModel<PlaceInsertViewModel> {
            parametersOf(args.dayId)
        }

        PlaceInsertPage(
            viewModel = viewModel,
            onExit = {
                navController.navigateUp()
            },
            onSaveClicked = {
                navController.navigateUp()
            }
        )
    }
}
