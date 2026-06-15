package com.takaotech.ktravel.di

import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.usecase.SavePlaceUseCase
import com.takaotech.ktravel.domain.usecase.SaveTransportStepUseCase
import com.takaotech.ktravel.presentation.planning.PlanningDetailViewModel
import com.takaotech.ktravel.presentation.planning.PlanningViewModel
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides

@GraphExtension(PlanningGraphScope::class)
interface PlanningGraph {
    val planningViewModel: PlanningViewModel
    val travelPlanRepository: TravelPlanRepository
    val savePlaceUseCase: SavePlaceUseCase
    val saveTransportStepUseCase: SaveTransportStepUseCase
    val planningDetailViewModelFactory: PlanningDetailViewModel.Factory

    @GraphExtension.Factory
    fun interface Factory {
        fun create(@Provides @Named("travelId") travelId: String): PlanningGraph
    }
}
