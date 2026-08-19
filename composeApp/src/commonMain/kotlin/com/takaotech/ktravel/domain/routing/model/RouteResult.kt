package com.takaotech.ktravel.domain.routing.model

/**
 * What came back from a calculation.
 *
 * Sealed for the same reason [com.takaotech.ktravel.domain.routing.RouteSelection] is: the two
 * profiles do not answer the same thing, and a screen that draws a journey has to draw lines, stops
 * and departure times, none of which a road route has. Which variant arrives is not guessed — it
 * follows from the selection that was sent.
 *
 * It is also what makes the choice of screen a `when` the compiler checks: a third profile does not
 * compile until it has been given something to draw.
 */
sealed interface RouteResult {

    /** Whether there is anything to show at all. */
    val isEmpty: Boolean

    /** Alternatives on roads. */
    data class Routing(val routes: RoutingRoutes) : RouteResult {
        override val isEmpty: Boolean get() = routes.routes.isEmpty()
    }

    /** Alternatives on scheduled services. */
    data class Transit(val journeys: TransitJourneys) : RouteResult {
        override val isEmpty: Boolean get() = journeys.journeys.isEmpty()
    }
}
