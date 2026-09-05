package com.takaotech.ktravel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.takaotech.ktravel.core.KTravelPlatform
import com.takaotech.ktravel.di.createAppGraph
import com.takaotech.ktravel.navigation.KTravelNavigation
import com.takaotech.ktravel.ui.theme.KTravelTheme
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

/**
 * The root of the application: the composition roots, and nothing else.
 *
 * Everything a destination needs is installed here once — the dependency graph, the platform
 * wrapper, the theme and Circuit's composition locals — and the back stack itself lives in
 * [KTravelNavigation].
 *
 * @param onRootPop What leaving the first screen means. Only the platform entry point knows: the
 *   activity finishes on Android, the window closes on desktop, and nothing happens on iOS, where
 *   an application does not exit itself.
 */
@Suppress("ModifierRequired")
@Composable
fun App(onRootPop: () -> Unit = {}) {
    val appGraph = remember { createAppGraph() }

    KTravelPlatform {
        KTravelTheme {
            CompositionLocalProvider(LocalMetroViewModelFactory provides appGraph.metroViewModelFactory) {
                CircuitCompositionLocals(appGraph.circuit) {
                    KTravelNavigation(appGraph = appGraph, onRootPop = onRootPop)
                }
            }
        }
    }
}
