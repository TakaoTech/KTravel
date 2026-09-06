package com.takaotech.ktravel

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composer
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit
import org.maplibre.compose.desktop.MapLibre
import org.maplibre.compose.desktop.ProvideMapHost
import org.maplibre.compose.desktop.rememberAwtComposeMapHost

@Suppress("UndocumentedPublicFunction")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun main() {
    MapLibre.configure(applicationId = "com.takaotech.ktravel")

    application {
        System.setProperty("compose.interop.blending", "true")
        System.setProperty("apple.awt.application.appearance", "system")
        FileKit.init(appId = "ktravel")
        Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.SourceInformation)

        Window(
            onCloseRequest = ::exitApplication,
            title = "ktravel",
        ) {
            ProvideMapHost(host = rememberAwtComposeMapHost(window)) {
                App(onRootPop = ::exitApplication)
            }
        }
    }
}
