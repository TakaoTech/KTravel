package com.takaotech.ktravel

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.takaotech.ktravel.core.LocalOperatingSystem
import com.takaotech.ktravel.core.platformModules
import com.takaotech.ktravel.di.appModule
import com.takaotech.ktravel.navigation.appNavGraph
import io.github.kdroidfilter.platformtools.getOperatingSystem
import kotlinx.serialization.Serializable
import org.koin.compose.KoinMultiplatformApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.ParametersDefinition
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

                    appNavGraph(navController)
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
    noinline parameters: ParametersDefinition? = null
): VM {
    val parentEntry = navGraphRoute?.let {
        navController.getBackStackEntry(it)
    } ?: this

    return koinViewModel(
        viewModelStoreOwner = parentEntry,
        parameters = parameters
    )
}
