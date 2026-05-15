package com.takaotech.ktravel.domain.usecase

import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

@Scope(name = "PlanningScope")
@Scoped
class SavePlaceUseCase(
    private val repository: TravelPlanRepository
) {
    open suspend operator fun invoke(name: String, lat: Double, lng: Double, dayId: String?) {
        val place = PlaceDomain(name = name, lat = lat, lng = lng)
        repository.savePlace(place, dayId)
    }
}
