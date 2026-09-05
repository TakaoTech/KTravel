package com.takaotech.ktravel.ui.plan.overview

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.data.archive.TravelArchiveFormat
import com.takaotech.ktravel.presentation.plan.ExportUiState
import com.takaotech.ktravel.presentation.plan.PlanOverviewViewModel
import com.takaotech.ktravel.ui.shared.dialog.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.shared.dialog.ExportSecretsDialog
import com.takaotech.ktravel.ui.shared.dialog.rememberDisruptiveOperationDialog
import com.takaotech.ktravel.ui.shared.format.message
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_trip_export_success
import ktravel.composeapp.generated.resources.planning_trip_export_success_partial
import org.jetbrains.compose.resources.stringResource

/** Message to show the user, or null when there is nothing to report. */
@Composable
internal fun ExportUiState.message(): String? = when (this) {
    ExportUiState.Idle, ExportUiState.AwaitingSecretsChoice, ExportUiState.InProgress -> null

    is ExportUiState.Completed -> if (skippedAttachments == 0) {
        stringResource(Res.string.planning_trip_export_success)
    } else {
        stringResource(Res.string.planning_trip_export_success_partial, skippedAttachments)
    }

    is ExportUiState.Failed -> error.message()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanOverviewPage(
    viewModel: PlanOverviewViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSettingClicked: () -> Unit,
    onAddPlaceClicked: () -> Unit,
    onDateClicked: (id: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val planHeader = uiState.planHeader
    val days = uiState.days

    val deleteDialogState = rememberDisruptiveOperationDialog<String> { placeId ->
        viewModel.deletePlace(placeId)
    }

    DisruptiveOperationDialog(
        state = deleteDialogState,
    )

    // The password survives the file saver round trip: it is chosen before a destination exists,
    // and the export only starts once both are known.
    var secretsPassword by remember { mutableStateOf<String?>(null) }

    // The file saver belongs to the page: the chosen destination has to be handed to the ViewModel,
    // the only one that knows which trip to export.
    val exportLauncher = rememberFileSaverLauncher(FileKitDialogSettings.createDefault()) { file ->
        if (file == null) {
            viewModel.cancelExport()
        } else {
            viewModel.exportTravel(file, secretsPassword)
        }
        secretsPassword = null
    }

    fun launchFileSaver() {
        exportLauncher.launch(
            suggestedName = planHeader.name.text.toArchiveFileName(),
            extension = TravelArchiveFormat.FILE_EXTENSION,
        )
    }

    if (uiState.export is ExportUiState.AwaitingSecretsChoice) {
        ExportSecretsDialog(
            onConfirm = { password ->
                secretsPassword = password
                launchFileSaver()
            },
            onDismiss = viewModel::cancelExport,
        )
    }

    PlanOverviewContent(
        modifier = modifier,
        planHeader = planHeader,
        places = uiState.places,
        days = days,
        exportState = uiState.export,
        onBackClick = onBackClick,
        onExportClick = {
            // With no API key configured there is nothing to ask about, so the dialog is skipped.
            if (viewModel.hasApiKey) viewModel.startExport() else launchFileSaver()
        },
        onExportMessageShown = viewModel::onExportMessageShown,
        onPlanNameChange = {
            viewModel.onPlanNameChanged(it)
        },
        onPlanDateRangeChanged = { start, end ->
            viewModel.onPlanDateChanged(start, end)
        },
        onAddPlaceClicked = onAddPlaceClicked,
        onDeletePermanentPlaceClick = {
            deleteDialogState.show(it)
        },
        onDateClicked = onDateClicked,
        onPlaceMovedToDay = { placeId, dayId ->
            viewModel.onPlaceMovedToDate(placeId, dayId)
        },
        onSettingClicked = onSettingClicked,
    )
}
