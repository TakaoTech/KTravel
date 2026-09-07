package com.takaotech.ktravel.core.logging

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** How many lines of the running session are kept in memory before the oldest ones are dropped. */
const val MAX_LOG_ENTRIES: Int = 1_000

/**
 * The lines of the session that is running, live.
 *
 * Only the hot tail: the log that survives a restart, and the one a bug report is built from, is the
 * file written by [LogFileSink]. This exists so the diagnostics screen updates while the user is
 * looking at it, without re-reading the file on every line.
 */
interface LogStore {

    /** The lines held right now, oldest first. */
    val entries: StateFlow<ImmutableList<LogEntry>>

    /**
     * Keeps [entry], dropping the oldest line when the buffer is full.
     *
     * @param entry The line just written.
     */
    fun record(entry: LogEntry)

    /** Forgets every line held in memory. The files on disk are not touched. */
    fun clear()
}

/**
 * A [LogStore] holding at most [MAX_LOG_ENTRIES] lines in a ring.
 *
 * The bound is what makes it safe to log from a loop: a session that runs for a day writes far more
 * than a screen can show, and holding all of it would trade a diagnostics feature for a leak.
 */
class InMemoryLogStore : LogStore {

    private val _entries = MutableStateFlow<ImmutableList<LogEntry>>(persistentListOf())

    override val entries: StateFlow<ImmutableList<LogEntry>> = _entries.asStateFlow()

    override fun record(entry: LogEntry) {
        _entries.update { held ->
            val grown = held + entry

            if (grown.size <= MAX_LOG_ENTRIES) {
                grown.toPersistentList()
            } else {
                grown.subList(grown.size - MAX_LOG_ENTRIES, grown.size).toPersistentList()
            }
        }
    }

    override fun clear() = _entries.update { persistentListOf() }
}
