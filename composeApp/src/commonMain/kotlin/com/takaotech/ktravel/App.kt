package com.takaotech.ktravel

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.*
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
import com.takaotech.ktravel.core.LocalOperatingSystem
import com.takaotech.ktravel.core.platformModules
import com.takaotech.ktravel.di.appModule
import com.takaotech.ktravel.presentation.place.PlaceInsertViewModel
import com.takaotech.ktravel.presentation.planning.PlanningDetailViewModel
import com.takaotech.ktravel.presentation.planning.PlanningViewModel
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportNavigationEvent
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportViewModel
import com.takaotech.ktravel.presentation.settings.SettingsViewModel
import com.takaotech.ktravel.ui.place.PlaceInsertNavigation
import com.takaotech.ktravel.ui.place.PlaceInsertPage
import com.takaotech.ktravel.ui.planning.detail.PlanningDetailPage
import com.takaotech.ktravel.ui.planning.detail.PlanningDetailPageNavigation
import com.takaotech.ktravel.ui.planning.transport.*
import com.takaotech.ktravel.ui.planning.trip.PlanningTripPage
import com.takaotech.ktravel.ui.planning.trip.PlanningTripPageNavigation
import com.takaotech.ktravel.ui.settings.SettingsNavigation
import com.takaotech.ktravel.ui.settings.SettingsPage
import io.github.kdroidfilter.platformtools.getOperatingSystem
import kotlinx.serialization.Serializable
import org.koin.compose.KoinMultiplatformApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.parametersOf
import org.koin.dsl.KoinConfiguration


@Serializable
object Intro

@Serializable
object PlanningNavigation

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class, KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    KoinMultiplatformApplication(
        config = KoinConfiguration {
            modules(appModule())
            platformModules()
        }
    ) {
        val currentOs = getOperatingSystem()

        CompositionLocalProvider(
            LocalOperatingSystem provides currentOs
        ) {
            MaterialTheme {
                val navController = rememberNavController()
                rememberCoroutineScope()
                rememberSupportingPaneScaffoldNavigator()

                NavHost(navController = navController, startDestination = Intro) {
                    composable<Intro> {
                        Column {
                            TextButton(onClick = {
                                navController.navigate(PlanningNavigation)
                            }) {
                                Text("Navigate to Planning")
                            }
                        }
                    }

                    navigation<PlanningNavigation>(startDestination = PlanningTripPageNavigation) {
                        composable<PlanningTripPageNavigation> {
                            val viewModel = it.sharedKoinViewModel2<PlanningViewModel>(navController)

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
                                                    PlanningRoutePreviewPageNavigation(
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

                            composable<PlanningRoutePreviewPageNavigation> { backStackEntry ->
                                val args = backStackEntry.toRoute<PlanningRoutePreviewPageNavigation>()
                                val viewModel =
                                    backStackEntry.sharedKoinViewModel2<PlanningTransportViewModel>(navController) {
                                        parametersOf(args.dayId, args.startPlaceId, args.endPlaceId)
                                    }
                                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                                uiState.routes?.let { routes ->
                                    PlanningRoutePreviewPage(
                                        routes = routes,
                                        selectedRouteIndex = uiState.selectedRouteIndex,
                                        onRouteConfirm = {

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
}

@Deprecated("https://github.com/InsertKoinIO/koin/pull/2293 is merged in 4.2.0-beta3, wait for 4.2.0 release")
@Composable
inline fun <reified VM : ViewModel> NavBackStackEntry.sharedKoinViewModel2(
    navController: NavController,
    navGraphRoute: Any? = this.destination.parent?.route,
    noinline parameters: ParametersDefinition? = null,
): VM {
    val navGraphRoute = navGraphRoute ?: return koinViewModel<VM>()
    val parentEntry = remember(this) {
        if (navGraphRoute is String) {
            navController.getBackStackEntry(navGraphRoute)
        } else {
            navController.getBackStackEntry(navGraphRoute)
        }
    }
    return koinViewModel(
        viewModelStoreOwner = parentEntry,
        parameters = parameters
    )
}
