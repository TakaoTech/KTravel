package com.takaotech.ktravel.ui.plan.transport.composer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.slack.circuit.foundation.CircuitContent
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.RouteTimeChoice
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.presentation.plan.StepUi
import com.takaotech.ktravel.presentation.plan.transport.RouteTimeMode
import com.takaotech.ktravel.presentation.plan.transport.TransportPlanningUiState
import com.takaotech.ktravel.ui.plan.transport.component.TransportPlanningTestTags
import com.takaotech.ktravel.ui.plan.transport.composer.component.NavigatorBlock
import com.takaotech.ktravel.ui.plan.transport.composer.component.ProfileBlock
import com.takaotech.ktravel.ui.plan.transport.composer.component.StepEndpoints
import com.takaotech.ktravel.ui.shared.format.label
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.planning_transport_calculate
import ktravel.composeapp.generated.resources.planning_transport_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransportPlanningContent(
    uiState: TransportPlanningUiState,
    onNavigationBackClick: () -> Unit,
    onNavigatorChange: (NavigatorKind) -> Unit,
    onRetryCatalog: () -> Unit,
    onProfileChange: (RoutingProfileId) -> Unit,
    onTimeModeChange: (RouteTimeMode) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
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
                modifier = Modifier.testTag(TransportPlanningTestTags.FAILURE),
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
                            .testTag(TransportPlanningTestTags.CALCULATE),
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
            StepEndpoints(
                modifier = Modifier.fillMaxWidth(),
                start = uiState.startPlace,
                end = uiState.endPlace,
            )

            RouteTimeSection(
                choice = uiState.timeChoice,
                dayDate = uiState.dayDate,
                supportsArriveBy = uiState.supportsArriveBy,
                onModeChange = onTimeModeChange,
                onTimeChange = onTimeChange,
            )

            NavigatorBlock(
                uiState = uiState,
                onNavigatorChange = onNavigatorChange,
                onRetryCatalog = onRetryCatalog,
            )

            ProfileBlock(
                uiState = uiState,
                onProfileChange = onProfileChange,
                onUseEmb = { onNavigatorChange(NavigatorKind.EMBEDDED) },
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

//region Previews

// The screen as a traveller normally meets it: a remote navigator that answered, and a catalog with
// something picked in it. The empty state renders as a title and two addresses, which says nothing
// about how the blocks sit together — so the preview carries a whole catalog, including the profiles
// that cannot be picked, because the reason lines under them are the part most likely to be laid
// out wrong. TransportPlanningUiState.routeOptionsScreen stays null: the options block is drawn
// through CircuitContent, which needs a Circuit the preview has no way to provide.
@PreviewScreenSizes
@Composable
private fun TransportPlanningPagePreview() = KTravelTheme {
    TransportPlanningContent(
        modifier = Modifier.fillMaxSize(),
        uiState = TransportPlanningUiState(
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
            timeChoice = RouteTimeChoice.DepartAt(LocalTime(hour = 10, minute = 30)),
            dayDate = LocalDate(year = 2026, month = Month.MAY, day = 18),
        ),
        onNavigationBackClick = {},
        onNavigatorChange = {},
        onRetryCatalog = {},
        onProfileChange = {},
        onTimeModeChange = {},
        onTimeChange = {},
        onCalculateClick = {},
        onFailureDismiss = {},
    )
}
//endregion Previews
