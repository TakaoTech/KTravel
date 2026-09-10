package com.takaotech.ktravel.presentation.diagnostics

import androidx.compose.runtime.Stable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.core.logging.LogEntry
import com.takaotech.ktravel.core.logging.LogLevel
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.di.AppScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * The log this installation has kept, and the settings that decide how much of it there is.
 *
 * Reached from the app settings, where it shows everything, and from a trip's settings, where it
 * shows only what was logged while that trip was open.
 *
 * @property travelId The trip to narrow the log to, or null for all of it.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class LogsScreen(val travelId: String? = null) : Screen

/**
 * What the diagnostics screen draws.
 *
 * @property entries The lines that pass the filters, oldest first.
 * @property days The days that still have a log file, oldest first.
 * @property selectedDay The day being shown, or null for every day kept.
 * @property minLevel The quietest level shown.
 * @property query What the search box holds.
 * @property retentionDays How many days of log are kept.
 * @property consent What the user answered about sending diagnostics.
 * @property consentDecidedAt When they answered, null when they never did.
 * @property isLoading True while the files are being read.
 * @property pendingSave A file the user asked to save, waiting for the platform's save dialog.
 * @property pendingIssueUrl A GitHub issue to open, waiting for the browser.
 * @property eventSink Where the screen sends what the user did.
 */
@Stable
data class LogsUiState(
    val entries: ImmutableList<LogEntry>,
    val days: ImmutableList<LocalDate>,
    val selectedDay: LocalDate?,
    val minLevel: LogLevel,
    val query: String,
    val retentionDays: Int,
    val consent: TelemetryConsent,
    val consentDecidedAt: Instant?,
    val isLoading: Boolean,
    val pendingSave: PendingLogSave?,
    val pendingIssueUrl: String?,
    val eventSink: (LogsEvent) -> Unit,
) : CircuitUiState

/**
 * A log file the user asked for, built and waiting to be written wherever they choose.
 *
 * @property fileName What to suggest in the save dialog.
 * @property content The log, already rendered.
 */
data class PendingLogSave(val fileName: String, val content: String)

/** What the diagnostics screen can ask for. */
sealed interface LogsEvent : CircuitUiEvent {

    /** The user is done. */
    data object Back : LogsEvent

    /**
     * Show one day, or all of them.
     *
     * @property day The day to show, null for all.
     */
    data class DaySelected(val day: LocalDate?) : LogsEvent

    /**
     * Hide everything quieter than this.
     *
     * @property level The quietest level to keep.
     */
    data class LevelSelected(val level: LogLevel) : LogsEvent

    /**
     * Filter by text.
     *
     * @property query What to look for, in the message and in the tag.
     */
    data class QueryChanged(val query: String) : LogsEvent

    /**
     * Keep a different number of days.
     *
     * @property days How many, brought inside the allowed range before it is stored.
     */
    data class RetentionChanged(val days: Int) : LogsEvent

    /**
     * Change the answer about sending diagnostics, without going through the introduction again.
     *
     * @property granted True to start sending, false to stop.
     */
    data class ConsentChanged(val granted: Boolean) : LogsEvent

    /**
     * Ask the backend to forget everything it already holds about this installation.
     *
     * Separate from [ConsentChanged] on purpose: stopping the sending and deleting what was already
     * sent are two different decisions, and the second one is asked for explicitly.
     */
    data object ForgetMeRequested : LogsEvent

    /** Save the whole kept log to a file. */
    data object SaveRequested : LogsEvent

    /** The save dialog is done with, whatever the user chose. */
    data object SaveHandled : LogsEvent

    /** Save the log and open a prefilled GitHub issue. */
    data object ReportRequested : LogsEvent

    /** The browser has been sent to the issue. */
    data object ReportHandled : LogsEvent

    /** Delete every log file and forget the lines held in memory. */
    data object Clear : LogsEvent
}
