package com.takaotech.ktravel.data.routing

import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.routing.RoutingProvider
import com.takaotech.ktravel.domain.routing.RoutingProviderFactory
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named

@ContributesBinding(AppScope::class)
@Inject
class RoutingProviderFactoryImpl(
    @param:Named("LOCAL") private val localProvider: () -> RoutingProvider,
    @param:Named("HERE") private val hereProvider: () -> RoutingProvider,
    @param:Named("GMAPS") private val gmapsProvider: () -> RoutingProvider,
) : RoutingProviderFactory {
    override fun getProvider(type: RoutingProviderType): RoutingProvider = when (type) {
        RoutingProviderType.LOCAL -> localProvider.invoke()
        RoutingProviderType.HERE -> hereProvider.invoke()
        RoutingProviderType.GMAPS -> gmapsProvider.invoke()
    }
}
