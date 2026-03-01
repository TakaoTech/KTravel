package com.takaotech.ktravel.domain.routing

import com.takaotech.ktravel.domain.routing.model.Routes

class GoogleMapsRoutingProvider : RoutingProvider {
    override suspend fun getRoutes(
        origin: String,
        destination: String,
        settings: RoutingProviderSettings
    ): Routes {
        TODO("Not yet implemented")
    }
}
