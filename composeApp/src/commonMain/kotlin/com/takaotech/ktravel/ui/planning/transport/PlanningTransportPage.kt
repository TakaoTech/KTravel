package com.takaotech.ktravel.ui.planning.transport

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.slack.circuit.foundation.CircuitContent
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.routing.RoutingCatalog
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.RoutingProfileInfo
import com.takaotech.ktravel.domain.routing.RoutingProfileOption
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
import ktravel.composeapp.generated.resources.planning_transport_arrival
import ktravel.composeapp.generated.resources.planning_transport_calculate
import ktravel.composeapp.generated.resources.planning_transport_catalog_failed
import ktravel.composeapp.generated.resources.planning_transport_catalog_loading
import ktravel.composeapp.generated.resources.planning_transport_departure
import ktravel.composeapp.generated.resources.planning_transport_navigator_version
import ktravel.composeapp.generated.resources.planning_transport_profiles_available
import ktravel.composeapp.generated.resources.planning_transport_profiles_declared
import ktravel.composeapp.generated.resources.planning_transport_profiles_none
import ktravel.composeapp.generated.resources.planning_transport_profiles_section
import ktravel.composeapp.generated.resources.planning_transport_retry
import ktravel.composeapp.generated.resources.planning_transport_server_embedded
import ktravel.composeapp.generated.resources.planning_transport_server_embedded_hint
import ktravel.composeapp.generated.resources.planning_transport_server_remote
import ktravel.composeapp.generated.resources.planning_transport_server_section
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
data class PlanningTransportPageNavigation(val dayId: String, val startPlaceId: String, val endPlaceId: String)

@Serializable
class PlanningTransportRoutePreviewPageNavigation(val dayId: String, val startPlaceId: String, val endPlaceId: String)

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
        onCalculateClick = viewModel::calculateTransport,
        onFailureDismiss = viewModel::dismissFailure,
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
    onCalculateClick: () -> Unit,
    onFailureDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnFailureDismiss by rememberUpdatedState(onFailureDismiss)

    val failureMessage = uiState.failure?.let { stringResource(it.label()) }

    LaunchedEffect(failureMessage) {
        val message = failureMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message,
            withDismissAction = true,
            duration = SnackbarDuration.Long,
        )
        currentOnFailureDismiss()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.testTag(PlanningTransportTestTags.FAILURE),
                hostState = snackbarHostState,
            )
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.planning_transport_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigationBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = null,
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
                end = uiState.endPlace,
            )

            NavigatorBlock(
                uiState = uiState,
                onNavigatorChange = onNavigatorChange,
                onRetryCatalog = onRetryCatalog,
            )

            ProfileBlock(
                uiState = uiState,
                onProfileChange = onProfileChange,
                onUseEmbedded = { onNavigatorChange(NavigatorKind.EMBEDDED) },
            )

            // Which controls belong here is not this screen's question. The profile decides the
            // family, the family decides the vehicle's own options, and both arrive as a screen the
            // Circuit factories resolve — so a new provider reaches the traveller without this file
            // gaining a branch.
            uiState.routeOptionsScreen
                ?.takeIf { uiState.selectedOption?.isSelectable == true }
                ?.let { CircuitContent(screen = it, onNavEvent = {}) }
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaceEndpoint(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.planning_transport_departure),
                name = start?.name.orEmpty(),
                icon = Res.drawable.place,
            )

            val composition by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/lottie_paper_airplane.json").decodeToString(),
                )
            }

            val width =
                if (windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
                        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
                    )
                ) {
                    Modifier.width(128.dp)
                } else {
                    Modifier.width(56.dp)
                }

            Image(
                modifier = Modifier.padding(horizontal = 8.dp) then width,
                painter = rememberLottiePainter(
                    composition = composition,
                    iterations = Compottie.IterateForever,
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

            // TODO Add icon for indicate destination
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
                            it,
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
                    catalog.selectable.size,
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
                ) {
                    Text(
                        text = stringResource(Res.string.planning_transport_catalog_failed),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    if (uiState.navigatorKind == NavigatorKind.REMOTE) {
                        TextButton(onClick = onUseEmbedded) {
                            Text(stringResource(Res.string.planning_transport_use_embedded))
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
 * The screen as a traveller normally meets it: a remote navigator that answered, and a catalog with
 * something picked in it.
 *
 * The empty state renders as a title and two addresses, which says nothing about how the blocks sit
 * together — so the preview carries a whole catalog, including the profiles that cannot be picked,
 * because the reason lines under them are the part most likely to be laid out wrong.
 *
 * [PlanningTransportUiState.routeOptionsScreen] stays null: the options block is drawn through
 * `CircuitContent`, which needs a Circuit the preview has no way to provide.
 */
@PreviewScreenSizes
@Composable
private fun PlanningTransportPagePreview() = KTravelTheme {
    PlanningTransportPage(
        modifier = Modifier.fillMaxSize(),
        uiState = PlanningTransportUiState(
            startPlace = StepUi.Place(
                name = "P.za del Colosseo, 1, 00184 Roma RM",
                lat = 0.0,
                lng = 0.0,
            ),
            endPlace = StepUi.Place(name = "Piazza di Trevi, 00187 Roma RM", lat = 0.0, lng = 0.0),
            navigatorKind = NavigatorKind.REMOTE,
            isRemoteConfigured = true,
            catalog = PREVIEW_CATALOG,
            selectedProfileId = PREVIEW_HERE_CAR.id,
            isRequestReady = true,
        ),
        onNavigationBackClick = {},
        onNavigatorChange = {},
        onRetryCatalog = {},
        onProfileChange = {},
        onCalculateClick = {},
        onFailureDismiss = {},
    )
}

private val PREVIEW_HERE_CAR = RoutingProfileInfo(
    id = RoutingProfileId(provider = "here", profile = "routing"),
    displayName = "HERE Routing",
    options = RoutingOptionsSpec.RoutingSingleMode(
        modes = listOf(RoutingMode("CAR"), RoutingMode("TRUCK"), RoutingMode("TAXI")),
        maxAlternatives = 3,
        modesSupportingShortest = setOf(RoutingMode("CAR"), RoutingMode("TRUCK")),
        supportsTolls = true,
    ),
    requiresApiKey = true,
)

private val PREVIEW_HERE_TRANSIT = RoutingProfileInfo(
    id = RoutingProfileId(provider = "here", profile = "transit"),
    displayName = "HERE Transit",
    options = RoutingOptionsSpec.TransitFilter(
        modes = listOf(
            RoutingMode("SUBWAY"),
            RoutingMode("BUS"),
            RoutingMode("REGIONAL_TRAIN"),
            RoutingMode("FERRY"),
        ),
        maxAlternatives = 5,
    ),
    requiresApiKey = true,
)

/** A profile from a newer navigator: no localized name, no description. */
private val PREVIEW_UNKNOWN = RoutingProfileInfo(
    id = RoutingProfileId(provider = "gunzou", profile = "hiking"),
    displayName = "Gunzou Hiking",
    options = RoutingOptionsSpec.RoutingSingleMode(
        modes = listOf(RoutingMode("HIKING")),
        maxAlternatives = 1,
    ),
)

private val PREVIEW_CATALOG = RoutingCatalog(
    options = listOf(
        RoutingProfileOption(PREVIEW_HERE_CAR, ProfileAvailability.Available),
        RoutingProfileOption(PREVIEW_HERE_TRANSIT, ProfileAvailability.MissingApiKey),
        RoutingProfileOption(PREVIEW_UNKNOWN, ProfileAvailability.NotServed),
    ),
    navigatorVersion = null,
    latencyMillis = 87,
)
