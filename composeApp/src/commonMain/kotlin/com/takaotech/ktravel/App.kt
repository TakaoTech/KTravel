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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.takaotech.ktravel.core.LocalOperatingSystem
import com.takaotech.ktravel.di.PlanningScope
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
import io.github.kdroidfilter.platformtools.getOperatingSystem
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.Koin
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.parametersOf


@Serializable
data class PlanningNavigation(val travelId: String)

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalComposeUiApi::class,
    KoinExperimentalAPI::class
)
@Composable
@Preview
fun App() {
    val currentOs = getOperatingSystem()

    CompositionLocalProvider(
        LocalOperatingSystem provides currentOs
    ) {
        MaterialTheme {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = TravelSelectionPage) {
                composable<TravelSelectionPage> {
                    TravelSelectionPage(
                        onNewTravelClick = {
                            navController.navigate(TravelCreationPage)
                        }
                    )
                }

                composable<TravelCreationPage> {
                    TravelCreationPage(
                        onBackClick = {
                            navController.navigateUp()
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
                        val scope = getKoin().getOrCreateScope<PlanningScope>(args.travelId)

                        val viewModel = backStackEntry.sharedKoinViewModel2<PlanningViewModel>(
                            navController = navController,
                            scope = scope
                        )
                        val coroutine = rememberCoroutineScope()

                        val launcher =
                            rememberFileSaverLauncher(FileKitDialogSettings.createDefault()) { file ->
                                // Write your data to the file

                            }

                        NavigationBackHandler(
                            state = rememberNavigationEventState(NavigationEventInfo.None),
                            isBackEnabled = true, // You can toggle this dynamically
                            onBackCompleted = {
                                scope.close()
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
                        val koin: Koin = getKoin()
                        val scope = remember(parentArgs.travelId) {
                            koin.getOrCreateScope<PlanningScope>(
                                parentArgs.travelId
                            )
                        }
                        val viewModel = koinViewModel<PlanningDetailViewModel>(scope = scope) {
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
                            val parentArgs = navController.getBackStackEntry<PlanningNavigation>()
                                .toRoute<PlanningNavigation>()
                            val koin: Koin = getKoin()
                            val scope = remember(parentArgs.travelId) {
                                koin.getOrCreateScope<PlanningScope>(parentArgs.travelId)
                            }
                            val viewModel =
                                backStackEntry.sharedKoinViewModel2<PlanningTransportViewModel>(
                                    navController = navController,
                                    scope = scope
                                ) {
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
                            val args =
                                backStackEntry.toRoute<PlanningTransportRoutePreviewPageNavigation>()
                            val parentArgs =
                                navController.getBackStackEntry<PlanningNavigation>()
                                    .toRoute<PlanningNavigation>()
                            val koin: Koin = getKoin()
                            val scope = remember(parentArgs.travelId) {
                                koin.getOrCreateScope<PlanningScope>(
                                    parentArgs.travelId
                                )
                            }
                            val viewModel =
                                backStackEntry.sharedKoinViewModel2<PlanningTransportViewModel>(
                                    navController = navController,
                                    scope = scope
                                ) {
                                    parametersOf(args.dayId, args.startPlaceId, args.endPlaceId)
                                }
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

                    val viewModel = koinViewModel<PlaceInsertViewModel> {
                        //TODO Fix this param inject, travelId is injected in dayId when dayId is null
                        parametersOf(
                            navController.getBackStackEntry<PlanningNavigation>()
                                .toRoute<PlanningNavigation>().travelId,
                            args.dayId
                        )
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

                composable<SettingsNavigation> {
                    val viewModel = koinViewModel<SettingsViewModel>()

                    SettingsPage(
                        viewModel = viewModel,
                        onNavigationBackClick = {
                            navController.navigateUp()
                        }
                    )
                }

                //                    composable<PlanningPage> {
                //                        LaunchedEffect(currentValue, directive) {
                //                            when (directive.maxHorizontalPartitions) {
                //                                1 -> {
                //                                    // Schermo piccolo: scegli quale pannello mantenere
                //                                    // Ad esempio, mantieni sempre il mainPane
                //                                    navigator
                //
                //                                    navigator.navigateBack()
                //                                }
                //                                2 -> {
                //                                    // Schermo medio: mostra due pannelli
                //                                    // Personalizza quale combinazione mostrare
                //                                }
                //                                else -> {
                //
                //                                }
                //                            }
                //                        }


                //                        PanelHorizontalDivided(
                //                            modifier = Modifier.fillMaxSize(),
                //                            scaffoldNavigator = navigator,
                //                            mainPane = {
                //                                AnimatedPane {
                //                                    PlanningPage(
                //                                        modifier = Modifier.fillMaxSize(),
                //                                        viewModel = koinViewModel()
                //                                    )
                //
                //                                    Button(
                //                                        onClick = {
                //                                            coroutine.launch {
                //                                                navigator.navigateTo(ThreePaneScaffoldRole.Secondary)
                //                                            }
                //                                        }
                //                                    ){
                //                                        Text("Secondary")
                //                                    }
                //
                //                                }
                //                            },
                //                            supportPane = {
                //                                AnimatedPane {
                //                                    Text("text")
                //                                }
                //                            },
                //                            extraPane = {
                //                                AnimatedPane {
                //                                    Scaffold(
                //                                        topBar = {
                //                                            IconButton(
                //                                                modifier = Modifier,
                //                                                onClick = {
                //                                                    coroutine.launch {
                //                                                        navigator.navigateBack()
                //                                                    }
                //                                                }
                //                                            ) {
                //                                                Icon(
                //                                                    painter = painterResource(Res.drawable.arrow_back),
                //                                                    contentDescription = null
                //                                                )
                //                                            }
                //                                        }
                //                                    ) {
                ////                                    MapForge(
                ////                                        modifier = Modifier
                ////                                            .fillMaxSize()
                ////                                            .padding(it),
                ////                                        showFps = true
                ////                                    )
                //                                    }
                //                                }
                //                            }
                //                        )
                //                    }
            }
        }
    }
}

@Deprecated("https://github.com/InsertKoinIO/koin/pull/2293 is merged in 4.2.0-beta3, wait for 4.2.0 release")
@Composable
inline fun <reified VM : ViewModel> NavBackStackEntry.sharedKoinViewModel2(
    navController: NavController,
    navGraphRoute: Any? = this.destination.parent?.route,
    scope: org.koin.core.scope.Scope? = null,
    noinline parameters: ParametersDefinition? = null,
): VM {
    val navGraphRoute = navGraphRoute ?: return if (scope != null) {
        koinViewModel<VM>(scope = scope, parameters = parameters)
    } else {
        koinViewModel<VM>(parameters = parameters)
    }
    val parentEntry = remember(this) {
        if (navGraphRoute is String) {
            navController.getBackStackEntry(navGraphRoute)
        } else {
            navController.getBackStackEntry(navGraphRoute)
        }
    }
    return if (scope != null) {
        koinViewModel(
            viewModelStoreOwner = parentEntry,
            scope = scope,
            parameters = parameters
        )
    } else {
        koinViewModel(
            viewModelStoreOwner = parentEntry,
            parameters = parameters
        )
    }
}
