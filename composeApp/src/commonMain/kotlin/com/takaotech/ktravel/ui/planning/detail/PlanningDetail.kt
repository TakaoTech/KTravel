package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.*
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.takaotech.ktravel.PanelHorizontalDivided
import com.takaotech.ktravel.presentation.planning.Place
import com.takaotech.ktravel.presentation.planning.TravelDay
import com.takaotech.ktravel.ui.common.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.common.rememberDisruptiveOperationDialog
import com.takaotech.ktravel.ui.place.PlaceItem
import com.takaotech.ktravel.ui.planning.common.AddPlaceButton
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Serializable
data class PlanningDetailPageNavigation(val id: String)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PlanningDetailPage(
    steps: ImmutableList<TravelDay.Step>,
    places: PersistentList<Place>,
    modifier: Modifier = Modifier,
    onNavigationBackClick: () -> Unit,

    onAddPlaceClick: () -> Unit,
    onMovePlaceToList: (String) -> Unit,
    onDeletePlaceClick: (String) -> Unit,
    onDeletePermanentPlaceClick: (String) -> Unit,
    onStepDeleteClicked: (String) -> Unit,

    onStepMoveUp: (String) -> Unit,
    onStepMoveDown: (String) -> Unit,

    onTransportAddClick: (startId: String, endId: String) -> Unit,
) {
    val coroutine = rememberCoroutineScope()
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()

    val isExpandedNavigation =
        windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val directive = calculatePaneScaffoldDirective(windowAdaptiveInfo)

//    val directive = calculatePaneScaffoldDirective(windowAdaptiveInfo).copy(
//        horizontalPartitionSpacerSize = 24.dp // Spazio tra i pannelli
//    )
//    val navigator = rememberSupportingPaneScaffoldNavigator(scaffoldDirective = directive)
    val navigator = rememberSupportingPaneScaffoldNavigator(scaffoldDirective = directive)
    val paneExpansionState: PaneExpansionState = rememberPaneExpansionState(keyProvider = navigator.scaffoldValue)

    val deleteDialogState = rememberDisruptiveOperationDialog<String> { placeId ->
        onDeletePermanentPlaceClick(placeId)
    }

    DisruptiveOperationDialog(
        state = deleteDialogState
    )

    if (isExpandedNavigation) {
        // Expanded screen layout
        PanelHorizontalDivided(
            scaffoldNavigator = navigator,
            paneExpansionState = paneExpansionState,
            modifier = modifier,
            mainPane = {
                AnimatedPane {
                    MainPaneContent(
                        steps = steps,
                        onStepDeleteClicked = onStepDeleteClicked,
                        onNavigationBackClick = onNavigationBackClick,
                        onPlaceMenuClicked = {
                            coroutine.launch {
                                navigator.navigateTo(SupportingPaneScaffoldRole.Supporting)
                            }
                        },
                        onStepMoveUp = onStepMoveUp,
                        onStepMoveDown = onStepMoveDown,
                        onTransportAddClick = onTransportAddClick,
                    )
                }
            },
            supportingPane = {
                AnimatedPane {
                    SupportingPaneContent(
                        modifier = Modifier.fillMaxSize(),
                        places = places,
                        onPermanentDeleteClick = { placeId -> deleteDialogState.show(placeId) },
                        onDeletePlaceClick = onDeletePlaceClick,
                        onAddPlaceClick = onAddPlaceClick,
                        onMovePlaceToList = onMovePlaceToList,
                        onCloseClick = {
                            coroutine.launch {
                                navigator.navigateBack()
                            }
                        }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainPaneContent(
    steps: ImmutableList<TravelDay.Step>,
    onStepDeleteClicked: (String) -> Unit,
    onNavigationBackClick: () -> Unit,
    onPlaceMenuClicked: () -> Unit,
    onStepMoveUp: (String) -> Unit,
    onStepMoveDown: (String) -> Unit,
    onTransportAddClick: (startId: String, endId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    IconButton(onClick = onNavigationBackClick) {
                        Icon(painter = painterResource(Res.drawable.arrow_back), contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onPlaceMenuClicked) {
                        Icon(painter = painterResource(Res.drawable.flight), contentDescription = null)
                    }
                }
            )
        }
    ) {
        if (steps.isEmpty()) {
            // TODO Add empty view
        } else {
            LazyColumn(modifier = Modifier.padding(it)) {
                itemsIndexed(steps) { index, step ->
                    when (step) {
                        is TravelDay.Step.Place -> {
                            TravelStepPlace(
                                step = step,
                                onStepDeleteClicked = onStepDeleteClicked,
                                onStepMoveUp = onStepMoveUp,
                                onStepMoveDown = onStepMoveDown
                            )

                            if ((index < steps.lastIndex) && steps.getOrNull(index + 1) !is TravelDay.Step.Transport) {
                                TravelTransportStepAdd(onClick = {
                                    onTransportAddClick(
                                        step.id,
                                        steps.get(index + 1).id
                                    )
                                })
                            }
                        }

                        is TravelDay.Step.Transport -> {
                            Icon(painter = painterResource(step.type.icon), contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportingPaneContent(
    places: PersistentList<Place>,
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit,
    onMovePlaceToList: (String) -> Unit,
    onPermanentDeleteClick: (String) -> Unit,
    onDeletePlaceClick: (String) -> Unit,
    onAddPlaceClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
    ) {
        val deleteDialogState = rememberDisruptiveOperationDialog<String> { placeId ->
            onPermanentDeleteClick(placeId)
        }

        DisruptiveOperationDialog(
            state = deleteDialogState
        )

        Column {
            IconButton(
                onClick = onCloseClick
            ) {
                Icon(painter = painterResource(Res.drawable.close), contentDescription = null)
            }

            Text(
                text = "Places Overview",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
                    .padding(horizontal = 8.dp)
            )

            AddPlaceButton(
                onClick = onAddPlaceClick
            )

            LazyColumn(
                contentPadding = PaddingValues(8.dp),
            ) {
                items(items = places, key = { it.id }) { place ->
                    // TODO Add Hours
                    // TODO Add image
                    PlaceItem(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        name = place.name,
                        onDeleteClick = {
                            onDeletePlaceClick(place.id)
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    onMovePlaceToList(place.id)
                                }
                            ) {
                                Icon(painter = painterResource(Res.drawable.add), contentDescription = null)
                            }
                        },
                        onPermanentDeleteClick = {
                            deleteDialogState.show(place.id)
                        }
                    )
                }

                if (places.isEmpty()) {
                    item {
                        Text(
                            text = "No places added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun PlanningDetailPagereview() {
    PlanningDetailPage(
        steps = persistentListOf(
            TravelDay.Step.Place(location = "Roma - Colosseo", lat = 0.0, lng = 0.0),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.BUS),
            TravelDay.Step.Place(location = "Fontana di Trevi", lat = 0.0, lng = 0.0),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.CAR),
            TravelDay.Step.Place(location = "Pantheon", lat = 0.0, lng = 0.0),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.TRAIN),
            TravelDay.Step.Place(location = "Piazza Navona", lat = 0.0, lng = 0.0),
            TravelDay.Step.Place(location = "Piazza Navona", lat = 0.0, lng = 0.0),
        ),
        onAddPlaceClick = {},
        onStepDeleteClicked = {},
        onDeletePlaceClick = {},
        places = persistentListOf(),
        onNavigationBackClick = {},
        onDeletePermanentPlaceClick = {},
        onMovePlaceToList = {},
        onStepMoveDown = {},
        onStepMoveUp = {},
        onTransportAddClick = { _, _ -> }
    )
}
