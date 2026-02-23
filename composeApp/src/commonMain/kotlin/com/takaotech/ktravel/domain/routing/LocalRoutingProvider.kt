package com.takaotech.ktravel.domain.routing

class LocalRoutingProvider : RoutingProvider {
    override suspend fun getRoutes(
        origin: String,
        destination: String,
        settings: RoutingProviderSettings
    ) {
        TODO("Not yet implemented")
    }
}
