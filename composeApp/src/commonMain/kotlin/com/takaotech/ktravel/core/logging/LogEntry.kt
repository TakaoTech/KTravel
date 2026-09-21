package com.takaotech.ktravel.core.logging

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * One line of log, as it is held in memory, written to disk and shown on the diagnostics screen.
 *
 * Serializable because the file on disk is one entry per line in JSON: a stack trace stays on a
 * single line, and what is read back is exactly what was written, which a plain text format cannot
 * promise.
 *
 * @property id Identifies the line across the file and the in-memory buffer, so a report that reads
 *   both does not show it twice.
 * @property timestamp When the line was written.
 * @property level How loud the line is.
 * @property tag Who wrote it: the application, one of the HTTP clients, the embedded navigator, or
 *   the name of a library's SLF4J logger.
 * @property message The line itself.
 * @property stackTrace The failure that came with the line, already rendered, or null.
 * @property travelId The trip that was open when the line was written, or null outside a trip.
 */
@Serializable
data class LogEntry(
    val id: String,
    val timestamp: Instant,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val stackTrace: String? = null,
    val travelId: String? = null,
)
