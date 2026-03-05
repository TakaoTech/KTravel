package com.takaotech.ktravel.domain.routing

import com.takaotech.ktravel.domain.routing.model.Routes
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
@Named("LOCAL")
class LocalRoutingProvider : RoutingProvider {
    override suspend fun getRoutes(
        origin: String,
        destination: String,
        settings: RoutingProviderSettings
    ): Routes {
        TODO("Not yet implemented")
    }
}
