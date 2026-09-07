package com.takaotech.ktravel.core.logging

/**
 * What the application logs through, and the only logging type its own code names.
 *
 * Behind it there is Kermit, a local buffer that feeds the diagnostics screen, a file on disk and —
 * when the user has consented — remote telemetry. None of that is visible here on purpose: a screen,
 * a view model or a repository asks for an [AppLogger] and writes a line, and where that line ends
 * up is decided once, in the dependency graph.
 *
 * The message is a lambda so that building it costs nothing when the level is filtered out.
 */
interface AppLogger {

    /**
     * The same logger, writing under another tag.
     *
     * @param tag What the lines it writes are labelled with.
     */
    fun withTag(tag: String): AppLogger

    /**
     * Writes at [LogLevel.Verbose].
     *
     * @param throwable The failure to record with the line, when there is one.
     * @param message The line, built only if the level survives the filter.
     */
    fun v(throwable: Throwable? = null, message: () -> String)

    /**
     * Writes at [LogLevel.Debug].
     *
     * @param throwable The failure to record with the line, when there is one.
     * @param message The line, built only if the level survives the filter.
     */
    fun d(throwable: Throwable? = null, message: () -> String)

    /**
     * Writes at [LogLevel.Info].
     *
     * @param throwable The failure to record with the line, when there is one.
     * @param message The line, built only if the level survives the filter.
     */
    fun i(throwable: Throwable? = null, message: () -> String)

    /**
     * Writes at [LogLevel.Warn].
     *
     * @param throwable The failure to record with the line, when there is one.
     * @param message The line, built only if the level survives the filter.
     */
    fun w(throwable: Throwable? = null, message: () -> String)

    /**
     * Writes at [LogLevel.Error].
     *
     * @param throwable The failure to record with the line, when there is one.
     * @param message The line, built only if the level survives the filter.
     */
    fun e(throwable: Throwable? = null, message: () -> String)
}
