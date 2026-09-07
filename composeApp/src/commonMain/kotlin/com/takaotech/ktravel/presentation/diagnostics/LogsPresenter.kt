package com.takaotech.ktravel.presentation.diagnostics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.core.KTravelBuildInfo
import com.takaotech.ktravel.core.logging.LogEntry
import com.takaotech.ktravel.core.logging.LogLevel
import com.takaotech.ktravel.core.logging.LogRepository
import com.takaotech.ktravel.core.logging.LogStore
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.core.telemetry.TelemetrySink
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.diagnostics.IssueReportBuilder
import com.takaotech.ktravel.domain.diagnostics.gitHubIssueUrl
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import io.github.kdroidfilter.platformtools.getOperatingSystem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/**
 * The log this installation kept, filtered the way the user asked for.
 *
 * Where the lines come from is deliberately split in two: the files are read again only when the day
 * being shown changes or something is deleted, while the lines of the running session arrive through
 * [LogStore] and are merged here. Re-reading a day's file on every logged line would make the screen
 * the loudest thing in the log.
 *
 * @param screen The trip to narrow to, when the screen was opened from inside one.
 * @param navigator Where the screen can go: back, or to the privacy notice.
 * @param logRepository Reads the kept log.
 * @param logStore The lines of the running session.
 * @param appSettingsRepository The consent and the retention, both editable from here.
 * @param telemetrySink Told when the consent changes, so it stops or resumes at once.
 */
@Suppress("LongMethod", "CyclomaticComplexMethod")
@CircuitInject(LogsScreen::class, AppScope::class)
@Composable
fun LogsPresenter(
    screen: LogsScreen,
    navigator: Navigator,
    logRepository: LogRepository,
    logStore: LogStore,
    appSettingsRepository: AppSettingsRepository,
    telemetrySink: TelemetrySink,
): LogsUiState {
    val scope = rememberCoroutineScope()
    val settings by appSettingsRepository.settings.collectAsState()
    val live by logStore.entries.collectAsState()

    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }
    var minLevel by remember { mutableStateOf(LogLevel.Verbose) }
    var query by remember { mutableStateOf("") }
    var pendingSave by remember { mutableStateOf<PendingLogSave?>(null) }
    var pendingIssueUrl by remember { mutableStateOf<String?>(null) }

    // Bumped whenever the files change under us — a deletion, a change of retention — so the read
    // below runs again without the screen having to know what changed.
    var refreshToken by remember { mutableIntStateOf(0) }

    val days by produceState(persistentListOf<LocalDate>() as ImmutableList<LocalDate>, refreshToken, live.size) {
        value = logRepository.days()
    }

    val persisted by produceState(
        initialValue = null as ImmutableList<LogEntry>?,
        selectedDay,
        refreshToken,
    ) {
        value = logRepository.persistedEntries(selectedDay)
    }

    val entries = remember(persisted, live, selectedDay, minLevel, query, screen.travelId) {
        (persisted.orEmpty() + live)
            .distinctBy { it.id }
            .filter { entry -> entry.matches(screen.travelId, selectedDay, minLevel, query, logRepository) }
            .sortedBy { it.timestamp }
            .toImmutableList()
    }

    return LogsUiState(
        entries = entries,
        days = days,
        selectedDay = selectedDay,
        minLevel = minLevel,
        query = query,
        retentionDays = settings.logRetentionDays,
        consent = settings.effectiveConsent(Clock.System.now()),
        consentDecidedAt = settings.consentDecidedAt,
        isLoading = persisted == null,
        pendingSave = pendingSave,
        pendingIssueUrl = pendingIssueUrl,
    ) { event ->
        when (event) {
            LogsEvent.Back -> navigator.pop()

            is LogsEvent.DaySelected -> selectedDay = event.day

            is LogsEvent.LevelSelected -> minLevel = event.level

            is LogsEvent.QueryChanged -> query = event.query

            is LogsEvent.RetentionChanged -> scope.launch {
                appSettingsRepository.updateLogRetentionDays(event.days)
                refreshToken++
            }

            is LogsEvent.ConsentChanged -> scope.launch {
                val consent = if (event.granted) TelemetryConsent.Granted else TelemetryConsent.Denied

                appSettingsRepository.updateTelemetryConsent(
                    consent = consent,
                    flowVersion = settings.acknowledgedConsentVersion,
                    decidedAt = Clock.System.now(),
                )

                // Revoking is not only "stop sending": what was already sent is asked to be dropped.
                if (!event.granted) telemetrySink.forgetMe()
            }

            LogsEvent.SaveRequested -> scope.launch {
                pendingSave = PendingLogSave(fileName = logFileName(), content = logRepository.exportText())
            }

            LogsEvent.SaveHandled -> pendingSave = null

            LogsEvent.ReportRequested -> scope.launch {
                val log = logRepository.exportText()
                val report = IssueReportBuilder.build(
                    appVersion = KTravelBuildInfo.VERSION,
                    platform = getOperatingSystem().name,
                    consent = settings.effectiveConsent(Clock.System.now()),
                    installationId = appSettingsRepository.installationId(),
                    logTail = log,
                )

                pendingSave = PendingLogSave(fileName = logFileName(), content = log)
                pendingIssueUrl = gitHubIssueUrl(report)
            }

            LogsEvent.ReportHandled -> pendingIssueUrl = null

            LogsEvent.Clear -> scope.launch {
                logRepository.clear()
                refreshToken++
            }
        }
    }
}

/** Whether a line survives the filters the user set. */
private fun LogEntry.matches(
    travelId: String?,
    day: LocalDate?,
    minLevel: LogLevel,
    query: String,
    repository: LogRepository,
): Boolean {
    val belongsToTrip = travelId == null || this.travelId == travelId
    val belongsToDay = day == null || repository.dayOf(this) == day
    val loudEnough = level.atLeast(minLevel)
    val matchesQuery = query.isBlank() ||
        message.contains(query, ignoreCase = true) ||
        tag.contains(query, ignoreCase = true)

    return belongsToTrip && belongsToDay && loudEnough && matchesQuery
}

/** The name suggested when the log is saved, distinct per moment so two saves do not collide. */
private fun logFileName(): String = "ktravel-log-${Clock.System.now().toEpochMilliseconds()}"
