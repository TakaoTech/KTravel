package com.takaotech.ktravel.di

import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.RoutingProviderFactory
import com.takaotech.ktravel.domain.usecase.SavePlaceUseCase
import com.takaotech.ktravel.domain.usecase.SaveTransportStepUseCase
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides

@GraphExtension(PlanningGraphScope::class)
interface PlanningGraph {
    val travelPlanRepository: TravelPlanRepository
    val savePlaceUseCase: SavePlaceUseCase
    val saveTransportStepUseCase: SaveTransportStepUseCase

    /** Exposed here, not injected directly, because the HERE provider needs this plan's API key. */
    val routingProviderFactory: RoutingProviderFactory

    @GraphExtension.Factory
    fun interface Factory {
        fun create(@Provides @Named("travelId") travelId: String): PlanningGraph
    }
}
