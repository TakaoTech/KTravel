package com.takaotech.ktravel.ui.planning.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.takaotech.ktravel.PlanningNavigation
import com.takaotech.ktravel.presentation.planning.PlanningDetailViewModel
import com.takaotech.ktravel.presentation.planning.PlanningViewModel
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportNavigationEvent
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportViewModel
import com.takaotech.ktravel.sharedKoinViewModel2
import com.takaotech.ktravel.ui.place.PlaceInsertNavigation
import com.takaotech.ktravel.ui.place.navigation.placeInsertNavGraph
import com.takaotech.ktravel.ui.planning.detail.PlanningDetailPage
import com.takaotech.ktravel.ui.planning.detail.PlanningDetailPageNavigation
import com.takaotech.ktravel.ui.planning.transport.*
import com.takaotech.ktravel.ui.planning.trip.PlanningTripPage
import com.takaotech.ktravel.ui.planning.trip.PlanningTripPageNavigation
import com.takaotech.ktravel.ui.settings.navigation.settingsNavGraph
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.planningNavGraph(navController: NavHostController) {
    navigation<PlanningNavigation>(startDestination = PlanningTripPageNavigation) {
        composable<PlanningTripPageNavigation> {
            val viewModel = it.sharedKoinViewModel2<PlanningViewModel>(navController)
            val coroutine = rememberCoroutineScope()

            val launcher = rememberFileSaverLauncher(FileKitDialogSettings.createDefault()) { file ->
                // Write your data to the file
            }

            PlanningTripPage(
                viewModel = viewModel,
                onAddPlaceClicked = {
                    navController.navigate(PlaceInsertNavigation())
                },
                onDateClicked = {
                    navController.navigate(PlanningDetailPageNavigation(it))
                },
                onSettingClicked = {
                    navController.navigate(com.takaotech.ktravel.ui.settings.SettingsNavigation)
                },
                onSaveClick = {
                    coroutine.launch {
                        launcher.launch(viewModel.uiState.value.planHeader.name.text, "json")
                    }
                }
            )
        }

        composable<PlanningDetailPageNavigation> { backStackEntry ->
            val args = backStackEntry.toRoute<PlanningDetailPageNavigation>()
            val viewModel = koinViewModel<PlanningDetailViewModel> {
                parametersOf(args.id)
            }

            val travelDay by viewModel.travelDay.collectAsStateWithLifecycle()

            PlanningDetailPage(
                steps = travelDay.steps,
                places = travelDay.places,
                onAddPlaceClick = {
                    navController.navigate(PlaceInsertNavigation(travelDay.id))
                },
                onDeletePlaceClick = {
                    viewModel.movePlaceToGeneral(it)
                },
                onDeletePermanentPlaceClick = {
                    viewModel.deletePlace(it)
                },
                onStepDeleteClicked = {
                    viewModel.moveStepToPlace(it)
                },
                onNavigationBackClick = {
                    navController.navigateUp()
                },
                onMovePlaceToList = {
                    viewModel.movePlaceToStep(it)
                },
                onStepMoveDown = {
                    viewModel.moveStepDown(it)
                },
                onStepMoveUp = {
                    viewModel.moveStepUp(it)
                },
                onTransportAddClick = { startId, endId ->
                    navController.navigate(
                        PlanningTransportPageNavigation(travelDay.id, startId, endId)
                    )
                }
            )
        }

        navigation<PlanningTransportNavigation>(startDestination = PlanningTransportPageNavigation::class) {
            composable<PlanningTransportPageNavigation> { backStackEntry ->
                val args = backStackEntry.toRoute<PlanningTransportPageNavigation>()
                val viewModel =
                    backStackEntry.sharedKoinViewModel2<PlanningTransportViewModel>(navController) {
                        parametersOf(args.dayId, args.startPlaceId, args.endPlaceId)
                    }

                LaunchedEffect(viewModel) {
                    viewModel.navigationEvent.collect { event ->
                        when (event) {
                            is PlanningTransportNavigationEvent.NavigateToRoutePreview -> {
                                navController.navigate(
                                    PlanningTransportRoutePreviewPageNavigation(
                                        args.dayId,
                                        args.startPlaceId,
                                        args.endPlaceId
                                    )
                                )
                            }
                        }
                    }
                }

                PlanningTransportPage(
                    viewModel = viewModel,
                    onNavigationBackClick = {
                        navController.navigateUp()
                    }
                )
            }

            composable<PlanningTransportRoutePreviewPageNavigation> { backStackEntry ->
                val args = backStackEntry.toRoute<PlanningTransportRoutePreviewPageNavigation>()
                val viewModel =
                    backStackEntry.sharedKoinViewModel2<PlanningTransportViewModel>(navController) {
                        parametersOf(args.dayId, args.startPlaceId, args.endPlaceId)
                    }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                uiState.routes?.let { routes ->
                    PlanningTransportRoutePreviewPage(
                        routes = routes,
                        selectedRouteIndex = uiState.selectedRouteIndex,
                        onRouteConfirm = {
                            viewModel.saveSelectedRoute()
                            navController.popBackStack<PlanningDetailPageNavigation>(inclusive = false)
                        },
                        onRouteChange = { viewModel.selectRoute(it) }
                    )
                }
            }
        }

        settingsNavGraph(navController)
        placeInsertNavGraph(navController)
    }
}
