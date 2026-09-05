package com.takaotech.ktravel.ui.travels.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.travels.ImportUiState
import com.takaotech.ktravel.presentation.travels.TravelSummaryUiState
import com.takaotech.ktravel.ui.theme.KTravelTheme
import com.takaotech.ktravel.ui.travels.list.component.SelectionModeTopBar
import com.takaotech.ktravel.ui.travels.list.component.TravelItem
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentSetOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.app_settings_cd_open
import ktravel.composeapp.generated.resources.file_open
import ktravel.composeapp.generated.resources.settings
import ktravel.composeapp.generated.resources.travel_selection_cd_import
import ktravel.composeapp.generated.resources.travel_selection_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelListContent(
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
                                modifier = Modifier.testTag(TravelListTestTags.TOP_BAR_IMPORT),
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
                                modifier = Modifier.testTag(TravelListTestTags.TOP_BAR_APP_SETTINGS),
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
                    modifier = Modifier.testTag(TravelListTestTags.FAB_NEW_TRAVEL),
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
                        .testTag(TravelListTestTags.SEARCH_BAR),
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
                        .testTag(TravelListTestTags.travelItemTag(travel.id)),
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

//region Previews

@PreviewScreenSizes
@Composable
private fun TravelListPagePreview() = KTravelTheme {
    TravelListContent(
        travelList = previewTravelList,
        onTravelClick = {},
        newTravelClick = {},
    )
}

@PreviewScreenSizes
@Composable
private fun TravelListPageSelectionModePreview() = KTravelTheme {
    TravelListContent(
        travelList = previewTravelList,
        isSelectionMode = true,
        selectedIds = persistentSetOf("1"),
        onTravelClick = {},
        newTravelClick = {},
    )
}
//endregion Previews
