package com.takaotech.ktravel.domain.usecase

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(PlanningGraphScope::class)
@OpenForMokkery
class SavePlaceUseCase @Inject constructor(
    private val repository: TravelPlanRepository
) {
    open suspend operator fun invoke(name: String, lat: Double, lng: Double, dayId: String?) {
        val place = PlaceDomain(name = name, lat = lat, lng = lng)
        repository.savePlace(place, dayId)
    }
}
