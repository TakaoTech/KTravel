package com.takaotech.ktravel.gunzou.server

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import org.slf4j.LoggerFactory

actual fun appLogWriter(): LogWriter = Slf4jLogWriter()

/**
 * Routes Kermit into SLF4J so that on the JVM every log line, both the application's and Ktor's own,
 * goes through the same backend and obeys `logback.xml`. Kermit's own `platformLogWriter()` would
 * write straight to stdout and bypass it.
 */
internal class Slf4jLogWriter : LogWriter() {

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val logger = LoggerFactory.getLogger(tag.ifEmpty { DEFAULT_TAG })
        when (severity) {
            Severity.Verbose -> logger.trace(message, throwable)

            Severity.Debug -> logger.debug(message, throwable)

            Severity.Info -> logger.info(message, throwable)

            Severity.Warn -> logger.warn(message, throwable)

            // SLF4J has no level above error, so Assert shares it.
            Severity.Error, Severity.Assert -> logger.error(message, throwable)
        }
    }

    private companion object {
        const val DEFAULT_TAG = "gunzou-navigator"
    }
}
