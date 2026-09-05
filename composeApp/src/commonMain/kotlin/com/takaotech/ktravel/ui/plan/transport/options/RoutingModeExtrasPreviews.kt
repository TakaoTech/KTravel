package com.takaotech.ktravel.ui.plan.transport.options

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.persistentSetOf

@Preview
@Composable
private fun RoutingModeExtrasContentPreview() = KTravelTheme {
    RoutingModeExtrasContent(
        avoidable = persistentSetOf(
            RouteFeature.TOLL_ROAD,
            RouteFeature.CONTROLLED_ACCESS_HIGHWAY,
            RouteFeature.FERRY,
            RouteFeature.TUNNEL,
            RouteFeature.DIRT_ROAD,
            RouteFeature.CAR_SHUTTLE_TRAIN,
        ),
        avoided = persistentSetOf(RouteFeature.FERRY),
        tollsUnsupported = false,
        supportsShortest = true,
        shortestDistance = false,
        onAvoidClick = {},
        onShortestChange = {},
    )
}

@Preview
@Composable
private fun RoutingModeExtrasContentScooterPreview() = KTravelTheme {
    RoutingModeExtrasContent(
        avoidable = persistentSetOf(RouteFeature.FERRY, RouteFeature.TUNNEL, RouteFeature.DIRT_ROAD),
        avoided = persistentSetOf(),
        tollsUnsupported = false,
        // A scooter is one of the vehicles HERE refuses to optimize for distance.
        supportsShortest = false,
        shortestDistance = false,
        onAvoidClick = {},
        onShortestChange = {},
    )
}
