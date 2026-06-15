package com.takaotech.ktravel

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.takaotech.ktravel.core.KTravelPlatform
import com.takaotech.ktravel.core.ui.lifecycleIsResumed
import com.takaotech.ktravel.di.createAppGraph
import com.takaotech.ktravel.presentation.place.PlaceInsertViewModel
import com.takaotech.ktravel.presentation.planning.PlanningDetailViewModel
import com.takaotech.ktravel.presentation.planning.PlanningViewModel
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportNavigationEvent
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportViewModel
import com.takaotech.ktravel.presentation.settings.SettingsViewModel
import com.takaotech.ktravel.ui.intro.TravelCreationPage
import com.takaotech.ktravel.ui.intro.TravelSelectionPage
import com.takaotech.ktravel.ui.place.PlaceInsertNavigation
import com.takaotech.ktravel.ui.place.PlaceInsertPage
import com.takaotech.ktravel.ui.planning.detail.PlanningDetailPage
import com.takaotech.ktravel.ui.planning.detail.PlanningDetailPageNavigation
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportNavigation
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportPage
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportPageNavigation
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportRoutePreviewPage
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportRoutePreviewPageNavigation
import com.takaotech.ktravel.ui.planning.trip.PlanningTripPage
import com.takaotech.ktravel.ui.planning.trip.PlanningTripPageNavigation
import com.takaotech.ktravel.ui.settings.SettingsNavigation
import com.takaotech.ktravel.ui.settings.SettingsPage
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data class PlanningNavigation(val travelId: String)

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
@Preview
fun App() {
    val appGraph = remember { createAppGraph() }

    KTravelPlatform {
        MaterialTheme {
            val navController = rememberNavController()

            CompositionLocalProvider(LocalMetroViewModelFactory provides appGraph.metroViewModelFactory) {
                NavHost(navController = navController, startDestination = TravelSelectionPage) {
                    composable<TravelSelectionPage> {
                        TravelSelectionPage(
                            onNewTravelClick = {
                                navController.navigate(TravelCreationPage)
                            },
                            onTravelClick = { id ->
                                appGraph.planningGraphStore.getOrCreate(id)
                                navController.navigate(PlanningNavigation(id)) {
                                    popUpTo(TravelSelectionPage) { inclusive = false }
                                }
                            }
                        )
                    }

                    composable<TravelCreationPage> {
                        TravelCreationPage(
                            onBackClick = {
                                if (it.lifecycleIsResumed()) {
                                    navController.navigateUp()
                                }
                            },
                            onNavigateToPlanning = { travelId ->
                                navController.navigate(PlanningNavigation(travelId)) {
                                    popUpTo(TravelSelectionPage) { inclusive = false }
                                }
                            }
                        )
                    }

                    navigation<PlanningNavigation>(startDestination = PlanningTripPageNavigation::class) {
                        composable<PlanningTripPageNavigation> { backStackEntry ->
                            val args = backStackEntry.toRoute<PlanningTripPageNavigation>()
                            val viewModel =
                                assistedMetroViewModel<PlanningViewModel, PlanningViewModel.Factory>(
                                    viewModelStoreOwner = backStackEntry,
                                    key = args.travelId
                                ) { _ -> create(args.travelId) }
                            val coroutine = rememberCoroutineScope()

                            val launcher =
                                rememberFileSaverLauncher(FileKitDialogSettings.createDefault()) { _ -> }

                            NavigationBackHandler(
                                state = rememberNavigationEventState(NavigationEventInfo.None),
                                isBackEnabled = true,
                                onBackCompleted = {
                                    if (backStackEntry.lifecycleIsResumed()) {
                                        navController.navigateUp()
                                    }
                                }
                            )

                            PlanningTripPage(
                                viewModel = viewModel,
                                onAddPlaceClicked = {
                                    navController.navigate(PlaceInsertNavigation())
                                },
                                onDateClicked = {
                                    navController.navigate(PlanningDetailPageNavigation(it))
                                },
                                onSettingClicked = {
                                    navController.navigate(SettingsNavigation)
                                },
                                onSaveClick = {
                                    coroutine.launch {
                                        launcher.launch(
                                            viewModel.uiState.value.planHeader.name.text,
                                            "json"
                                        )
                                    }
                                }
                            )
                        }

                        composable<PlanningDetailPageNavigation> { backStackEntry ->
                            val args = backStackEntry.toRoute<PlanningDetailPageNavigation>()
                            val parentArgs =
                                navController.getBackStackEntry<PlanningNavigation>()
                                    .toRoute<PlanningNavigation>()
                            val viewModel =
                                assistedMetroViewModel<PlanningDetailViewModel, PlanningDetailViewModel.Factory>(
                                    key = "detail_${args.id}"
                                ) { _ -> create(parentArgs.travelId, args.id) }

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
                                    if (backStackEntry.lifecycleIsResumed()) {
                                        navController.navigateUp()
                                    }
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
                                        PlanningTransportPageNavigation(
                                            travelDay.id,
                                            startId,
                                            endId
                                        )
                                    )
                                }
                            )
                        }

                        navigation<PlanningTransportNavigation>(startDestination = PlanningTransportPageNavigation::class) {
                            composable<PlanningTransportPageNavigation> { backStackEntry ->
                                val args = backStackEntry.toRoute<PlanningTransportPageNavigation>()
                                val parentArgs =
                                    navController.getBackStackEntry<PlanningNavigation>()
                                        .toRoute<PlanningNavigation>()
                                val transportEntry = remember(backStackEntry) {
                                    navController.getBackStackEntry<PlanningTransportNavigation>()
                                }
                                val viewModel =
                                    assistedMetroViewModel<PlanningTransportViewModel, PlanningTransportViewModel.Factory>(
                                        viewModelStoreOwner = transportEntry
                                    ) { _ ->
                                        create(
                                            parentArgs.travelId,
                                            args.dayId,
                                            args.startPlaceId,
                                            args.endPlaceId
                                        )
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
                                        if (backStackEntry.lifecycleIsResumed()) {
                                            navController.navigateUp()
                                        }
                                    }
                                )
                            }

                            composable<PlanningTransportRoutePreviewPageNavigation> { backStackEntry ->
                                val transportEntry = remember(backStackEntry) {
                                    navController.getBackStackEntry<PlanningTransportNavigation>()
                                }
                                val viewModel =
                                    viewModel<PlanningTransportViewModel>(viewModelStoreOwner = transportEntry)

                                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                                uiState.routes?.let { routes ->
                                    PlanningTransportRoutePreviewPage(
                                        routes = routes,
                                        selectedRouteIndex = uiState.selectedRouteIndex,
                                        onRouteConfirm = {
                                            viewModel.saveSelectedRoute()
                                            navController.popBackStack<PlanningDetailPageNavigation>(
                                                inclusive = false
                                            )
                                        },
                                        onRouteChange = { viewModel.selectRoute(it) }
                                    )
                                }
                            }
                        }
                    }

                    composable<PlaceInsertNavigation> { backStackEntry ->
                        val args = backStackEntry.toRoute<PlaceInsertNavigation>()
                        val travelId = navController.getBackStackEntry<PlanningNavigation>()
                            .toRoute<PlanningNavigation>().travelId
                        val viewModel =
                            assistedMetroViewModel<PlaceInsertViewModel, PlaceInsertViewModel.Factory>(
                                key = "place_${travelId}_${args.dayId}"
                            ) { _ -> create(travelId, args.dayId) }

                        PlaceInsertPage(
                            viewModel = viewModel,
                            onExit = {
                                if (backStackEntry.lifecycleIsResumed()) {
                                    navController.navigateUp()
                                }
                            },
                            onSaveClicked = {
                                navController.navigateUp()
                            }
                        )
                    }

                    composable<SettingsNavigation> { backStackEntry ->
                        val viewModel: SettingsViewModel = metroViewModel()

                        SettingsPage(
                            viewModel = viewModel,
                            onNavigationBackClick = {
                                if (backStackEntry.lifecycleIsResumed()) {
                                    navController.navigateUp()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
