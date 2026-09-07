package com.takaotech.ktravel.core.logging

/** The single letter a level is abbreviated to in the exported text. */
private fun LogLevel.symbol(): String = when (this) {
    LogLevel.Verbose -> "V"
    LogLevel.Debug -> "D"
    LogLevel.Info -> "I"
    LogLevel.Warn -> "W"
    LogLevel.Error -> "E"
    LogLevel.Assert -> "A"
}

/**
 * The line as it appears in an exported log file or in a bug report.
 *
 * One entry per line, with the stack trace indented under it, so the result reads the way a log is
 * expected to read while staying grep-able.
 */
fun LogEntry.toDisplayLine(): String = buildString {
    append(timestamp)
    append("  ")
    append(level.symbol())
    append("  ")
    append(tag)
    if (travelId != null) {
        append(" [travel:")
        append(travelId)
        append(']')
    }
    append("  ")
    append(message)
    if (stackTrace != null) {
        append('\n')
        append(stackTrace.trimEnd().prependIndent("    "))
    }
}
