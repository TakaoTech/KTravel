package com.takaotech.ktravel.ui.diagnostics

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.diagnostics.LogsEvent
import com.takaotech.ktravel.presentation.diagnostics.LogsScreen
import com.takaotech.ktravel.presentation.diagnostics.LogsUiState
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.launch
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.diagnostics_report_hint
import ktravel.composeapp.generated.resources.diagnostics_saved
import org.jetbrains.compose.resources.stringResource

/** Extension of the saved log file, which is plain text on every platform. */
private const val LOG_FILE_EXTENSION = "txt"

/**
 * Circuit entry point of the diagnostics screen.
 *
 * The two things the presenter cannot do live here, because both are platform dialogs: writing the
 * log wherever the user chooses, and handing the prefilled issue to the browser. The presenter asks
 * for them through the state — `pendingSave`, `pendingIssueUrl` — and is told when they are done.
 */
@CircuitInject(LogsScreen::class, AppScope::class)
@Composable
fun LogsUi(state: LogsUiState, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(Res.string.diagnostics_saved)
    val reportHint = stringResource(Res.string.diagnostics_report_hint)

    // Held aside because the save dialog answers later, with a destination and nothing else.
    var contentToWrite by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val saver = rememberFileSaverLauncher(FileKitDialogSettings.createDefault()) { file ->
        val content = contentToWrite

        contentToWrite = null
        state.eventSink(LogsEvent.SaveHandled)

        if (file != null && content != null) {
            // Writing is suspending and the launcher's callback is not, so it goes to the scope.
            scope.launch {
                file.write(content.encodeToByteArray())
                snackbarHostState.showSnackbar(savedMessage)
            }
        }
    }

    LaunchedEffect(state.pendingSave) {
        val pending = state.pendingSave ?: return@LaunchedEffect

        contentToWrite = pending.content
        saver.launch(suggestedName = pending.fileName, defaultExtension = LOG_FILE_EXTENSION)
    }

    LaunchedEffect(state.pendingIssueUrl) {
        val url = state.pendingIssueUrl ?: return@LaunchedEffect

        uriHandler.openUri(url)
        state.eventSink(LogsEvent.ReportHandled)
        snackbarHostState.showSnackbar(reportHint)
    }

    LogsContent(state = state, snackbarHostState = snackbarHostState, modifier = modifier)
}
