package com.takaotech.ktravel.gunzou.server

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

/** The tag every line of this server carries, whoever's logger it is written through. */
private const val NAVIGATOR_TAG = "gunzou-navigator"

/**
 * The writer every target logs through. On the JVM it forwards to SLF4J, so `logback.xml` keeps
 * owning the output format; Android and iOS use the writers Kermit ships for Logcat and NSLog.
 *
 * Public because a host application building its own [Logger] needs the same per target answer, and
 * writing it twice is how the two copies end up disagreeing.
 */
expect fun appLogWriter(): LogWriter

/**
 * The logger a host installed through [installLogging], if it did.
 *
 * Not thread safe on purpose: it is written once, while the Ktor module is being built, before the
 * engine accepts a request.
 */
// TODO Analyze remove this variable
private var installedLog: Logger? = null

/**
 * What the server writes through: the host's logger when there is one, and its own otherwise.
 *
 * Ktor's own [io.ktor.util.logging.Logger] is a separate channel: it is SLF4J on the JVM and
 * `KtorSimpleLogger` elsewhere, and is left untouched.
 */
val appLog: Logger
    get() = installedLog ?: Logger.withTag(NAVIGATOR_TAG)

/**
 * Decides where this server's log goes. Called once per application, by [module].
 *
 * Two situations, and they are not the same one:
 *
 * - **A deployment**, where this server is the process. Nobody else has an opinion, so [logger] is
 *   null and the Kermit singleton is configured here — writer and minimum severity both.
 * - **Embedded in an application**, where it is not. The host owns the process and builds its own
 *   [Logger] through its dependency graph; it passes it in, and this configures no global anything.
 *   That is what keeps the server from undoing the host's configuration when it starts — which it
 *   would, silently, by lowering the minimum severity back to [Severity.Info] and taking every HTTP
 *   line down with it.
 *
 * @param logger The host's logger. Re-tagged, so its lines stay recognisable as the server's while
 *   still going to the writer the host chose.
 * @param minSeverity The lowest severity kept, when there is no host to decide it. Below
 *   [Severity.Debug] nothing the HTTP clients write survives.
 */
fun installLogging(logger: Logger? = null, minSeverity: Severity = Severity.Info) {
    if (logger != null) {
        installedLog = logger.withTag(NAVIGATOR_TAG)
        return
    }

    Logger.setLogWriters(appLogWriter())
    Logger.setMinSeverity(minSeverity)
    installedLog = Logger.withTag(NAVIGATOR_TAG)
}
