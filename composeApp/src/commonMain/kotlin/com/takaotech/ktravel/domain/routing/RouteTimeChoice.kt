package com.takaotech.ktravel.domain.routing

import kotlinx.datetime.LocalTime

/**
 * When the traveller wants to be moving, as the screen asks it.
 *
 * Sealed for the same reason [com.takaotech.navigator.api.common.RouteTime] is downstream: leaving at
 * a time and arriving by one are exclusive, and a type that cannot hold both is a better guarantee
 * than a rule that says it must not.
 *
 * A [LocalTime] and not a date and time, because a leg belongs to a day of the plan and that day is
 * what supplies the date. The instant is resolved where the request is built, in the timezone the
 * traveller was looking at when they picked the hour.
 */
sealed interface RouteTimeChoice {

    /** Now is use because as fallback if isn't possible declare a depart/arrive time.
     * With this method save a navigation route without specifying time.
     */
    data object Now : RouteTimeChoice

    /** Leave at [time] on the day of the leg. */
    data class DepartAt(val time: LocalTime) : RouteTimeChoice

    /** Arrive no later than [time] on the day of the leg; the departure is computed backwards. */
    data class ArriveBy(val time: LocalTime) : RouteTimeChoice
}
