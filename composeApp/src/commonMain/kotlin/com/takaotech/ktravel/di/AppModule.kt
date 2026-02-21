package com.takaotech.ktravel.di

import com.takaotech.ktravel.domain.routing.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.takaotech.ktravel")
class AppModule

fun appModule() = listOf(
    AppModule().module,
    routingProviderModule
)

private val routingProviderModule = module {
    single(named(RoutingProviderType.LOCAL)) { LocalRoutingProvider() } bind RoutingProvider::class
    single(named(RoutingProviderType.HERE)) { HereRoutingProvider(get()) } bind RoutingProvider::class
    single(named(RoutingProviderType.GMAPS)) { GoogleMapsRoutingProvider() } bind RoutingProvider::class
}