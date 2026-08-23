@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.domain.usecase

import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.model.TravelPlanEditor.transportAfter
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Files the alternative the traveller confirmed into the plan.
 *
 * The request that produced the alternatives is filed beside the answer, so a second calculation
 * between the same two places starts from what the traveller asked for rather than from the
 * defaults. It is passed in rather than read from the options draft: the draft is what the screen
 * is holding *now*, and the traveller may have changed a vehicle after computing and before
 * confirming.
 *
 * One overload per kind of answer rather than one method taking a common type: they are not stored
 * in the same shape, and they are not filed under the same vehicle. A journey almost always starts
 * on foot, so reading the mode off its first step filed every train ride in every plan as a walk —
 * and, since a walk is not a [TransportType], as a car. [TransportAnswer.principalMode] is where
 * that rule now lives, next to the answer it applies to.
 */
@SingleIn(PlanningGraphScope::class)
@Inject
class SaveTransportStepUseCase(private val repository: TravelPlanRepository) {

    /** Files a route on roads, under the vehicle it is driven in. */
    suspend operator fun invoke(
        dayId: String,
        afterStepId: String,
        route: RoutingRoute,
        request: RouteSelection,
        now: Instant = Clock.System.now(),
    ): String? = store(dayId, afterStepId, TransportAnswer.Routing(route), request, now)

    /** Files a journey, under the first vehicle it puts the traveller on. */
    suspend operator fun invoke(
        dayId: String,
        afterStepId: String,
        journey: TransitJourney,
        request: RouteSelection,
        now: Instant = Clock.System.now(),
    ): String? = store(dayId, afterStepId, TransportAnswer.Transit(journey), request, now)

    /**
     * Files the step and answers with its id, which is what the caller navigates to.
     *
     * The id is read back rather than taken from the step handed over: filing over an existing
     * transport keeps the id already in the plan, so the one built here is not the one that ends up
     * saved. Null when nothing was filed, which is what an unknown day or place amounts to.
     */
    private suspend fun store(
        dayId: String,
        afterStepId: String,
        answer: TransportAnswer,
        request: RouteSelection,
        now: Instant,
    ): String? {
        repository.putTransportStep(
            dayId,
            afterStepId,
            StepDomain.Transport(
                type = answer.principalMode.toTransportType(),
                answer = answer,
                request = request,
                calculatedAt = now,
            ),
        )
        return repository.getTravelDayFlow(dayId).first().steps.transportAfter(afterStepId)?.id
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
