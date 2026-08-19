package com.takaotech.ktravel.ui.planning.transport.preview

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.check
import ktravel.composeapp.generated.resources.route_preview_alternative
import ktravel.composeapp.generated.resources.route_preview_cd_confirm
import ktravel.composeapp.generated.resources.route_preview_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Height of the sheet left visible over the map before the traveller pulls it up. */
private val SHEET_PEEK_HEIGHT = 128.dp

/**
 * The chrome both preview screens sit in: a title bar that confirms, a list, and a map.
 *
 * The only thing the two screens share, and deliberately so. What goes in [list] has nothing in
 * common between them — one is a list of manoeuvres, the other a timeline of departures — so it is
 * a slot rather than a parameter, and this file knows nothing about either model.
 *
 * On a wide window the list and the map sit side by side; on a narrow one the list is a bottom sheet
 * over the map. The map is told when the sheet covers it so its gestures stop competing with the
 * drag that is pulling the sheet.
 *
 * @param alternatives How many options the traveller can choose between. A single one draws no tabs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePreviewScaffold(
    alternatives: Int,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onConfirm: () -> Unit,
    list: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    map: @Composable (mapEnabled: Boolean) -> Unit,
) {
    val wide = currentWindowAdaptiveInfoV2().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    val listWithTabs: @Composable () -> Unit = {
        androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
            if (alternatives > 1) {
                AlternativeTabs(alternatives = alternatives, selectedIndex = selectedIndex, onSelect = onSelect)
            }
            list()
        }
    }

    if (wide) {
        Scaffold(modifier = modifier, topBar = { RoutePreviewTopBar(onConfirm) }) { insets ->
            Row(modifier = Modifier.padding(insets)) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) { listWithTabs() }
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight(),
                ) {
                    map(true)
                }
            }
        }
    } else {
        val sheetState = rememberBottomSheetScaffoldState()
        var mapEnabled by remember { mutableStateOf(true) }

        LaunchedEffect(sheetState.bottomSheetState) {
            snapshotFlow { sheetState.bottomSheetState.hasExpandedState }.collect { mapEnabled = it }
        }

        BottomSheetScaffold(
            modifier = modifier,
            scaffoldState = sheetState,
            sheetPeekHeight = SHEET_PEEK_HEIGHT,
            sheetContent = { listWithTabs() },
            topBar = { RoutePreviewTopBar(onConfirm) },
        ) {
            map(mapEnabled)
        }
    }
}

@Composable
private fun AlternativeTabs(alternatives: Int, selectedIndex: Int, onSelect: (Int) -> Unit) {
    PrimaryScrollableTabRow(selectedTabIndex = selectedIndex, modifier = Modifier.fillMaxWidth()) {
        repeat(alternatives) { index ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                text = { Text(stringResource(Res.string.route_preview_alternative, index + 1)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutePreviewTopBar(onConfirm: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(Res.string.route_preview_title)) },
        actions = {
            IconButton(onClick = onConfirm) {
                Icon(
                    painter = painterResource(Res.drawable.check),
                    contentDescription = stringResource(Res.string.route_preview_cd_confirm),
                )
            }
        },
    )
}
