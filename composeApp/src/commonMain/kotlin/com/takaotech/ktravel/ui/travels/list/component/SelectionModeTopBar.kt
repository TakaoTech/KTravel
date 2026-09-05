package com.takaotech.ktravel.ui.travels.list.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.takaotech.ktravel.ui.travels.list.TravelListTestTags
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.close
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.travel_selection_cd_delete_selected
import ktravel.composeapp.generated.resources.travel_selection_cd_exit_selection
import ktravel.composeapp.generated.resources.travel_selection_selected_count
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectionModeTopBar(
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
                modifier = Modifier.testTag(TravelListTestTags.TOP_BAR_EXIT_SELECTION),
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
                modifier = Modifier.testTag(TravelListTestTags.TOP_BAR_DELETE_SELECTED),
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
