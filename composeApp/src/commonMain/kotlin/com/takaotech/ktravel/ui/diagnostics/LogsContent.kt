package com.takaotech.ktravel.ui.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.core.logging.LogLevel
import com.takaotech.ktravel.core.logging.MAX_LOG_RETENTION_DAYS
import com.takaotech.ktravel.core.logging.MIN_LOG_RETENTION_DAYS
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.domain.staticflows.CONSENT_VALIDITY
import com.takaotech.ktravel.presentation.diagnostics.LogsEvent
import com.takaotech.ktravel.presentation.diagnostics.LogsUiState
import com.takaotech.ktravel.ui.shared.format.formatDayMonthYear
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.description
import ktravel.composeapp.generated.resources.diagnostics_cd_clear
import ktravel.composeapp.generated.resources.diagnostics_cd_report
import ktravel.composeapp.generated.resources.diagnostics_cd_save
import ktravel.composeapp.generated.resources.diagnostics_consent_decided
import ktravel.composeapp.generated.resources.diagnostics_consent_label
import ktravel.composeapp.generated.resources.diagnostics_consent_never
import ktravel.composeapp.generated.resources.diagnostics_day_all
import ktravel.composeapp.generated.resources.diagnostics_empty
import ktravel.composeapp.generated.resources.diagnostics_level_all
import ktravel.composeapp.generated.resources.diagnostics_retention_label
import ktravel.composeapp.generated.resources.diagnostics_search_label
import ktravel.composeapp.generated.resources.diagnostics_title
import ktravel.composeapp.generated.resources.file_export
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/**
 * The log, the filters over it, and the two settings that decide how much of it exists.
 *
 * Everything it draws is an argument, so it renders in a preview and in a test without a graph
 * behind it — the same split every other screen here uses.
 *
 * @param state What to draw.
 * @param snackbarHostState Where "saved" and the report hint are shown.
 * @param modifier The modifier applied to the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogsContent(state: LogsUiState, snackbarHostState: SnackbarHostState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.diagnostics_title)) },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(LogsEvent.Back) }) {
                        Icon(painterResource(Res.drawable.arrow_back), contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { state.eventSink(LogsEvent.SaveRequested) },
                        modifier = Modifier.testTag(LogsTestTags.SAVE),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.file_export),
                            contentDescription = stringResource(Res.string.diagnostics_cd_save),
                        )
                    }
                    IconButton(
                        onClick = { state.eventSink(LogsEvent.ReportRequested) },
                        modifier = Modifier.testTag(LogsTestTags.REPORT),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.description),
                            contentDescription = stringResource(Res.string.diagnostics_cd_report),
                        )
                    }
                    IconButton(
                        onClick = { state.eventSink(LogsEvent.Clear) },
                        modifier = Modifier.testTag(LogsTestTags.CLEAR),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.delete),
                            contentDescription = stringResource(Res.string.diagnostics_cd_clear),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            DiagnosticsSettings(state = state)

            HorizontalDivider()

            LogFilters(state = state)

            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

            if (state.entries.isEmpty() && !state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.diagnostics_empty),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp).testTag(LogsTestTags.EMPTY),
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().testTag(LogsTestTags.LIST)) {
                    items(state.entries, key = { it.id }) { entry ->
                        LogRow(entry)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

/** The two decisions that belong to the log itself: who sees it, and for how long it is kept. */
@Composable
private fun DiagnosticsSettings(state: LogsUiState, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.diagnostics_consent_label),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = state.consent == TelemetryConsent.Granted,
                    onCheckedChange = { state.eventSink(LogsEvent.ConsentChanged(it)) },
                    modifier = Modifier.testTag(LogsTestTags.CONSENT_SWITCH),
                )
            }

            ConsentDecisionLine(decidedAt = state.consentDecidedAt)

            Row(
                modifier = Modifier.fillMaxWidth().testTag(LogsTestTags.RETENTION),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        Res.string.diagnostics_retention_label,
                        state.retentionDays,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { state.eventSink(LogsEvent.RetentionChanged(state.retentionDays - 1)) },
                    enabled = state.retentionDays > MIN_LOG_RETENTION_DAYS,
                ) {
                    // TODO Change to Icon
                    Text("-")
                }
                TextButton(
                    onClick = { state.eventSink(LogsEvent.RetentionChanged(state.retentionDays + 1)) },
                    enabled = state.retentionDays < MAX_LOG_RETENTION_DAYS,
                ) {
                    // TODO Change to Icon
                    Text("+")
                }
            }
        }
    }
}

/** When the user answered about diagnostics, and when the answer runs out. */
@Composable
private fun ConsentDecisionLine(decidedAt: Instant?, modifier: Modifier = Modifier) {
    val zone = TimeZone.currentSystemDefault()
    val text = if (decidedAt == null) {
        stringResource(Res.string.diagnostics_consent_never)
    } else {
        stringResource(
            Res.string.diagnostics_consent_decided,
            decidedAt.toLocalDate(zone).formatDayMonthYear(),
            (decidedAt + CONSENT_VALIDITY).toLocalDate(zone).formatDayMonthYear(),
        )
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

/** Which day, how loud, and what the line has to say. */
@Composable
private fun LogFilters(state: LogsUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DayFilter(state = state, modifier = Modifier.weight(1f))
            LevelFilter(state = state, modifier = Modifier.weight(1f))
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = { state.eventSink(LogsEvent.QueryChanged(it)) },
            label = { Text(stringResource(Res.string.diagnostics_search_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(LogsTestTags.SEARCH),
        )
    }
}

/** The day being read, or all of the days being kept. */
@Composable
private fun DayFilter(state: LogsUiState, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val allDays = stringResource(Res.string.diagnostics_day_all)

    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().testTag(LogsTestTags.DAY_FILTER),
        ) {
            Text(state.selectedDay?.toString() ?: allDays)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(allDays) },
                onClick = {
                    state.eventSink(LogsEvent.DaySelected(null))
                    expanded = false
                },
            )
            state.days.forEach { day: LocalDate ->
                DropdownMenuItem(
                    text = { Text(day.toString()) },
                    onClick = {
                        state.eventSink(LogsEvent.DaySelected(day))
                        expanded = false
                    },
                )
            }
        }
    }
}

/** The quietest level shown. */
@Composable
private fun LevelFilter(state: LogsUiState, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val allLevels = stringResource(Res.string.diagnostics_level_all)

    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().testTag(LogsTestTags.LEVEL_FILTER),
        ) {
            Text(if (state.minLevel == LogLevel.Verbose) allLevels else state.minLevel.name)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LogLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { Text(if (level == LogLevel.Verbose) allLevels else level.name) },
                    onClick = {
                        state.eventSink(LogsEvent.LevelSelected(level))
                        expanded = false
                    },
                )
            }
        }
    }
}

//region Previews

@PreviewScreenSizes
@Composable
private fun LogsContentPreview(@PreviewParameter(LogsContentPreviewParams::class) state: LogsUiState) = KTravelTheme {
    LogsContent(state = state, snackbarHostState = remember { SnackbarHostState() })
}
//endregion Previews
