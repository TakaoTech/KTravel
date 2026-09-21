package com.takaotech.ktravel.core.telemetry

import com.takaotech.ktravel.core.telemetry.KTravelTelemetry.sink

/**
 * The one telemetry backend of the process.
 *
 * An object and not a graph binding because the platform entry points have to reach it before the
 * dependency graph exists: crash reporting is worth having during startup, which is exactly when a
 * startup crash happens. The graph binds [sink] as well, so everything downstream still receives it
 * by injection rather than reaching for this.
 */
object KTravelTelemetry {

    /** The backend chosen at build time: Kotzilla when the project is configured, a no-op otherwise. */
    val sink: TelemetrySink = createTelemetrySink()

    /** Starts the backend. Called once per process, from the platform entry point. */
    fun start() = sink.start()
}
