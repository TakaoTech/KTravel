package com.takaotech.ktravel.core.telemetry

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.takaotech.ktravel.core.logging.LogLevel
import com.takaotech.ktravel.core.logging.toLogLevel

/** The quietest line that is worth sending: below this it is noise about a device nobody is debugging. */
private val MIN_REMOTE_LEVEL = LogLevel.Info

/**
 * The Kermit writer that forwards to remote telemetry, and the only thing that decides whether a
 * line may leave the device.
 *
 * The consent is read on every line rather than captured once: it changes while the process runs —
 * the user answers in the introduction, revokes from the settings, or a year passes and it expires
 * — and a writer that had captured it would keep sending after the revocation.
 *
 * @param sink The backend the line is handed to.
 * @param consent The decision as it stands right now.
 */
class TelemetryLogWriter(private val sink: TelemetrySink, private val consent: () -> TelemetryConsent) :
    LogWriter() {

    override fun isLoggable(tag: String, severity: Severity): Boolean =
        consent() == TelemetryConsent.Granted && severity.toLogLevel().atLeast(MIN_REMOTE_LEVEL)

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        // Kermit calls isLoggable before this, but a writer that sends data off the device does not
        // rely on being asked first.
        if (!isLoggable(tag, severity)) return

        sink.record(severity.toLogLevel(), tag, message, throwable)
    }
}
