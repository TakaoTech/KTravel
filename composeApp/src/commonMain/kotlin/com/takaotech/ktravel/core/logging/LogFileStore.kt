package com.takaotech.ktravel.core.logging

import kotlinx.datetime.LocalDate
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

/** Prefix and suffix of a day's log file, which is also how a file is recognised as one. */
private const val FILE_PREFIX = "ktravel-"

private const val FILE_SUFFIX = ".log"

/**
 * The log files on disk, one per day, and the only place that knows their names.
 *
 * kotlinx-io rather than FileKit because a log is appended to: FileKit's `write` replaces the file,
 * which for a log means reading the whole day back into memory on every line.
 *
 * @param directory Where the files live, created on the first write.
 */
class LogFileStore(private val directory: Path) {

    /**
     * Appends [lines] to the file of [day], creating it when it is the first line of the day.
     *
     * @param day The day the lines belong to.
     * @param lines One serialized [LogEntry] each, without their terminating newline.
     */
    fun append(day: LocalDate, lines: List<String>) {
        if (lines.isEmpty()) return

        SystemFileSystem.createDirectories(directory)
        SystemFileSystem.sink(fileOf(day), append = true).buffered().use { sink ->
            lines.forEach { line ->
                sink.writeString(line)
                sink.writeString("\n")
            }
        }
    }

    /**
     * The lines written on [day], or nothing when that day has no file.
     *
     * @param day The day to read.
     */
    fun readLines(day: LocalDate): List<String> {
        val file = fileOf(day)
        if (SystemFileSystem.metadataOrNull(file) == null) return emptyList()

        return SystemFileSystem.source(file).buffered()
            .use { it.readString() }
            .lineSequence()
            .filter { it.isNotBlank() }
            .toList()
    }

    /** The days that have a file, oldest first. */
    fun days(): List<LocalDate> {
        if (SystemFileSystem.metadataOrNull(directory) == null) return emptyList()

        return SystemFileSystem.list(directory)
            .mapNotNull { it.name.toLogDay() }
            .sorted()
    }

    /**
     * Deletes the file of [day], if it is still there.
     *
     * @param day The day to forget.
     */
    fun delete(day: LocalDate) = SystemFileSystem.delete(fileOf(day), mustExist = false)

    private fun fileOf(day: LocalDate) = Path(directory, "$FILE_PREFIX$day$FILE_SUFFIX")
}

/** The day a log file name stands for, or null when the name is not one of ours. */
private fun String.toLogDay(): LocalDate? {
    if (!startsWith(FILE_PREFIX) || !endsWith(FILE_SUFFIX)) return null

    return runCatching {
        LocalDate.parse(substring(FILE_PREFIX.length, length - FILE_SUFFIX.length))
    }.getOrNull()
}
