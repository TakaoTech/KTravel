package com.takaotech.ktravel.ui.travels.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.takaotech.ktravel.data.archive.TravelArchiveFormat
import com.takaotech.ktravel.domain.archive.ImportConflictStrategy
import com.takaotech.ktravel.presentation.travels.ImportUiState
import com.takaotech.ktravel.presentation.travels.TravelListViewModel
import com.takaotech.ktravel.ui.shared.dialog.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.shared.dialog.ImportConflictDialog
import com.takaotech.ktravel.ui.shared.dialog.ImportSecretsChoiceDialog
import com.takaotech.ktravel.ui.shared.dialog.ImportSecretsPasswordDialog
import com.takaotech.ktravel.ui.shared.dialog.rememberDisruptiveOperationDialog
import com.takaotech.ktravel.ui.shared.format.message
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.travel_selection_import_duplicate_name
import ktravel.composeapp.generated.resources.travel_selection_import_success
import org.jetbrains.compose.resources.stringResource

/** True as long as no import is running or waiting for a choice. */
internal val ImportUiState.isIdle: Boolean
    get() = this !is ImportUiState.Reading &&
        this !is ImportUiState.Importing &&
        this !is ImportUiState.AwaitingConflictChoice &&
        this !is ImportUiState.AwaitingSecretsChoice &&
        this !is ImportUiState.AwaitingSecretsPassword

internal val ImportUiState.isRunning: Boolean
    get() = this is ImportUiState.Reading || this is ImportUiState.Importing

/** Message to show the user, or null when there is nothing to report. */
@Composable
internal fun ImportUiState.message(): String? = when (this) {
    ImportUiState.Idle,
    ImportUiState.Reading,
    ImportUiState.Importing,
    is ImportUiState.AwaitingConflictChoice,
    ImportUiState.AwaitingSecretsChoice,
    is ImportUiState.AwaitingSecretsPassword,
    -> null

    is ImportUiState.Completed ->
        stringResource(Res.string.travel_selection_import_success, travelName)

    is ImportUiState.Failed -> error.message()
}

@Composable
fun TravelListPage(
    viewModel: TravelListViewModel = metroViewModel(),
    onTravelClick: (id: String) -> Unit,
    onNewTravelClick: () -> Unit,
    onAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(viewModel) {
        viewModel.loadTravelPlans()
        onPauseOrDispose { }
    }

    val deleteDialogState = rememberDisruptiveOperationDialog<PersistentSet<String>> { ids ->
        viewModel.deleteTravels(ids)
    }

    DisruptiveOperationDialog(state = deleteDialogState)

    val importLauncher = rememberFilePickerLauncher(
        type = FileKitType.File(TravelArchiveFormat.ACCEPTED_EXTENSIONS),
        mode = FileKitMode.Single,
    ) { file ->
        file?.let(viewModel::stageImport)
    }

    val importState = uiState.import
    if (importState is ImportUiState.AwaitingConflictChoice) {
        // The copy name is formatted here: stringResource cannot be called from the ViewModel.
        val duplicateName = stringResource(
            Res.string.travel_selection_import_duplicate_name,
            importState.importedName,
        )
        ImportConflictDialog(
            existingName = importState.existingName,
            onDuplicate = {
                viewModel.confirmImport(ImportConflictStrategy.DUPLICATE, duplicateName)
            },
            onReplace = {
                viewModel.confirmImport(ImportConflictStrategy.REPLACE, duplicateName)
            },
            onDismiss = viewModel::cancelImport,
        )
    }

    if (importState is ImportUiState.AwaitingSecretsChoice) {
        ImportSecretsChoiceDialog(
            onImport = { viewModel.onSecretsChoice(includeSecrets = true) },
            onSkip = { viewModel.onSecretsChoice(includeSecrets = false) },
        )
    }

    if (importState is ImportUiState.AwaitingSecretsPassword) {
        ImportSecretsPasswordDialog(
            attemptFailed = importState.attemptFailed,
            onConfirm = viewModel::submitSecretsPassword,
            onCancel = viewModel::cancelImport,
        )
    }

    if (uiState.isSelectionMode) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = true,
            onBackCompleted = viewModel::exitSelectionMode,
        )
    }

    TravelListContent(
        modifier = modifier,
        travelList = uiState.travelList,
        isSelectionMode = uiState.isSelectionMode,
        selectedIds = uiState.selectedIds,
        importState = importState,
        onTravelClick = { id ->
            if (uiState.isSelectionMode) viewModel.toggleSelection(id) else onTravelClick(id)
        },
        onTravelLongClick = viewModel::enterSelectionMode,
        onExitSelectionMode = viewModel::exitSelectionMode,
        onDeleteSelectedClick = { deleteDialogState.show(uiState.selectedIds) },
        onSwipeToDelete = { id -> deleteDialogState.show(persistentSetOf(id)) },
        onImportClick = { importLauncher.launch() },
        onAppSettingsClick = onAppSettingsClick,
        onImportMessageShown = viewModel::onImportMessageShown,
        newTravelClick = onNewTravelClick,
    )
}
