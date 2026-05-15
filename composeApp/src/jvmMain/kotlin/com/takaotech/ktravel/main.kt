package com.takaotech.ktravel

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.takaotech.ktravel.di.startKTravelKoin
import io.github.vinceglb.filekit.FileKit

@Suppress("UndocumentedPublicFunction")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun main() {
    startKTravelKoin {
        printLogger()
    }

    application {
        System.setProperty("compose.interop.blending", "true")
        FileKit.init(appId = "ktravel")

        Window(
            onCloseRequest = ::exitApplication,
            title = "ktravel",
        ) {
            App()
        }
    }
}