package com.takaotech.ktravel.core.logging.slf4j

import co.touchlab.kermit.Severity
import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.LegacyAbstractLogger
import org.slf4j.helpers.MessageFormatter

/**
 * One SLF4J logger, writing into Kermit.
 *
 * Extends `LegacyAbstractLogger` so that the forty-odd overloads SLF4J declares — varargs, one and
 * two argument forms, markers — arrive here already normalised into a single call.
 *
 * The SLF4J logger name becomes the Kermit tag, which is what keeps a Ktor line recognisable as
 * Ktor's on the diagnostics screen.
 *
 * @param loggerName The name SLF4J was asked for, normally a fully qualified class name.
 */
internal class KermitSlf4jLogger(loggerName: String) : LegacyAbstractLogger() {

    init {
        name = loggerName
    }

    /** Not used: caller detection is a logback feature and this binding does not offer it. */
    override fun getFullyQualifiedCallerName(): String? = null

    override fun isTraceEnabled(): Boolean = isEnabled(Severity.Verbose)

    override fun isDebugEnabled(): Boolean = isEnabled(Severity.Debug)

    override fun isInfoEnabled(): Boolean = isEnabled(Severity.Info)

    override fun isWarnEnabled(): Boolean = isEnabled(Severity.Warn)

    override fun isErrorEnabled(): Boolean = isEnabled(Severity.Error)

    override fun handleNormalizedLoggingCall(
        level: Level?,
        marker: Marker?,
        messagePattern: String?,
        arguments: Array<out Any?>?,
        throwable: Throwable?,
    ) {
        val message = format(messagePattern, arguments)

        val severity = when (level) {
            Level.TRACE -> Severity.Verbose

            Level.DEBUG -> Severity.Debug

            Level.WARN -> Severity.Warn

            Level.ERROR -> Severity.Error

            // SLF4J's INFO, and anything a future version adds, which is better logged than dropped.
            else -> Severity.Info
        }

        KermitSlf4jBridge.guarded {
            val logger = KermitSlf4jBridge.target.withTag(name)

            logger.log(severity, logger.tag, throwable, message)
        }
    }

    /** The `{}` placeholders SLF4J leaves for the backend to fill in. */
    private fun format(messagePattern: String?, arguments: Array<out Any?>?): String {
        val pattern = messagePattern ?: return ""

        return if (arguments.isNullOrEmpty()) {
            pattern
        } else {
            MessageFormatter.basicArrayFormat(pattern, arguments)
        }
    }

    private fun isEnabled(severity: Severity): Boolean = severity >= KermitSlf4jBridge.target.config.minSeverity
}
