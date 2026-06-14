package com.takaotech.ktravel.data.routing

import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.routing.RoutingProvider
import com.takaotech.ktravel.domain.routing.RoutingProviderFactory
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provider

@ContributesBinding(AppScope::class)
class RoutingProviderFactoryImpl @Inject constructor(
    @Named("LOCAL") private val localProvider: Provider<RoutingProvider>,
    @Named("HERE") private val hereProvider: Provider<RoutingProvider>,
    @Named("GMAPS") private val gmapsProvider: Provider<RoutingProvider>,
) : RoutingProviderFactory {
    override fun getProvider(type: RoutingProviderType): RoutingProvider = when (type) {
        RoutingProviderType.LOCAL -> localProvider.invoke()
        RoutingProviderType.HERE -> hereProvider.invoke()
        RoutingProviderType.GMAPS -> gmapsProvider.invoke()
    }
}
