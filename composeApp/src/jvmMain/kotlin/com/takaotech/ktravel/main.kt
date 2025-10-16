package com.takaotech.ktravel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ktravel",
    ) {
        val scaffoldNavigator = rememberSupportingPaneScaffoldNavigator()
        val scope = rememberCoroutineScope()



        SupportingPaneScaffold(
            mainPane = {
                Text("testo")
            },
            directive = scaffoldNavigator.scaffoldDirective,
            value = scaffoldNavigator.scaffoldValue,
            supportingPane = {
                MapForge(
                    modifier = Modifier.fillMaxSize(),
                    showFps = true
                )
            },
        )

//        App()
    }
}