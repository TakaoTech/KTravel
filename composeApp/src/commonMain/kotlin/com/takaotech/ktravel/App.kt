package com.takaotech.ktravel

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavEvent
import com.takaotech.ktravel.core.KTravelPlatform
import com.takaotech.ktravel.core.ui.lifecycleIsResumed
import com.takaotech.ktravel.di.createAppGraph
import com.takaotech.ktravel.domain.routing.model.RouteResult
import com.takaotech.ktravel.presentation.place.PlaceInsertViewModel
import com.takaotech.ktravel.presentation.planning.PlanningViewModel
import com.takaotech.ktravel.presentation.planning.detail.AddPlaceScreen
import com.takaotech.ktravel.presentation.planning.detail.AddTransportScreen
import com.takaotech.ktravel.presentation.planning.detail.PlanningDetailScreen
import com.takaotech.ktravel.presentation.planning.detail.StepDetailScreen
import com.takaotech.ktravel.presentation.planning.detail.TransportDetailScreen
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportNavigationEvent
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportViewModel
import com.takaotech.ktravel.presentation.settings.SettingsViewModel
import com.takaotech.ktravel.ui.intro.TravelCreationPage
import com.takaotech.ktravel.ui.intro.TravelSelectionPage
import com.takaotech.ktravel.ui.place.PlaceInsertNavigation
import com.takaotech.ktravel.ui.place.PlaceInsertPage
import com.takaotech.ktravel.ui.planning.detail.PlanningDetailPageNavigation
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportNavigation
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportPage
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportPageNavigation
import com.takaotech.ktravel.ui.planning.transport.PlanningTransportRoutePreviewPageNavigation
import com.takaotech.ktravel.ui.planning.transport.preview.RoutingRoutePreviewPage
import com.takaotech.ktravel.ui.planning.transport.preview.TransitJourneyPreviewPage
import com.takaotech.ktravel.ui.planning.trip.PlanningTripPage
import com.takaotech.ktravel.ui.planning.trip.PlanningTripPageNavigation
import com.takaotech.ktravel.ui.settings.AppSettingsNavigation
import com.takaotech.ktravel.ui.settings.AppSettingsPage
import com.takaotech.ktravel.ui.settings.SettingsNavigation
import com.takaotech.ktravel.ui.settings.SettingsPage
import com.takaotech.ktravel.ui.theme.KTravelTheme
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.serialization.Serializable

@Serializable
data class PlanningNavigation(val travelId: String)

@Serializable
data class StepDetailPageNavigation(val travelId: String, val dayId: String, val stepId: String)

@Serializable
data class TransportDetailPageNavigation(val travelId: String, val dayId: String, val stepId: String)

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
@Preview
fun App() {
    val appGraph = remember { createAppGraph() }

    KTravelPlatform {
        KTravelTheme {
            val navController = rememberNavController()

            CompositionLocalProvider(LocalMetroViewModelFactory provides appGraph.metroViewModelFactory) {
                CircuitCompositionLocals(appGraph.circuit) {
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
                                },
                                onAppSettingsClick = { navController.navigate(AppSettingsNavigation) },
                            )
                        }

                        composable<AppSettingsNavigation> { backStackEntry ->
                            AppSettingsPage(
                                viewModel = metroViewModel(),
                                onNavigationBackClick = {
                                    if (backStackEntry.lifecycleIsResumed()) {
                                        navController.navigateUp()
                                    }
                                },
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
                                },
                            )
                        }

                        navigation<PlanningNavigation>(startDestination = PlanningTripPageNavigation::class) {
                            composable<PlanningTripPageNavigation> { backStackEntry ->
                                val args = backStackEntry.toRoute<PlanningTripPageNavigation>()
                                val viewModel =
                                    assistedMetroViewModel<PlanningViewModel, PlanningViewModel.Factory>(
                                        viewModelStoreOwner = backStackEntry,
                                        key = args.travelId,
                                    ) { _ -> create(args.travelId) }

                                NavigationBackHandler(
                                    state = rememberNavigationEventState(NavigationEventInfo.None),
                                    isBackEnabled = true,
                                    onBackCompleted = {
                                        if (backStackEntry.lifecycleIsResumed()) {
                                            appGraph.planningGraphStore.release(args.travelId)
                                            navController.navigateUp()
                                        }
                                    },
                                )

                                PlanningTripPage(
                                    viewModel = viewModel,
                                    onBackClick = {
                                        if (backStackEntry.lifecycleIsResumed()) {
                                            appGraph.planningGraphStore.release(args.travelId)
                                            navController.navigateUp()
                                        }
                                    },
                                    onAddPlaceClicked = {
                                        navController.navigate(PlaceInsertNavigation())
                                    },
                                    onDateClicked = {
                                        navController.navigate(PlanningDetailPageNavigation(it))
                                    },
                                    onSettingClicked = {
                                        navController.navigate(SettingsNavigation)
                                    },
                                )
                            }

                            composable<PlanningDetailPageNavigation> { backStackEntry ->
                                val args = backStackEntry.toRoute<PlanningDetailPageNavigation>()
                                val parentArgs =
                                    navController.getBackStackEntry<PlanningNavigation>()
                                        .toRoute<PlanningNavigation>()

                                CircuitContent(
                                    screen = PlanningDetailScreen(parentArgs.travelId, args.id),
                                    onNavEvent = { event ->
                                        when (event) {
                                            is NavEvent.Pop -> {
                                                if (backStackEntry.lifecycleIsResumed()) {
                                                    navController.navigateUp()
                                                }
                                            }

                                            is NavEvent.GoTo -> when (val screen = event.screen) {
                                                is AddPlaceScreen -> navController.navigate(
                                                    PlaceInsertNavigation(screen.dayId),
                                                )

                                                is AddTransportScreen -> navController.navigate(
                                                    PlanningTransportPageNavigation(
                                                        screen.dayId,
                                                        screen.startPlaceId,
                                                        screen.endPlaceId,
                                                    ),
                                                )

                                                is StepDetailScreen -> navController.navigate(
                                                    StepDetailPageNavigation(
                                                        screen.travelId,
                                                        screen.dayId,
                                                        screen.stepId,
                                                    ),
                                                )

                                                is TransportDetailScreen -> navController.navigate(
                                                    TransportDetailPageNavigation(
                                                        screen.travelId,
                                                        screen.dayId,
                                                        screen.stepId,
                                                    ),
                                                )

                                                else -> Unit
                                            }

                                            else -> Unit
                                        }
                                    },
                                )
                            }

                            composable<StepDetailPageNavigation> { backStackEntry ->
                                val args = backStackEntry.toRoute<StepDetailPageNavigation>()

                                CircuitContent(
                                    screen = StepDetailScreen(
                                        travelId = args.travelId,
                                        dayId = args.dayId,
                                        stepId = args.stepId,
                                    ),
                                    onNavEvent = { event ->
                                        when (event) {
                                            is NavEvent.Pop -> {
                                                if (backStackEntry.lifecycleIsResumed()) {
                                                    navController.navigateUp()
                                                }
                                            }

                                            else -> Unit
                                        }
                                    },
                                )
                            }

                            composable<TransportDetailPageNavigation> { backStackEntry ->
                                val args = backStackEntry.toRoute<TransportDetailPageNavigation>()

                                CircuitContent(
                                    screen = TransportDetailScreen(
                                        travelId = args.travelId,
                                        dayId = args.dayId,
                                        stepId = args.stepId,
                                    ),
                                    onNavEvent = { event ->
                                        when (event) {
                                            is NavEvent.Pop -> {
                                                if (backStackEntry.lifecycleIsResumed()) {
                                                    navController.navigateUp()
                                                }
                                            }

                                            // The pencil: recompute the leg between the same two
                                            // places, with the request it was filed with.
                                            is NavEvent.GoTo -> when (val screen = event.screen) {
                                                is AddTransportScreen -> navController.navigate(
                                                    PlanningTransportPageNavigation(
                                                        screen.dayId,
                                                        screen.startPlaceId,
                                                        screen.endPlaceId,
                                                    ),
                                                )

                                                else -> Unit
                                            }

                                            else -> Unit
                                        }
                                    },
                                )
                            }

                            navigation<PlanningTransportNavigation>(
                                startDestination = PlanningTransportPageNavigation::class,
                            ) {
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
                                            viewModelStoreOwner = transportEntry,
                                        ) { _ ->
                                            create(
                                                parentArgs.travelId,
                                                args.dayId,
                                                args.startPlaceId,
                                                args.endPlaceId,
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
                                                            args.endPlaceId,
                                                        ),
                                                    )
                                                }

                                                // Handled by the preview destination, which is the
                                                // one still composed when the route is confirmed.
                                                is PlanningTransportNavigationEvent
                                                    .NavigateToTransportDetail,
                                                -> Unit
                                            }
                                        }
                                    }

                                    PlanningTransportPage(
                                        viewModel = viewModel,
                                        onNavigationBackClick = {
                                            if (backStackEntry.lifecycleIsResumed()) {
                                                navController.navigateUp()
                                            }
                                        },
                                    )
                                }

                                composable<PlanningTransportRoutePreviewPageNavigation> { backStackEntry ->
                                    val previewArgs =
                                        backStackEntry.toRoute<PlanningTransportRoutePreviewPageNavigation>()
                                    val transportEntry = remember(backStackEntry) {
                                        navController.getBackStackEntry<PlanningTransportNavigation>()
                                    }
                                    val viewModel =
                                        viewModel<PlanningTransportViewModel>(viewModelStoreOwner = transportEntry)

                                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                                    val parentArgs =
                                        navController.getBackStackEntry<PlanningNavigation>()
                                            .toRoute<PlanningNavigation>()

                                    // Saving is asynchronous and answers with the id of the leg it
                                    // filed, so the navigation waits for it rather than guessing.
                                    LaunchedEffect(viewModel) {
                                        viewModel.navigationEvent.collect { event ->
                                            if (event !is PlanningTransportNavigationEvent.NavigateToTransportDetail) {
                                                return@collect
                                            }
                                            navController.navigate(
                                                TransportDetailPageNavigation(
                                                    travelId = parentArgs.travelId,
                                                    dayId = previewArgs.dayId,
                                                    stepId = event.stepId,
                                                ),
                                            ) {
                                                popUpTo(PlanningDetailPageNavigation(previewArgs.dayId)) {
                                                    inclusive = false
                                                }
                                            }
                                        }
                                    }

                                    val confirm = {
                                        viewModel.saveSelectedRoute()
                                    }

                                    when (val result = uiState.result) {
                                        null -> Unit

                                        is RouteResult.Routing -> RoutingRoutePreviewPage(
                                            routes = result.routes,
                                            selectedRouteIndex = uiState.selectedRouteIndex,
                                            onRouteChange = { viewModel.selectRoute(it) },
                                            onRouteConfirm = confirm,
                                            onNavigationBackClick = {
                                                if (backStackEntry.lifecycleIsResumed()) {
                                                    navController.navigateUp()
                                                }
                                            },
                                        )

                                        is RouteResult.Transit -> TransitJourneyPreviewPage(
                                            journeys = result.journeys,
                                            selectedJourneyIndex = uiState.selectedRouteIndex,
                                            onJourneyChange = { viewModel.selectRoute(it) },
                                            onJourneyConfirm = confirm,
                                            onNavigationBackClick = {
                                                if (backStackEntry.lifecycleIsResumed()) {
                                                    navController.navigateUp()
                                                }
                                            },
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
                                    key = "place_${travelId}_${args.dayId}",
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
                                },
                            )
                        }

                        composable<SettingsNavigation> { backStackEntry ->
                            // The settings page is only reachable from a trip, and the API key belongs
                            // to that trip: the id comes off the planning entry still on the back stack.
                            val travelId = navController.getBackStackEntry<PlanningNavigation>()
                                .toRoute<PlanningNavigation>().travelId
                            val viewModel =
                                assistedMetroViewModel<SettingsViewModel, SettingsViewModel.Factory>(
                                    key = "settings_$travelId",
                                ) { _ -> create(travelId) }

                            SettingsPage(
                                viewModel = viewModel,
                                onNavigationBackClick = {
                                    if (backStackEntry.lifecycleIsResumed()) {
                                        navController.navigateUp()
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
