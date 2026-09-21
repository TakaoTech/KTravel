package com.takaotech.ktravel

import androidx.compose.ui.window.ComposeUIViewController
import com.takaotech.ktravel.core.telemetry.KTravelTelemetry

/**
 * The application, as UIKit sees it.
 *
 * Telemetry is started here rather than inside the composition: this is the first Kotlin the iOS app
 * runs, and a crash before the first frame is one worth reporting.
 */
fun MainViewController() = ComposeUIViewController {
    KTravelTelemetry.start()
    App()
}
