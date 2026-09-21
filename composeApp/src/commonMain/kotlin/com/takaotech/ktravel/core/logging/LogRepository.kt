package com.takaotech.ktravel.core.logging

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json

/**
 * What the diagnostics screen and the bug report read.
 *
 * Two sources, and both are needed: the files hold the days being kept, including the session that
 * crashed, while [LogStore] holds the lines of the running session, which reach the screen before
 * the sink has had a chance to write them. Merged on [LogEntry.id], so a line that is in both
 * appears once.
 *
 * The split between [persistedEntries] and [allEntries] is what keeps the screen cheap: the files
 * are read when the day being shown changes, not on every line that is logged while the user looks
 * at it — those arrive through [LogStore] and are merged in the presenter.
 *
 * @param store The log files on disk.
 * @param logStore The lines of the running session.
 * @param timeZone The zone the day boundary is drawn in, the device's own.
 */
class LogRepository(
    private val store: LogFileStore,
    private val logStore: LogStore,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {

    private val json = Json { ignoreUnknownKeys = true }

    /** The days that can be read, oldest first: the files, plus the day the session is writing. */
    suspend fun days(): ImmutableList<LocalDate> = withContext(Dispatchers.IO) {
        val live = logStore.entries.value.map { it.dayIn(timeZone) }

        (store.days() + live).distinct().sorted().toImmutableList()
    }

    /**
     * The lines already written to disk, oldest first.
     *
     * @param day The day to read, or null for every day being kept.
     */
    suspend fun persistedEntries(day: LocalDate? = null): ImmutableList<LogEntry> = withContext(Dispatchers.IO) {
        val days = day?.let(::listOf) ?: store.days()

        merge(days.flatMap { store.readLines(it) }.mapNotNull(::decode))
    }

    /** Every kept line, from the files and from the running session, oldest first. */
    suspend fun allEntries(): ImmutableList<LogEntry> = merge(persistedEntries() + logStore.entries.value)

    /** Every kept line rendered as text, which is what is saved to a file and quoted in an issue. */
    suspend fun exportText(): String = allEntries().joinToString("\n") { it.toDisplayLine() }

    /** Deletes every log file and forgets the lines held in memory. */
    suspend fun clear() {
        withContext(Dispatchers.IO) { store.days().forEach(store::delete) }
        logStore.clear()
    }

    /** The day [entry] belongs to, in the zone the files are named after. */
    fun dayOf(entry: LogEntry): LocalDate = entry.dayIn(timeZone)

    /**
     * A line that cannot be decoded is dropped rather than failing the read: a file truncated by a
     * process that died mid-write must not take the whole diagnostics screen down with it.
     */
    private fun decode(line: String): LogEntry? = runCatching { json.decodeFromString<LogEntry>(line) }.getOrNull()

    private fun merge(entries: List<LogEntry>): ImmutableList<LogEntry> =
        entries.distinctBy { it.id }.sortedBy { it.timestamp }.toImmutableList()

    private fun LogEntry.dayIn(zone: TimeZone): LocalDate = timestamp.toLocalDateTime(zone).date
}
