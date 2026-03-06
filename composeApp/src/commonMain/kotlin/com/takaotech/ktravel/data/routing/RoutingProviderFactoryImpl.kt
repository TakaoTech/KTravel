package com.takaotech.ktravel.data.routing

import com.takaotech.ktravel.domain.routing.RoutingProvider
import com.takaotech.ktravel.domain.routing.RoutingProviderFactory
import com.takaotech.ktravel.domain.routing.RoutingProviderType
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
