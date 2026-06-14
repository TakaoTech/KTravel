package com.takaotech.ktravel

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.takaotech.ktravel.di.LocalAppGraph
import com.takaotech.ktravel.di.createAppGraph

fun MainViewController() = run {
    val appGraph = createAppGraph()
    ComposeUIViewController {
        CompositionLocalProvider(LocalAppGraph provides appGraph) {
            App()
        }
    }
}