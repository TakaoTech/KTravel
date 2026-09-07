package com.takaotech.ktravel.core.logging

import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * Writes the log to disk, one file per day, and deletes the days that are past their keeping.
 *
 * Why a file at all: the log worth reading in a bug report is the one from before the crash, and the
 * in-memory buffer ([LogStore]) dies with the process that produced it.
 *
 * Why a channel: [write] is called from whatever thread happened to log, including the main one
 * during composition. It only enqueues; the file is touched by [run], on the IO dispatcher.
 *
 * @param store The files themselves.
 * @param retentionDays How many days to keep, read on every rollover so a change of the setting
 *   takes effect without a restart.
 * @param clock Where "today" comes from.
 * @param timeZone The zone the day boundary is drawn in, the device's own.
 */
class LogFileSink(
    private val store: LogFileStore,
    private val retentionDays: () -> Int,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {

    private val queue = Channel<LogEntry>(Channel.UNLIMITED)

    private val json = Json { encodeDefaults = false }

    private var lastWrittenDay: LocalDate? = null

    /**
     * Enqueues [entry]. Never blocks and never throws: a log line must not be able to break the
     * call that wrote it.
     *
     * @param entry The line just written.
     */
    fun write(entry: LogEntry) {
        queue.trySend(entry)
    }

    /**
     * Consumes the queue until the scope running it is cancelled.
     *
     * Started once, by the dependency graph. Lines that arrive together are appended together: a
     * burst of HTTP logging opens the file once instead of once per line.
     */
    suspend fun run() {
        purgeExpired()

        for (first in queue) {
            val batch = mutableListOf(first)
            while (true) {
                batch += queue.tryReceive().getOrNull() ?: break
            }

            appendBatch(batch)
        }
    }

    /** Deletes the files that are older than the days being kept. */
    fun purgeExpired() {
        val today = clock.now().toLocalDateTime(timeZone).date

        LogRetention.expired(store.days(), today, retentionDays()).forEach(store::delete)
    }

    private fun appendBatch(batch: List<LogEntry>) {
        batch.groupBy { it.timestamp.toLocalDateTime(timeZone).date }
            .forEach { (day, entries) ->
                store.append(day, entries.map { json.encodeToString(it) })

                // A new day means the oldest kept one has just fallen out of the window.
                if (lastWrittenDay != null && lastWrittenDay != day) purgeExpired()
                lastWrittenDay = day
            }
    }
}
