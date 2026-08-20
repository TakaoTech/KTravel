package com.takaotech.ktravel.domain.usecase

import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.storedTransportMode
import com.takaotech.ktravel.domain.routing.model.toStoredRoute
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Files the alternative the traveller confirmed into the plan.
 *
 * One overload per kind of answer rather than one method taking a common type. The two are stored in
 * the same shape, but they are not filed the same way, and the difference is not a detail: a journey
 * almost always starts on foot, so reading the mode off its first step filed every train ride in
 * every plan as a walk — and, since a walk is not a [TransportType], as a car.
 */
@SingleIn(PlanningGraphScope::class)
@Inject
class SaveTransportStepUseCase(private val repository: TravelPlanRepository) {

    /** Files a route on roads, under the vehicle it is driven in. */
    suspend operator fun invoke(dayId: String, afterStepId: String, route: RoutingRoute) {
        store(dayId, afterStepId, route.sections.firstOrNull()?.mode, route.toStoredRoute())
    }

    /** Files a journey, under the first vehicle it puts the traveller on. */
    suspend operator fun invoke(dayId: String, afterStepId: String, journey: TransitJourney) {
        store(dayId, afterStepId, journey.storedTransportMode(), journey.toStoredRoute())
    }

    private suspend fun store(
        dayId: String,
        afterStepId: String,
        mode: String?,
        route: com.takaotech.ktravel.domain.routing.model.Route,
    ) {
        repository.addTransportStep(
            dayId,
            afterStepId,
            StepDomain.Transport(type = mode.toTransportType(), route = route),
        )
    }

    /**
     * The kind of step the plan shows, out of the mode the navigator named it with.
     *
     * The plan's vocabulary is much smaller than the navigator's, so most modes land on the default.
     * Rail is matched on more than one word because the two profiles name it differently: a road
     * route never has a train in it, and a timetable has half a dozen kinds of one.
     */

    // FIXME This conversion lost what type of transport you selected.
    private fun String?.toTransportType(): TransportType = when (this?.uppercase()) {
        "TRAIN",
        "HIGH_SPEED_TRAIN",
        "INTERCITY_TRAIN",
        "INTER_REGIONAL_TRAIN",
        "REGIONAL_TRAIN",
        "CITY_TRAIN",
        -> TransportType.TRAIN

        "BUS", "PRIVATE_BUS", "BUS_RAPID" -> TransportType.BUS

        "FLIGHT" -> TransportType.FLIGHT

        else -> TransportType.CAR
    }
}
