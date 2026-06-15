package com.takaotech.ktravel.data.routing

import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.routing.RoutingProvider
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.ktravel.domain.routing.model.Routes
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named

@Named("LOCAL")
@ContributesBinding(AppScope::class)
@Inject
class LocalRoutingProvider : RoutingProvider {
    override suspend fun getRoutes(
        origin: String,
        destination: String,
        settings: RoutingProviderSettings
    ): Routes {
        TODO("Not yet implemented")
    }
}
