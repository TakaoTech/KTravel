package com.takaotech.ktravel.ui.intro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.data.archive.TravelArchiveFormat
import com.takaotech.ktravel.domain.archive.ImportConflictStrategy
import com.takaotech.ktravel.presentation.intro.ImportUiState
import com.takaotech.ktravel.presentation.intro.TravelSelectionViewModel
import com.takaotech.ktravel.presentation.intro.TravelSummaryUiState
import com.takaotech.ktravel.ui.common.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.common.ImportConflictDialog
import com.takaotech.ktravel.ui.common.ImportSecretsChoiceDialog
import com.takaotech.ktravel.ui.common.ImportSecretsPasswordDialog
import com.takaotech.ktravel.ui.common.message
import com.takaotech.ktravel.ui.common.rememberDisruptiveOperationDialog
import com.takaotech.ktravel.ui.theme.KTravelTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.app_settings_cd_open
import ktravel.composeapp.generated.resources.close
import ktravel.composeapp.generated.resources.date_range
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.file_open
import ktravel.composeapp.generated.resources.settings
import ktravel.composeapp.generated.resources.travel_selection_cd_delete_selected
import ktravel.composeapp.generated.resources.travel_selection_cd_exit_selection
import ktravel.composeapp.generated.resources.travel_selection_cd_import
import ktravel.composeapp.generated.resources.travel_selection_cd_swipe_delete
import ktravel.composeapp.generated.resources.travel_selection_import_duplicate_name
import ktravel.composeapp.generated.resources.travel_selection_import_success
import ktravel.composeapp.generated.resources.travel_selection_selected_count
import ktravel.composeapp.generated.resources.travel_selection_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Serializable
object TravelSelectionPage

internal object TravelSelectionTestTags {
    const val SEARCH_BAR = "travel_selection_search_bar"
    const val FAB_NEW_TRAVEL = "travel_selection_fab"
    const val TOP_BAR_EXIT_SELECTION = "travel_selection_exit_selection"
    const val TOP_BAR_DELETE_SELECTED = "travel_selection_delete_selected"
    const val TOP_BAR_IMPORT = "travel_selection_import"
    const val TOP_BAR_APP_SETTINGS = "travel_selection_app_settings"
    fun travelItemTag(id: String) = "travel_item_$id"
}

/** True as long as no import is running or waiting for a choice. */
private val ImportUiState.isIdle: Boolean
    get() = this !is ImportUiState.Reading &&
        this !is ImportUiState.Importing &&
        this !is ImportUiState.AwaitingConflictChoice &&
        this !is ImportUiState.AwaitingSecretsChoice &&
        this !is ImportUiState.AwaitingSecretsPassword

private val ImportUiState.isRunning: Boolean
    get() = this is ImportUiState.Reading || this is ImportUiState.Importing

/** Message to show the user, or null when there is nothing to report. */
@Composable
private fun ImportUiState.message(): String? = when (this) {
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
fun TravelSelectionPage(
    onTravelClick: (id: String) -> Unit,
    onNewTravelClick: () -> Unit,
    onAppSettingsClick: () -> Unit,
) {
    val viewModel: TravelSelectionViewModel = metroViewModel()
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

    TravelSelectionPage(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelSelectionPage(
    travelList: PersistentList<TravelSummaryUiState>,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    selectedIds: ImmutableSet<String> = persistentSetOf(),
    importState: ImportUiState = ImportUiState.Idle,
    onTravelClick: (id: String) -> Unit,
    onTravelLongClick: (id: String) -> Unit = {},
    onExitSelectionMode: () -> Unit = {},
    onDeleteSelectedClick: () -> Unit = {},
    onSwipeToDelete: (id: String) -> Unit = {},
    onImportClick: () -> Unit = {},
    onImportMessageShown: () -> Unit = {},
    onAppSettingsClick: () -> Unit = {},
    newTravelClick: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val importMessage = importState.message()

    LaunchedEffect(importMessage) {
        importMessage?.let {
            snackbarHostState.showSnackbar(it)
            onImportMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                SelectionModeTopBar(
                    selectedCount = selectedIds.size,
                    onExitSelectionMode = onExitSelectionMode,
                    onDeleteSelectedClick = onDeleteSelectedClick,
                )
            } else {
                Column {
                    TopAppBar(
                        title = { Text(text = stringResource(Res.string.travel_selection_title)) },
                        actions = {
                            IconButton(
                                modifier = Modifier.testTag(TravelSelectionTestTags.TOP_BAR_IMPORT),
                                onClick = onImportClick,
                                enabled = importState.isIdle,
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.file_open),
                                    contentDescription = stringResource(
                                        Res.string.travel_selection_cd_import,
                                    ),
                                )
                            }

                            IconButton(
                                modifier = Modifier.testTag(TravelSelectionTestTags.TOP_BAR_APP_SETTINGS),
                                onClick = onAppSettingsClick,
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.settings),
                                    contentDescription = stringResource(Res.string.app_settings_cd_open),
                                )
                            }
                        },
                    )

                    if (importState.isRunning) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(
                    modifier = Modifier.testTag(TravelSelectionTestTags.FAB_NEW_TRAVEL),
                    onClick = newTravelClick,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.add),
                        contentDescription = null,
                    )
                }
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            stickyHeader {
                val searchBarState = rememberSearchBarState()
                var textFieldState by remember { mutableStateOf(TextFieldState()) }

                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag(TravelSelectionTestTags.SEARCH_BAR),
                    state = searchBarState,
                    inputField = {
                        InputField(
                            placeholder = { Text("Cerca un viaggio creato") },
                            textFieldState = textFieldState,
                            searchBarState = searchBarState,
                            onSearch = {
                            },
                        )
                    },
                )
            }

            items(
                key = { travel -> travel.id },
                items = travelList,
            ) { travel ->
                // TODO Format date correctly
                TravelItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag(TravelSelectionTestTags.travelItemTag(travel.id)),
                    name = travel.name,
                    startDate = travel.periodStart.toString(),
                    endDate = travel.periodEnd.toString(),
                    isSelectionMode = isSelectionMode,
                    selected = travel.id in selectedIds,
                    onClick = {
                        onTravelClick(travel.id)
                    },
                    onLongClick = {
                        onTravelLongClick(travel.id)
                    },
                    onSwipeToDelete = {
                        onSwipeToDelete(travel.id)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionModeTopBar(
    selectedCount: Int,
    onExitSelectionMode: () -> Unit,
    onDeleteSelectedClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = pluralStringResource(
                    Res.plurals.travel_selection_selected_count,
                    selectedCount,
                    selectedCount,
                ),
            )
        },
        navigationIcon = {
            IconButton(
                modifier = Modifier.testTag(TravelSelectionTestTags.TOP_BAR_EXIT_SELECTION),
                onClick = onExitSelectionMode,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.close),
                    contentDescription = stringResource(Res.string.travel_selection_cd_exit_selection),
                )
            }
        },
        actions = {
            IconButton(
                modifier = Modifier.testTag(TravelSelectionTestTags.TOP_BAR_DELETE_SELECTED),
                enabled = selectedCount > 0,
                onClick = onDeleteSelectedClick,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    contentDescription = stringResource(Res.string.travel_selection_cd_delete_selected),
                )
            }
        },
    )
}

@Composable
internal fun TravelItem(
    name: String,
    startDate: String?,
    endDate: String?,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onSwipeToDelete: () -> Unit = {},
) {
    val itemModifier = modifier.semantics { this.selected = selected }

    if (isSelectionMode) {
        // Swiping is disabled while picking items, otherwise the two gestures would fight each other.
        TravelItemCard(
            modifier = itemModifier,
            name = name,
            startDate = startDate,
            endDate = endDate,
            isSelectionMode = true,
            selected = selected,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    } else {
        SwipeToDeleteBox(
            modifier = itemModifier,
            onSwipeToDelete = onSwipeToDelete,
        ) {
            TravelItemCard(
                modifier = Modifier.fillMaxWidth(),
                name = name,
                startDate = startDate,
                endDate = endDate,
                isSelectionMode = false,
                selected = selected,
                onClick = onClick,
                onLongClick = onLongClick,
            )
        }
    }
}

/**
 * Wraps [content] in a swipe away gesture that asks for the deletion of the item. The direction is
 * the logical [SwipeToDismissBoxValue.EndToStart], so it is a swipe to the left on left to right
 * layouts and a swipe to the right on right to left ones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteBox(
    onSwipeToDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentOnSwipeToDelete by rememberUpdatedState(onSwipeToDelete)
    val dismissState = rememberSwipeToDismissBoxState(
        // Always refuses the state change: the item is only removed once the confirmation dialog is
        // accepted, so a cancelled deletion must not leave a dismissed row behind.
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                currentOnSwipeToDelete()
            }
            false
        },
    )

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardDefaults.shape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                // Alignment.CenterEnd mirrors itself on right to left layouts, matching the gesture.
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    contentDescription = stringResource(Res.string.travel_selection_cd_swipe_delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        content = { content() },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TravelItemCard(
    name: String,
    startDate: String?,
    endDate: String?,
    isSelectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelectionMode) {
                Checkbox(
                    modifier = Modifier.padding(start = 8.dp),
                    checked = selected,
                    // The whole card toggles the selection, the checkbox is only an indicator.
                    onCheckedChange = null,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                )

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.date_range),
                        contentDescription = null,
                    )

                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = "$startDate - $endDate",
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private val previewTravelList = persistentListOf(
    TravelSummaryUiState(
        id = "1",
        name = "Viaggio a Tokyo",
        periodStart = Clock.System.now().toLocalDate(),
        periodEnd = Clock.System.now().toLocalDate(),
    ),
    TravelSummaryUiState(
        id = "2",
        name = "Weekend a Roma",
        periodStart = Clock.System.now().toLocalDate(),
        periodEnd = Clock.System.now().toLocalDate(),
    ),
)

@PreviewScreenSizes
@Composable
private fun TravelSelectionPagePreview() = KTravelTheme {
    TravelSelectionPage(
        travelList = previewTravelList,
        onTravelClick = {},
        newTravelClick = {},
    )
}

@PreviewScreenSizes
@Composable
private fun TravelSelectionPageSelectionModePreview() = KTravelTheme {
    TravelSelectionPage(
        travelList = previewTravelList,
        isSelectionMode = true,
        selectedIds = persistentSetOf("1"),
        onTravelClick = {},
        newTravelClick = {},
    )
}
