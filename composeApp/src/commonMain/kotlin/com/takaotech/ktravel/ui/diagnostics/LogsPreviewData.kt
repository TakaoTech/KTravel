package com.takaotech.ktravel.ui.diagnostics

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.core.logging.DEFAULT_LOG_RETENTION_DAYS
import com.takaotech.ktravel.core.logging.LogEntry
import com.takaotech.ktravel.core.logging.LogLevel
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.presentation.diagnostics.LogsUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** The three states worth looking at: still reading, empty, and a log with a failure in it. */
internal class LogsContentPreviewParams : PreviewParameterProvider<LogsUiState> {

    override val values = sequenceOf(
        previewState.copy(isLoading = true, entries = persistentListOf()),
        previewState.copy(entries = persistentListOf()),
        previewState,
    )
}

private val previewInstant = Instant.parse("2026-09-06T09:41:00Z")

private val previewEntries = persistentListOf(
    LogEntry(
        id = "1",
        timestamp = previewInstant,
        level = LogLevel.Info,
        tag = "KTravel",
        message = "Travel plan 42 opened",
    ),
    LogEntry(
        id = "2",
        timestamp = previewInstant,
        level = LogLevel.Debug,
        tag = "gunzou-client",
        message = "GET /v1/route?profile=car",
        travelId = "42",
    ),
    LogEntry(
        id = "3",
        timestamp = previewInstant,
        level = LogLevel.Error,
        tag = "gunzou-navigator",
        message = "The route could not be computed",
        stackTrace = "java.io.IOException: connection refused\n\tat io.ktor.client...",
        travelId = "42",
    ),
)

private val previewState = LogsUiState(
    entries = previewEntries,
    days = persistentListOf(LocalDate(2026, 9, 5), LocalDate(2026, 9, 6)),
    selectedDay = null,
    minLevel = LogLevel.Verbose,
    query = "",
    retentionDays = DEFAULT_LOG_RETENTION_DAYS,
    consent = TelemetryConsent.Denied,
    consentDecidedAt = previewInstant,
    isLoading = false,
    pendingSave = null,
    pendingIssueUrl = null,
    eventSink = {},
)
