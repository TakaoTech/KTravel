package com.takaotech.ktravel

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.takaotech.ktravel.core.LocalOperatingSystem
import com.takaotech.ktravel.core.platformModules
import com.takaotech.ktravel.di.appModule
import com.takaotech.ktravel.presentation.planner.PlanningViewModel
import com.takaotech.ktravel.ui.planner.PlanningDetailPage
import com.takaotech.ktravel.ui.planner.PlanningPage
import io.github.kdroidfilter.platformtools.getOperatingSystem
import kotlinx.serialization.Serializable
import org.koin.compose.KoinMultiplatformApplication
import org.koin.compose.viewmodel.sharedKoinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
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
                val coroutine = rememberCoroutineScope()
                val navigator = rememberSupportingPaneScaffoldNavigator()

                NavHost(navController = navController, startDestination = PlanningPage) {
                    composable<Intro> {
                        TextButton(onClick = {
                            navController.navigate(PlanningPage)
                        }) {
                            Text("Navigate to Planning")
                        }
                    }

                    navigation<PlanningNavigation>(startDestination = PlanningPage) {
                        composable<PlanningPage> {
                            val viewModel = it.sharedKoinViewModel<PlanningViewModel>(navController)

                            PlanningPage(
                                viewModel = viewModel,
                                onDateClicked = {
                                    navController.navigate(PlanningDetailPage(it))
                                }
                            )
                        }

                        composable<PlanningDetailPage> {
                            val viewModel = it.sharedKoinViewModel<PlanningViewModel>(navController).let {

                            }


//                            PlanningDetailPage()
                        }
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