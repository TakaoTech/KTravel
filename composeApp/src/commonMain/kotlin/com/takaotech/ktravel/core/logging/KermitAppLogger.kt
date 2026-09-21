package com.takaotech.ktravel.core.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

/**
 * The [AppLogger] the application actually runs on: Kermit, and nothing else.
 *
 * Thin by design. Everything that makes a line reach the diagnostics screen, the log file or the
 * telemetry backend is a `LogWriter` on the Kermit logger this wraps (see `core/AppLogging.kt`), so
 * the same configuration serves the `:gunzou-*` clients, which speak Kermit directly, and the
 * application, which speaks [AppLogger].
 *
 * @param logger The configured Kermit logger every line is written through.
 */
internal class KermitAppLogger(private val logger: Logger) : AppLogger {

    override fun withTag(tag: String): AppLogger = KermitAppLogger(logger.withTag(tag))

    override fun v(throwable: Throwable?, message: () -> String) = write(Severity.Verbose, throwable, message)

    override fun d(throwable: Throwable?, message: () -> String) = write(Severity.Debug, throwable, message)

    override fun i(throwable: Throwable?, message: () -> String) = write(Severity.Info, throwable, message)

    override fun w(throwable: Throwable?, message: () -> String) = write(Severity.Warn, throwable, message)

    override fun e(throwable: Throwable?, message: () -> String) = write(Severity.Error, throwable, message)

    private fun write(severity: Severity, throwable: Throwable?, message: () -> String) =
        logger.logBlock(severity, logger.tag, throwable, message)
}
