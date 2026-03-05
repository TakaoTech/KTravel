package com.takaotech.ktravel.domain.routing

import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

@Factory
class RoutingProviderFactoryImpl : RoutingProviderFactory, KoinComponent {
    override fun getProvider(type: RoutingProviderType): RoutingProvider = when (type) {
        RoutingProviderType.LOCAL -> get<RoutingProvider>(named("LOCAL"))
        RoutingProviderType.HERE -> get<RoutingProvider>(named("HERE"))
        RoutingProviderType.GMAPS -> get<RoutingProvider>(named("GMAPS"))
    }
}
