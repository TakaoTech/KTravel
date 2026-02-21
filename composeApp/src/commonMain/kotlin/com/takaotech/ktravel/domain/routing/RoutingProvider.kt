package com.takaotech.ktravel.domain.routing

import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_transport_provider_name_gmaps
import ktravel.composeapp.generated.resources.planning_transport_provider_name_here
import ktravel.composeapp.generated.resources.planning_transport_provider_name_local
import org.jetbrains.compose.resources.StringResource

interface RoutingProvider

enum class RoutingProviderType(val stringName: StringResource) {
    LOCAL(Res.string.planning_transport_provider_name_local),
    HERE(Res.string.planning_transport_provider_name_here),
    GMAPS(Res.string.planning_transport_provider_name_gmaps)
}
