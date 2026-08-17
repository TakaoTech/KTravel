package com.takaotech.ktravel.core

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import com.takaotech.ktravel.appLogWriter

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
 * Everything logs through the instance this returns — the application itself, both HTTP clients and
 * the embedded navigator, each re-tagging it as its own — so where output lands and how much of it
 * survives are decided here and passed down, rather than read from the Kermit singleton by whoever
 * happens to want a log line. That is what makes the decision reviewable: there is one call site,
 * and a component that logs had to be given a logger to do it.
 *
 * The writer comes from `:gunzou-navigator` because that is where the per target one already lives:
 * Logcat on Android, NSLog on iOS, SLF4J and therefore `logback.xml` on the desktop.
 *
 * @param minSeverity The lowest severity kept.
 */
fun createAppLogger(minSeverity: Severity = DEFAULT_MIN_SEVERITY): Logger = Logger(
    config = loggerConfigInit(appLogWriter(), minSeverity = minSeverity),
    tag = APP_TAG,
)
