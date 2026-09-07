@file:OptIn(ExperimentalUuidApi::class)

package com.takaotech.ktravel.core.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The Kermit writer that turns a log line into a [LogEntry] the application can show and keep.
 *
 * Installed on the one Kermit logger of the process, which is also the logger the `:gunzou-*`
 * clients and the embedded navigator write through and the one the SLF4J binding forwards to. That
 * is what makes the diagnostics screen show everything rather than only what the app itself logged.
 *
 * @param store Where the running session's lines are held for the screen.
 * @param sink Where they are written so they outlive the process.
 * @param scope The trip that is open, stamped on every line.
 * @param clock When the line happened.
 * @param newId Identity of a line, replaceable so a test can make it predictable.
 */
class LogBufferWriter(
    private val store: LogStore,
    private val sink: LogFileSink,
    private val scope: LogScope,
    private val clock: Clock = Clock.System,
    private val newId: () -> String = { Uuid.random().toString() },
) : LogWriter() {

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val entry = LogEntry(
            id = newId(),
            timestamp = clock.now(),
            level = severity.toLogLevel(),
            tag = tag,
            message = message,
            stackTrace = throwable?.stackTraceToString(),
            travelId = scope.travelId,
        )

        store.record(entry)
        sink.write(entry)
    }
}
