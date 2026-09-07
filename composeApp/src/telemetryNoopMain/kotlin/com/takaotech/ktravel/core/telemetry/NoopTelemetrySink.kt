package com.takaotech.ktravel.core.telemetry

import com.takaotech.ktravel.core.logging.LogLevel

/**
 * The telemetry backend of a build without a `kotzilla.json`: none.
 *
 * This source directory is the one compiled when the project file is absent — a fresh clone, CI, a
 * contributor without an account — so the application builds and behaves identically, minus the
 * sending. See the source set switch in `composeApp/build.gradle.kts`.
 */
internal fun createTelemetrySink(): TelemetrySink = NoopTelemetrySink

private object NoopTelemetrySink : TelemetrySink {

    override fun start() = Unit

    override fun applyConsent(consent: TelemetryConsent) = Unit

    override fun identify(installationId: String) = Unit

    override fun record(level: LogLevel, tag: String, message: String, throwable: Throwable?) = Unit

    override fun forgetMe() = Unit
}
