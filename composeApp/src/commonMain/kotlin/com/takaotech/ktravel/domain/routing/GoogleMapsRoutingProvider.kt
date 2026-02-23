package com.takaotech.ktravel.domain.routing

class GoogleMapsRoutingProvider : RoutingProvider {
    override suspend fun getRoutes(
        origin: String,
        destination: String,
        settings: RoutingProviderSettings
    ) {
        TODO("Not yet implemented")
    }
}
