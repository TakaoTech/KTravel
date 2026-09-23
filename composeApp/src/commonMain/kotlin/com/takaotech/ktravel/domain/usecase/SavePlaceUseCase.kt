package com.takaotech.ktravel.domain.usecase

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Adds the places the traveller picked to the trip's backlog.
 *
 * All of them in one change, so the plan is persisted once and never holds half a selection.
 */
@SingleIn(PlanningGraphScope::class)
@OpenForMokkery
@Inject
class SavePlaceUseCase(private val repository: TravelPlanRepository) {

    /**
     * Saves [candidates] in the order they were picked, to the backlog of the day with [dayId], or to
     * the trip's own backlog when it is `null`.
     */
    open suspend operator fun invoke(candidates: List<PlaceCandidate>, dayId: String?) {
        if (candidates.isEmpty()) return

        val places = candidates.map {
            PlaceDomain(name = it.title, lat = it.coordinate.lat, lng = it.coordinate.lng)
        }
        repository.savePlaces(places, dayId)
    }
}
