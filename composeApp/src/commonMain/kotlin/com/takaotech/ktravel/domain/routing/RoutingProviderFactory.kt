package com.takaotech.ktravel.domain.routing

interface RoutingProviderFactory {
    fun getProvider(type: RoutingProviderType): RoutingProvider
}
