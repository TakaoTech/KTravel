package com.takaotech.ktravel

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

/**
 * The writer every target logs through. On the JVM it forwards to SLF4J, so `logback.xml` keeps
 * owning the output format; Android and iOS use the writers Kermit ships for Logcat and NSLog.
 */
internal expect fun appLogWriter(): LogWriter

/**
 * Application wide logger. Ktor's own [io.ktor.util.logging.Logger] is a separate channel: it is
 * SLF4J on the JVM and `KtorSimpleLogger` elsewhere, and is left untouched.
 */
val appLog: Logger
    get() = Logger.withTag("gunzo-navigator")

/**
 * Points the Kermit singleton at [appLogWriter]. Called once per application by [module].
 */
fun installLogging(minSeverity: Severity = Severity.Info) {
    Logger.setLogWriters(appLogWriter())
    Logger.setMinSeverity(minSeverity)
}
