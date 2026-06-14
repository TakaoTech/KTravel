package com.takaotech.ktravel

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.takaotech.ktravel.di.LocalAppGraph
import com.takaotech.ktravel.di.createAppGraph

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appGraph = createAppGraph()
    ComposeViewport {
        CompositionLocalProvider(LocalAppGraph provides appGraph) {
            App()
        }
    }
}