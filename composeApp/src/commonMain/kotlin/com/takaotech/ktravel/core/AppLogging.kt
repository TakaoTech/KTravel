package com.takaotech.ktravel.core

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter

/** The tag the application's own lines carry. Its clients and the embedded server re-tag their own. */
private const val APP_TAG = "KTravel"

/**
 * Keeps `Severity.Debug`, which is what the HTTP clients write their request and response dumps at.
 *
 * Lowering this to [Severity.Info] is how a build stops logging them, and there is a reason to want
 * that: the dumps contain the URLs and bodies of routing calls, HERE API keys included.
 */
private val DEFAULT_MIN_SEVERITY = Severity.Debug

/**
 * The application's logger, built once and handed out by the dependency graph.
 *
 * Everything logs through the instance this returns — the application itself through `AppLogger`,
 * both HTTP clients and the embedded navigator through Kermit directly, and every library that
 * speaks SLF4J through the binding in `core/logging/slf4j` — so where output lands and how much of
 * it survives are decided here and passed down, rather than read from the Kermit singleton by
 * whoever happens to want a log line.
 *
 * The platform writer is Kermit's own: Logcat on Android, NSLog on iOS, the console on the desktop.
 * It is deliberately *not* `:gunzou-server`'s `appLogWriter()`, which on the JVM forwards to SLF4J:
 * with the SLF4J binding of this application pointing back at this logger, that would be a loop.
 * The standalone `:gunzou-server-app` keeps that writer, and its logback configuration with it.
 *
 * @param extraWriters The writers that make a line reachable: the diagnostics buffer, the file on
 *   disk, and telemetry when the user has consented.
 * @param minSeverity The lowest severity kept.
 */
fun createAppLogger(
    extraWriters: List<LogWriter> = emptyList(),
    minSeverity: Severity = DEFAULT_MIN_SEVERITY,
): Logger = Logger(
    config = loggerConfigInit(platformLogWriter(), *extraWriters.toTypedArray(), minSeverity = minSeverity),
    tag = APP_TAG,
)
