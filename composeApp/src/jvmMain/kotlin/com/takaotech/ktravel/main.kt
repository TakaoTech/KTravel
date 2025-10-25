package com.takaotech.ktravel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun main() = application {
    System.setProperty("compose.interop.blending", "true")

    Window(
        onCloseRequest = ::exitApplication,
        title = "ktravel",
    ) {
        App()
    }
}