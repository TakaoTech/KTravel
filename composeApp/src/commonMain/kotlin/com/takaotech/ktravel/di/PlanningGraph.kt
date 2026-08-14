package com.takaotech.ktravel.di

import com.takaotech.ktravel.data.navigator.NavigatorTargetResolver
import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.RoutingService
import com.takaotech.ktravel.domain.usecase.SavePlaceUseCase
import com.takaotech.ktravel.domain.usecase.SaveTransportStepUseCase
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides

@GraphExtension(PlanningGraphScope::class)
interface PlanningGraph {
    val travelPlanRepository: TravelPlanRepository

    /** Preferences of this plan: the HERE API key today, the user's plan-wide settings tomorrow. */
    val settingsRepository: SettingsRepository
    val savePlaceUseCase: SavePlaceUseCase
    val saveTransportStepUseCase: SaveTransportStepUseCase

    /**
     * Exposed here, not injected directly, because it reads this plan's API key and this plan's
     * choice of navigator.
     */
    val routingService: RoutingService

    /** Which navigator this plan starts on, and whether the remote one can be offered at all. */
    val navigatorTargetResolver: NavigatorTargetResolver

    @GraphExtension.Factory
    fun interface Factory {
        fun create(@Provides @Named("travelId") travelId: String): PlanningGraph
    }
}
