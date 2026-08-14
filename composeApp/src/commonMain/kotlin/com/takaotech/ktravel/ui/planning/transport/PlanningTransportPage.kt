package com.takaotech.ktravel.ui.planning.transport

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.ModeSelection
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportUiState
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportViewModel
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.flag
import ktravel.composeapp.generated.resources.place
import ktravel.composeapp.generated.resources.planning_transport_alternatives
import ktravel.composeapp.generated.resources.planning_transport_arrival
import ktravel.composeapp.generated.resources.planning_transport_avoid_tolls
import ktravel.composeapp.generated.resources.planning_transport_avoid_tolls_hint
import ktravel.composeapp.generated.resources.planning_transport_avoid_tolls_unsupported
import ktravel.composeapp.generated.resources.planning_transport_calculate
import ktravel.composeapp.generated.resources.planning_transport_catalog_failed
import ktravel.composeapp.generated.resources.planning_transport_catalog_loading
import ktravel.composeapp.generated.resources.planning_transport_departure
import ktravel.composeapp.generated.resources.planning_transport_mode_filter_hint
import ktravel.composeapp.generated.resources.planning_transport_mode_filter_section
import ktravel.composeapp.generated.resources.planning_transport_mode_section
import ktravel.composeapp.generated.resources.planning_transport_navigator_version
import ktravel.composeapp.generated.resources.planning_transport_options_section
import ktravel.composeapp.generated.resources.planning_transport_profiles_available
import ktravel.composeapp.generated.resources.planning_transport_profiles_declared
import ktravel.composeapp.generated.resources.planning_transport_profiles_none
import ktravel.composeapp.generated.resources.planning_transport_profiles_section
import ktravel.composeapp.generated.resources.planning_transport_retry
import ktravel.composeapp.generated.resources.planning_transport_server_embedded
import ktravel.composeapp.generated.resources.planning_transport_server_embedded_hint
import ktravel.composeapp.generated.resources.planning_transport_server_remote
import ktravel.composeapp.generated.resources.planning_transport_server_section
import ktravel.composeapp.generated.resources.planning_transport_shortest
import ktravel.composeapp.generated.resources.planning_transport_shortest_hint
import ktravel.composeapp.generated.resources.planning_transport_shortest_unsupported
import ktravel.composeapp.generated.resources.planning_transport_status_checking
import ktravel.composeapp.generated.resources.planning_transport_status_offline
import ktravel.composeapp.generated.resources.planning_transport_status_online
import ktravel.composeapp.generated.resources.planning_transport_title
import ktravel.composeapp.generated.resources.planning_transport_use_embedded
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Serializable
object PlanningTransportNavigation

@Serializable
data class PlanningTransportPageNavigation(
    val dayId: String,
    val startPlaceId: String,
    val endPlaceId: String
)

@Serializable
class PlanningTransportRoutePreviewPageNavigation(
    val dayId: String,
    val startPlaceId: String,
    val endPlaceId: String
)

@Composable
fun PlanningTransportPage(
    viewModel: PlanningTransportViewModel,
    modifier: Modifier = Modifier,
    onNavigationBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlanningTransportPage(
        modifier = modifier,
        uiState = uiState,
        onNavigationBackClick = onNavigationBackClick,
        onNavigatorChange = viewModel::selectNavigator,
        onRetryCatalog = viewModel::retryCatalog,
        onProfileChange = viewModel::selectProfile,
        onModeChange = viewModel::selectMode,
        onModeFilterToggle = viewModel::toggleModeFilter,
        onAlternativesChange = viewModel::setAlternatives,
        onAvoidTollsChange = viewModel::setAvoidTolls,
        onShortestChange = viewModel::setShortestDistance,
        onCalculateClick = viewModel::calculateTransport,
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanningTransportPage(
    uiState: PlanningTransportUiState,
    onNavigationBackClick: () -> Unit,
    onNavigatorChange: (NavigatorKind) -> Unit,
    onRetryCatalog: () -> Unit,
    onProfileChange: (RoutingProfileId) -> Unit,
    onModeChange: (RoutingMode) -> Unit,
    onModeFilterToggle: (RoutingMode) -> Unit,
    onAlternativesChange: (Int) -> Unit,
    onAvoidTollsChange: (Boolean) -> Unit,
    onShortestChange: (Boolean) -> Unit,
    onCalculateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.planning_transport_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigationBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth()
                            .testTag(PlanningTransportTestTags.CALCULATE),
                        onClick = onCalculateClick,
                        enabled = uiState.canCalculate,
                    ) {
                        Text(stringResource(Res.string.planning_transport_calculate))
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            LegEndpoints(
                modifier = Modifier.fillMaxWidth(),
                start = uiState.startPlace,
                end = uiState.endPlace
            )

            NavigatorBlock(
                uiState = uiState,
                onNavigatorChange = onNavigatorChange,
                onRetryCatalog = onRetryCatalog,
            )

            ProfileBlock(
                uiState = uiState,
                onProfileChange = onProfileChange,
                onRetryCatalog = onRetryCatalog,
                onUseEmbedded = { onNavigatorChange(NavigatorKind.EMBEDDED) },
            )

            ModeBlock(
                uiState = uiState,
                onModeChange = onModeChange,
                onModeFilterToggle = onModeFilterToggle,
                onAvoidTollsChange = onAvoidTollsChange,
            )

            OptionsBlock(
                uiState = uiState,
                onAlternativesChange = onAlternativesChange,
                onShortestChange = onShortestChange,
            )

            uiState.failure?.let { failure ->
                Surface(
                    modifier = Modifier.fillMaxWidth().testTag(PlanningTransportTestTags.FAILURE),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        text = stringResource(failure.label()),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

/**
 * The two places, with the paper aeroplane still flying between them.
 *
 * The animation is the one thing on this screen that predates the redesign and stays: it is what the
 * leg reads as, and a static connector would be a plainer screen for no reason.
 */
@Composable
private fun LegEndpoints(start: StepUi.Place?, end: StepUi.Place?, modifier: Modifier = Modifier) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()

    if (windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        Row(
            modifier = modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaceEndpoint(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.planning_transport_departure),
                name = start?.name.orEmpty(),
                icon = Res.drawable.place,
            )

            val composition by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/lottie_paper_airplane.json").decodeToString()
                )
            }

            val width =
                if (windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
                    Modifier.width(128.dp)
                } else {
                    Modifier.width(56.dp)
                }

            Image(
                modifier = Modifier.padding(horizontal = 8.dp) then width,
                painter = rememberLottiePainter(
                    composition = composition,
                    iterations = Compottie.IterateForever
                ),
                contentDescription = null,
            )

            PlaceEndpoint(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.planning_transport_arrival),
                name = end?.name.orEmpty(),
                icon = Res.drawable.flag,
            )
        }
    } else {
        Column(modifier = modifier) {
            PlaceEndpoint(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.planning_transport_departure),
                name = start?.name.orEmpty(),
                icon = Res.drawable.place,
            )

            //TODO Add icon for indicate destination
            Spacer(modifier = Modifier.height(16.dp))

            PlaceEndpoint(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.planning_transport_arrival),
                name = end?.name.orEmpty(),
                icon = Res.drawable.flag,
            )
        }

    }
}

/** Which navigator computes this route, and whether it is answering. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigatorBlock(
    uiState: PlanningTransportUiState,
    onNavigatorChange: (NavigatorKind) -> Unit,
    onRetryCatalog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(stringResource(Res.string.planning_transport_server_section))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                modifier = Modifier.testTag(PlanningTransportTestTags.NAVIGATOR_EMBEDDED),
                selected = uiState.navigatorKind == NavigatorKind.EMBEDDED,
                onClick = { onNavigatorChange(NavigatorKind.EMBEDDED) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text(stringResource(Res.string.planning_transport_server_embedded))
            }
            SegmentedButton(
                modifier = Modifier.testTag(PlanningTransportTestTags.NAVIGATOR_REMOTE),
                selected = uiState.navigatorKind == NavigatorKind.REMOTE,
                onClick = { onNavigatorChange(NavigatorKind.REMOTE) },
                // Offered only when there is an address to call: switching to a navigator that was
                // never configured would fail in a way this screen cannot help with.
                enabled = uiState.isRemoteConfigured,
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text(stringResource(Res.string.planning_transport_server_remote))
            }
        }

        TransportCard(modifier = Modifier.padding(top = 10.dp)) {
            if (uiState.navigatorKind == NavigatorKind.REMOTE) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ReachabilityPill(
                        isChecking = uiState.isCatalogLoading,
                        isReachable = uiState.catalog?.isReachable == true,
                        latencyMillis = uiState.catalog?.latencyMillis,
                        checkingText = stringResource(Res.string.planning_transport_status_checking),
                        reachableText = stringResource(Res.string.planning_transport_status_online),
                        unreachableText = stringResource(Res.string.planning_transport_status_offline),
                    )
                    TextButton(
                        modifier = Modifier.testTag(PlanningTransportTestTags.RETRY_CATALOG),
                        onClick = onRetryCatalog,
                    ) {
                        Text(stringResource(Res.string.planning_transport_retry))
                    }
                }
            } else {
                Text(
                    text = stringResource(Res.string.planning_transport_server_embedded_hint),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            val version = uiState.catalog?.navigatorVersion
            val declared = uiState.catalog?.options?.size ?: 0
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = listOfNotNull(
                    version?.let {
                        stringResource(
                            Res.string.planning_transport_navigator_version,
                            it
                        )
                    },
                    stringResource(Res.string.planning_transport_profiles_declared, declared),
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Which profile of which provider, including the ones that cannot be picked and why. */
@Composable
private fun ProfileBlock(
    uiState: PlanningTransportUiState,
    onProfileChange: (RoutingProfileId) -> Unit,
    onRetryCatalog: () -> Unit,
    onUseEmbedded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val catalog = uiState.catalog
    val unreachable = catalog != null && !catalog.isReachable

    Column(modifier = modifier) {
        SectionLabel(
            text = stringResource(Res.string.planning_transport_profiles_section),
            trailing = when {
                catalog == null || unreachable -> stringResource(Res.string.planning_transport_profiles_none)
                else -> stringResource(
                    Res.string.planning_transport_profiles_available,
                    catalog.selectable.size
                )
            },
        )

        when {
            uiState.isCatalogLoading && catalog == null -> Text(
                text = stringResource(Res.string.planning_transport_catalog_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            unreachable -> Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.planning_transport_catalog_failed),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onRetryCatalog) {
                            Text(stringResource(Res.string.planning_transport_retry))
                        }
                        if (uiState.navigatorKind == NavigatorKind.REMOTE) {
                            TextButton(onClick = onUseEmbedded) {
                                Text(stringResource(Res.string.planning_transport_use_embedded))
                            }
                        }
                    }
                }
            }

            catalog != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                catalog.options.forEach { option ->
                    ProfileRow(
                        option = option,
                        selected = option.profile.id == uiState.selectedProfileId,
                        onSelect = { onProfileChange(option.profile.id) },
                    )
                }
            }
        }
    }
}

/**
 * The modes of the profile that is selected, and nothing else.
 *
 * Which control this is depends on the profile, not on the provider: a road API takes one vehicle and
 * a transit API takes a set of acceptable ones, so the same block renders a choice or a filter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeBlock(
    uiState: PlanningTransportUiState,
    onModeChange: (RoutingMode) -> Unit,
    onModeFilterToggle: (RoutingMode) -> Unit,
    onAvoidTollsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = uiState.selectedProfile ?: return
    val isFilter = profile.modeSelection == ModeSelection.FILTER

    Column(modifier = modifier) {
        SectionLabel(
            stringResource(
                if (isFilter) {
                    Res.string.planning_transport_mode_filter_section
                } else {
                    Res.string.planning_transport_mode_section
                },
            ),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profile.modes.forEach { mode ->
                ModeChip(
                    mode = mode,
                    selected = if (isFilter) mode in uiState.modeFilter else mode == uiState.selectedMode,
                    enabled = uiState.selectedOption?.isSelectable == true,
                    onClick = { if (isFilter) onModeFilterToggle(mode) else onModeChange(mode) },
                )
            }
        }

        if (isFilter) {
            // An empty filter is no restriction, which is not the same as nothing being allowed.
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = stringResource(Res.string.planning_transport_mode_filter_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            ToggleRow(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .testTag(PlanningTransportTestTags.AVOID_TOLLS),
                title = stringResource(Res.string.planning_transport_avoid_tolls),
                subtitle = if (profile.supportsTolls) {
                    stringResource(Res.string.planning_transport_avoid_tolls_hint)
                } else {
                    stringResource(Res.string.planning_transport_avoid_tolls_unsupported)
                },
                checked = uiState.avoidTolls,
                enabled = profile.supportsTolls,
                onCheckedChange = onAvoidTollsChange,
            )
        }
    }
}

/** How many routes, and what to optimize them for. */
@Composable
private fun OptionsBlock(
    uiState: PlanningTransportUiState,
    onAlternativesChange: (Int) -> Unit,
    onShortestChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = uiState.selectedProfile ?: return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(stringResource(Res.string.planning_transport_options_section))

        Text(
            text = stringResource(Res.string.planning_transport_alternatives, uiState.alternatives),
            style = MaterialTheme.typography.bodyMedium,
        )

        Slider(
            value = uiState.alternatives.toFloat(),
            onValueChange = { onAlternativesChange(it.toInt()) },
            valueRange = 1f..profile.maxAlternatives.toFloat().coerceAtLeast(1f),
            steps = (profile.maxAlternatives - 2).coerceAtLeast(0),
        )

        if (profile.modeSelection == ModeSelection.SINGLE) {
            ToggleRow(
                modifier = Modifier.testTag(PlanningTransportTestTags.SHORTEST),
                title = stringResource(Res.string.planning_transport_shortest),
                subtitle = if (uiState.canOptimizeForDistance) {
                    stringResource(Res.string.planning_transport_shortest_hint)
                } else {
                    stringResource(Res.string.planning_transport_shortest_unsupported)
                },
                checked = uiState.shortestDistance,
                enabled = uiState.canOptimizeForDistance,
                onCheckedChange = onShortestChange,
            )
        }
    }
}

@PreviewScreenSizes
@Composable
private fun PlanningTransportPagePreview() = KTravelTheme {
    PlanningTransportPage(
        uiState = PlanningTransportUiState(
            startPlace = StepUi.Place(
                name = "P.za del Colosseo, 1, 00184 Roma RM",
                lat = 0.0,
                lng = 0.0
            ),
            endPlace = StepUi.Place(name = "Piazza di Trevi, 00187 Roma RM", lat = 0.0, lng = 0.0),
        ),
        onNavigationBackClick = {},
        onNavigatorChange = {},
        onRetryCatalog = {},
        onProfileChange = {},
        onModeChange = {},
        onModeFilterToggle = {},
        onAlternativesChange = {},
        onAvoidTollsChange = {},
        onShortestChange = {},
        onCalculateClick = {},
    )
}
