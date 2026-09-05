package com.takaotech.ktravel.ui.plan.transport.routepreview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.takaotech.ktravel.domain.routing.model.RouteResult

/**
 * The computed itineraries, rendered as what they are.
 *
 * A road answer and a scheduled-services answer are two different screens, and which one to draw
 * follows from the result alone: it is a rendering decision, not a navigation one, which is why it
 * lives here rather than in the destination that hosts it.
 *
 * @param result The computed itineraries, or null while none has been computed yet.
 * @param selectedIndex The index of the alternative currently being shown.
 * @param onSelectionChange Called with the index of the alternative the user moved to.
 * @param onConfirm Called when the shown alternative is the one to file.
 * @param onNavigationBackClick Called when the user leaves the preview.
 * @param modifier The modifier applied to whichever screen is drawn.
 */
@Composable
fun RoutePreviewPage(
    result: RouteResult?,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onNavigationBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (result) {
        null -> Unit

        is RouteResult.Routing -> RoutingRoutePreviewPage(
            routes = result.routes,
            selectedRouteIndex = selectedIndex,
            onRouteChange = onSelectionChange,
            onRouteConfirm = onConfirm,
            onNavigationBackClick = onNavigationBackClick,
            modifier = modifier,
        )

        is RouteResult.Transit -> TransitJourneyPreviewPage(
            journeys = result.journeys,
            selectedJourneyIndex = selectedIndex,
            onJourneyChange = onSelectionChange,
            onJourneyConfirm = onConfirm,
            onNavigationBackClick = onNavigationBackClick,
            modifier = modifier,
        )
    }
}
