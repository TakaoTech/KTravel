package com.takaotech.ktravel.ui.planner

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.*
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.takaotech.ktravel.PanelHorizontalDivided
import com.takaotech.ktravel.presentation.planner.Place
import com.takaotech.ktravel.presentation.planner.TravelDay
import com.takaotech.ktravel.ui.common.DisruptiveOperationDialog
import com.takaotech.ktravel.ui.common.rememberDisruptiveOperationDialog
import com.takaotech.ktravel.ui.place.PlaceItem
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
@Deprecated("Deprecated")
@Composable
fun PlanningDetailPage(
    steps: ImmutableList<TravelDay.Step>,
    places: PersistentList<Place>,
    modifier: Modifier = Modifier,
    onAddPlaceClick: () -> Unit,
    onDeletePlaceClick: (String) -> Unit,
    onDelePermanentPlaceClick: (String) -> Unit,
    onStepDeleteClicked: (String) -> Unit,
) {
    var isPlaceExpanded by remember { mutableStateOf(true) }

    LazyColumn(modifier = modifier) {
        item {
            AddPlaceButton(
                onClick = onAddPlaceClick
            )
        }

        item {
            TextButton(
                onClick = { isPlaceExpanded = !isPlaceExpanded }
            ) {
                Text("Places")
            }
        }

        if (isPlaceExpanded) {
            items(places) { place ->
                // TODO Add Hours
                // TODO Add image
                PlaceItem(
                    modifier = Modifier
                        .animateItem()
                        .padding(16.dp),
                    name = place.name,
                    onDeleteClick = {
                        onDeletePlaceClick(place.id)
                        // TODO Function for remove place
                        //  Move to all place list or permanent delete?
                    },
                    onPermanentDeleteClick = {
//                        deleteDialogState.show(place.id)
                    }
                )
            }
        }

        item {
            HorizontalDivider()
        }


        itemsIndexed(steps) { index, step ->
            when (step) {
                is TravelDay.Step.Place -> {
                    Row {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    vertical = 12.dp,
                                ),
                        ) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = step.location
                            )
                        }

                        IconButton(
                            modifier = Modifier.padding(top = 8.dp),
                            onClick = {
                                onStepDeleteClicked(step.id)
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.delete),
                                contentDescription = null,
                            )
                        }
                    }

                    if ((index < steps.lastIndex) && steps.getOrNull(index + 1) !is TravelDay.Step.Transport) {
                        TextButton(
                            onClick = {

                            }
                        ) {
                            Text("Add Transport")
                        }
                    }
                }

                is TravelDay.Step.Transport -> {
                    Icon(painter = painterResource(step.type.icon), contentDescription = null)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PlanningDetail2(
    steps: ImmutableList<TravelDay.Step>,
    places: PersistentList<Place>,
    modifier: Modifier = Modifier,
    onNavigationBackClick: () -> Unit,

    onAddPlaceClick: () -> Unit,
    onDeletePlaceClick: (String) -> Unit,
    onDeletePermanentPlaceClick: (String) -> Unit,
    onStepDeleteClicked: (String) -> Unit,
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
                        }
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
        LazyColumn(modifier = Modifier.padding(it)) {
            itemsIndexed(steps) { index, step ->
                when (step) {
                    is TravelDay.Step.Place -> {
                        Row {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(
                                        vertical = 12.dp,
                                    ),
                            ) {
                                Text(
                                    modifier = Modifier.padding(16.dp),
                                    text = step.location
                                )
                            }

                            IconButton(
                                modifier = Modifier.padding(top = 8.dp),
                                onClick = {
                                    onStepDeleteClicked(step.id)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.delete),
                                    contentDescription = null,
                                )
                            }
                        }

                        if ((index < steps.lastIndex) && steps.getOrNull(index + 1) !is TravelDay.Step.Transport) {
                            TextButton(
                                onClick = {
                                    // TODO: Add transport action
                                }
                            ) {
                                Text("Add Transport")
                            }
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

@Composable
private fun SupportingPaneContent(
    places: PersistentList<Place>,
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit,

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

        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                IconButton(onClick = onCloseClick) {
                    Icon(painter = painterResource(Res.drawable.close), contentDescription = null)
                }
            }

            item {
                Text(
                    text = "Places Overview",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                AddPlaceButton(
                    onClick = onAddPlaceClick
                )
            }

            items(places) { place ->
                // TODO Add Hours
                // TODO Add image
                PlaceItem(
                    modifier = Modifier
                        .animateItem()
                        .padding(16.dp),
                    name = place.name,
                    onDeleteClick = {
                        onDeletePlaceClick(place.id)
                        // TODO Function for remove place
                        //  Move to all place list or permanent delete?
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

@Preview(showBackground = true)
@Composable
private fun PlanningDetailPagereview() {
    PlanningDetailPage(
        steps = persistentListOf(
            TravelDay.Step.Place(location = "Roma - Colosseo"),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.BUS),
            TravelDay.Step.Place(location = "Fontana di Trevi"),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.CAR),
            TravelDay.Step.Place(location = "Pantheon"),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.TRAIN),
            TravelDay.Step.Place(location = "Piazza Navona"),
            TravelDay.Step.Place(location = "Piazza Navona"),
        ),
        onAddPlaceClick = {},
        onStepDeleteClicked = {},
        onDeletePlaceClick = {},
        onDelePermanentPlaceClick = {},
        places = persistentListOf()
    )
}