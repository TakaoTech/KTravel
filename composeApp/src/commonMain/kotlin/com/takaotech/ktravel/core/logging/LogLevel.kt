package com.takaotech.ktravel.core.logging

import co.touchlab.kermit.Severity

/**
 * How loud a line is, in the application's own vocabulary.
 *
 * A type of ours rather than Kermit's `Severity`, because the point of [AppLogger] is that no caller
 * has to know which library writes the line: the moment a severity from Kermit appears in a
 * signature, every logging call site depends on it again.
 */
enum class LogLevel {
    /** Detail only useful while chasing something specific. */
    Verbose,

    /** What the application did, at the granularity a developer reads while it runs. */
    Debug,

    /** A milestone worth keeping in a report: a plan imported, a server started. */
    Info,

    /** Something recovered from, but that should not have happened. */
    Warn,

    /** Something failed, and the user probably noticed. */
    Error,

    /** A broken assumption: the code reached a state it is written to consider impossible. */
    Assert,
    ;

    /** True when this level is at least as loud as [other], which is how filters are expressed. */
    fun atLeast(other: LogLevel): Boolean = ordinal >= other.ordinal
}

/** The Kermit severity this level is written at. */
internal fun LogLevel.toSeverity(): Severity = when (this) {
    LogLevel.Verbose -> Severity.Verbose
    LogLevel.Debug -> Severity.Debug
    LogLevel.Info -> Severity.Info
    LogLevel.Warn -> Severity.Warn
    LogLevel.Error -> Severity.Error
    LogLevel.Assert -> Severity.Assert
}

/** The level a Kermit severity is recorded as. */
internal fun Severity.toLogLevel(): LogLevel = when (this) {
    Severity.Verbose -> LogLevel.Verbose
    Severity.Debug -> LogLevel.Debug
    Severity.Info -> LogLevel.Info
    Severity.Warn -> LogLevel.Warn
    Severity.Error -> LogLevel.Error
    Severity.Assert -> LogLevel.Assert
}
