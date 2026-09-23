package com.takaotech.ktravel.ui.plan.place

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.presentation.place.PlaceInsertEvent
import com.takaotech.ktravel.presentation.place.PlaceInsertUiState
import com.takaotech.ktravel.presentation.place.PlaceListContent
import com.takaotech.ktravel.ui.plan.place.component.CategoryFilterButton
import com.takaotech.ktravel.ui.plan.place.component.NearbyPlacesList
import com.takaotech.ktravel.ui.plan.place.component.PlaceDetailCard
import com.takaotech.ktravel.ui.plan.place.component.PlaceInsertTopBar
import com.takaotech.ktravel.ui.plan.place.component.PlaceSearchBar
import com.takaotech.ktravel.ui.plan.place.component.PlaceSearchMap
import com.takaotech.ktravel.ui.plan.place.component.SearchResultsList
import com.takaotech.ktravel.ui.plan.place.component.SelectedPlacesInventory
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.persistentListOf

/** Width of the list pane beside the map on a wide window. */
private val LIST_PANE_WIDTH = 372.dp

/** Height of the map above the list on a medium window. */
private val MEDIUM_MAP_HEIGHT = 400.dp

/** Widest the search bar grows over a wide map. */
private val SEARCH_BAR_MAX_WIDTH = 420.dp

/** Width of the place card over a wide map. */
private val DETAIL_CARD_WIDTH = 328.dp

/** Share of the height the list sheet covers over the map when it is open. */
private const val SHEET_OPEN_FRACTION = 0.45f

/** What is left of the list sheet over the map once it is pulled down. */
private val SHEET_PEEK_HEIGHT = 112.dp

/** Space between the things floating over the map and its edges. */
private val OVERLAY_PADDING = 12.dp

/**
 * The «add places» screen: a map with a search bar floating over it, and a list area that shows the
 * framed area, the search results or the selection.
 *
 * On a wide window the list is a pane beside the map, on a medium one it sits under a map of fixed
 * height, and on a narrow one it is a bottom sheet over the map. The place card floats over the
 * bottom of the map, or of the sheet on a narrow window.
 */
@Composable
fun PlaceInsertContent(state: PlaceInsertUiState, modifier: Modifier = Modifier) {
    var areSuggestionsExpanded by remember { mutableStateOf(false) }
    val sink = state.eventSink

    val topBar: @Composable () -> Unit = {
        PlaceInsertTopBar(
            selectedCount = state.selected.size,
            isInventoryOpen = state.isInventoryOpen,
            canConfirm = state.canConfirm,
            onClose = { sink(PlaceInsertEvent.Exit) },
            onInventoryClick = { sink(PlaceInsertEvent.InventoryToggled) },
            onConfirm = { sink(PlaceInsertEvent.Confirm) },
        )
    }
    val listArea: @Composable (Modifier) -> Unit = { PlaceListArea(state = state, modifier = it) }
    val mapArea: @Composable (gesturesEnabled: Boolean, compact: Boolean, Modifier) -> Unit = { gestures, compact, m ->
        MapWithSearch(
            state = state,
            gesturesEnabled = gestures,
            compact = compact,
            areSuggestionsExpanded = areSuggestionsExpanded,
            onSuggestionsExpandedChange = { areSuggestionsExpanded = it },
            modifier = m,
        )
    }
    val detailCard: @Composable (compact: Boolean, Modifier) -> Unit = { compact, m ->
        state.detail?.let { detail ->
            PlaceDetailCard(
                candidate = detail,
                isSelected = state.isSelected(detail.id),
                onDismiss = { sink(PlaceInsertEvent.DetailDismissed) },
                onCenterClick = { sink(PlaceInsertEvent.CenterOn(detail)) },
                onToggleSelection = { sink(PlaceInsertEvent.SelectionToggled(detail)) },
                modifier = m
                    .padding(OVERLAY_PADDING)
                    .then(if (compact) Modifier.fillMaxWidth() else Modifier.width(DETAIL_CARD_WIDTH)),
            )
        }
    }

    // TODO Change windowSizeClass for support 3 gate of width and propagate to components, like MapWithSearch
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) ->
            Scaffold(modifier = modifier, topBar = topBar) { insets ->
                Row(modifier = Modifier.fillMaxSize().padding(insets)) {
                    listArea(Modifier.width(LIST_PANE_WIDTH).fillMaxHeight())
                    VerticalDivider()
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        mapArea(true, false, Modifier.fillMaxSize())
                        detailCard(false, Modifier.align(Alignment.BottomStart))
                    }
                }
            }

//        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
//            Scaffold(modifier = modifier, topBar = topBar) { insets ->
//                Box(modifier = Modifier.fillMaxSize().padding(insets)) {
//                    Column(modifier = Modifier.fillMaxSize()) {
//                        mapArea(true, false, Modifier.fillMaxWidth().height(MEDIUM_MAP_HEIGHT))
//                        HorizontalDivider()
//                        listArea(Modifier.fillMaxWidth().weight(1f))
//                    }
//                    detailCard(false, Modifier.align(Alignment.BottomStart))
//                }
//            }

        else -> CompactLayout(
            topBar = topBar,
            isInventoryOpen = state.isInventoryOpen,
            map = { mapArea(true, true, Modifier.fillMaxSize()) },
            list = { listArea(Modifier.fillMaxWidth()) },
            detailCard = { detailCard(true, it) },
            modifier = modifier,
        )
    }
}

/**
 * Map under a [BottomSheetScaffold] that carries the list.
 *
 * The sheet opens over [SHEET_OPEN_FRACTION] of the height, as it always did, and is pulled down to
 * [SHEET_PEEK_HEIGHT], where its handle and the head of the list stay over the map — the list is
 * never hidden, it only gets out of the map's way, which is why `Hidden` is not among its values.
 *
 * Dragging the sheet is left to the scaffold: it takes the drag from the list as well, so the list
 * scrolls on its own and hands the gesture over once it is at its top.
 *
 * The place card floats over everything along the bottom, the sheet included, so a place picked on
 * the map is read without pulling the list down first.
 *
 * @param isInventoryOpen Whether the sheet is showing the places already chosen. Asking for them
 *   with the sheet pulled down would answer under the map, so the sheet comes back up with them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactLayout(
    topBar: @Composable () -> Unit,
    isInventoryOpen: Boolean,
    map: @Composable () -> Unit,
    list: @Composable () -> Unit,
    detailCard: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = SheetValue.Expanded,
            enabledValues = setOf(SheetValue.PartiallyExpanded, SheetValue.Expanded),
        ),
    )
    LaunchedEffect(isInventoryOpen) {
        if (isInventoryOpen) scaffoldState.bottomSheetState.expand()
    }

    Box(modifier = modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = topBar,
            sheetPeekHeight = SHEET_PEEK_HEIGHT,
            sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            sheetContent = {
                Box(modifier = Modifier.fillMaxHeight(SHEET_OPEN_FRACTION)) {
                    list()
                }
            },
        ) {
            map()
        }

        detailCard(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun MapWithSearch(
    state: PlaceInsertUiState,
    gesturesEnabled: Boolean,
    compact: Boolean,
    areSuggestionsExpanded: Boolean,
    onSuggestionsExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sink = state.eventSink
    Box(modifier = modifier) {
        PlaceSearchMap(
            initialCamera = state.initialCamera,
            cameraRequest = state.cameraRequest,
            places = when (state.listContent) {
                PlaceListContent.NEARBY -> state.nearby.places
                PlaceListContent.SEARCH_RESULTS -> state.search.places
                PlaceListContent.INVENTORY -> persistentListOf()
            },
            selected = state.selected,
            isGesturesEnabled = gesturesEnabled,
            onMarkerClick = { sink(PlaceInsertEvent.DetailOpened(it)) },
            onViewportChange = { area, center -> sink(PlaceInsertEvent.ViewportChanged(area, center)) },
            onMapClick = { onSuggestionsExpandedChange(false) },
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(OVERLAY_PADDING)
                .then(if (compact) Modifier.fillMaxWidth() else Modifier),
        ) {
            // TODO
            PlaceSearchBar(
                query = state.query,
                suggestions = state.suggestions,
                coordinateCandidate = state.coordinateCandidate,
                providers = state.providers,
                selectedProvider = state.selectedProvider,
                isSuggestionsExpanded = areSuggestionsExpanded,
                onQueryChange = { sink(PlaceInsertEvent.QueryChanged(it)) },
                onClear = { sink(PlaceInsertEvent.QueryCleared) },
                onSuggestionChosen = { sink(PlaceInsertEvent.SuggestionChosen(it)) },
                onProviderSelect = { sink(PlaceInsertEvent.ProviderSelected(it)) },
                onSuggestionsExpandedChange = onSuggestionsExpandedChange,
                modifier = if (compact) Modifier.weight(1f) else Modifier.width(SEARCH_BAR_MAX_WIDTH),
            )
            CategoryFilterButton(
                category = state.category,
                onCategorySelect = { sink(PlaceInsertEvent.CategorySelected(it)) },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun PlaceListArea(state: PlaceInsertUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    val onPlaceClick = { place: PlaceCandidate ->
        sink(PlaceInsertEvent.CenterOn(place))
    }
    val onDetailsClick = { place: PlaceCandidate ->
        sink(PlaceInsertEvent.DetailOpened(place))
    }
    val onToggleSelection = { place: PlaceCandidate ->
        sink(PlaceInsertEvent.SelectionToggled(place))
    }

    when (state.listContent) {
        PlaceListContent.NEARBY -> NearbyPlacesList(
            state = state.nearby,
            category = state.category,
            selected = state.selected,
            onPlaceClick = onPlaceClick,
            onDetailsClick = onDetailsClick,
            onToggleSelection = onToggleSelection,
            onShowAllClick = { sink(PlaceInsertEvent.CategorySelected(null)) },
            modifier = modifier,
        )

        PlaceListContent.SEARCH_RESULTS -> SearchResultsList(
            state = state.search,
            selected = state.selected,
            onPlaceClick = onPlaceClick,
            onDetailsClick = onDetailsClick,
            onToggleSelection = onToggleSelection,
            modifier = modifier,
        )

        PlaceListContent.INVENTORY -> SelectedPlacesInventory(
            selected = state.selected,
            onPlaceClick = onPlaceClick,
            onDetailsClick = onDetailsClick,
            onRemove = onToggleSelection,
            modifier = modifier,
        )
    }
}

//region Previews
@PreviewScreenSizes
@Composable
private fun PlaceInsertContentPreview(@PreviewParameter(PlaceInsertPreviewParams::class) state: PlaceInsertUiState) =
    KTravelTheme {
        PlaceInsertContent(state = state)
    }
//endregion Previews
