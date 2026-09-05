package com.takaotech.ktravel.di

import com.takaotech.ktravel.data.navigator.NavigatorTargetResolver
import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.RoutingService
import com.takaotech.ktravel.domain.usecase.SavePlaceUseCase
import com.takaotech.ktravel.domain.usecase.SaveTransportStepUseCase
import com.takaotech.ktravel.presentation.plan.transport.RouteAnswerDraft
import com.takaotech.ktravel.presentation.plan.transport.RouteOptionsDraft
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

    /**
     * The route request the transport screen is assembling.
     *
     * Shared between the options presenters, which write it, and the screen's view model, which
     * sends it. Scoped to the plan rather than to the composition so it survives the trip to the
     * route preview and back.
     */
    val routeOptionsDraft: RouteOptionsDraft

    /**
     * The answer that request produced.
     *
     * Scoped to the plan for the same reason as the request, and because the composer and the route
     * preview are two destinations reading one answer — see [RouteAnswerDraft].
     */
    val routeAnswerDraft: RouteAnswerDraft

    @GraphExtension.Factory
    fun interface Factory {
        fun create(@Provides @Named("travelId") travelId: String): PlanningGraph
    }
}
