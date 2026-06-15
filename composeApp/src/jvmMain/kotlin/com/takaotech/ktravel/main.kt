package com.takaotech.ktravel

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composer
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit

@Suppress("UndocumentedPublicFunction")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun main() {
    application {
        System.setProperty("compose.interop.blending", "true")
        FileKit.init(appId = "ktravel")
        Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.SourceInformation)

        Window(
            onCloseRequest = ::exitApplication,
            title = "ktravel",
        ) {
            App()
        }
    }
}