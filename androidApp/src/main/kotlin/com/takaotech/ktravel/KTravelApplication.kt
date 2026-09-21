package com.takaotech.ktravel

import android.app.Application
import com.takaotech.ktravel.core.telemetry.KTravelTelemetry

/**
 * The Android application object.
 *
 * Its only job is starting telemetry as early as the platform allows, so a crash on the way to the
 * first activity is still reported. A no-op in a build without a Kotzilla project file.
 */
class KTravelApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KTravelTelemetry.start()
    }
}
