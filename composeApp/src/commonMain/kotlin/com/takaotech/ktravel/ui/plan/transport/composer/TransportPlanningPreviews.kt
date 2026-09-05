package com.takaotech.ktravel.ui.plan.transport.composer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.RouteTimeChoice
import com.takaotech.ktravel.presentation.plan.StepUi
import com.takaotech.ktravel.presentation.plan.transport.TransportPlanningUiState
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month

/**
 * The screen as a traveller normally meets it: a remote navigator that answered, and a catalog with
 * something picked in it.
 *
 * The empty state renders as a title and two addresses, which says nothing about how the blocks sit
 * together — so the preview carries a whole catalog, including the profiles that cannot be picked,
 * because the reason lines under them are the part most likely to be laid out wrong.
 *
 * [TransportPlanningUiState.routeOptionsScreen] stays null: the options block is drawn through
 * `CircuitContent`, which needs a Circuit the preview has no way to provide.
 */
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
