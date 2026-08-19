package com.takaotech.ktravel.domain.routing.model

/**
 * What a road profile answered: ways of driving, walking or cycling from one place to another.
 *
 * Separate from [TransitJourneys] because the two are not one thing described twice — see the note
 * there. The pieces they genuinely share ([RouteSummary], [RouteAction], [RouteDeparture]) live in
 * `Routes.kt` and are used by both.
 *
 * @property routes The alternatives, best first.
 */
data class RoutingRoutes(val routes: List<RoutingRoute>)

/**
 * One way of getting there by road.
 *
 * @property summary Totals for the whole alternative, aggregated by the navigator.
 * @property sections The legs, in travel order — usually one per via waypoint.
 */
data class RoutingRoute(val summary: RouteSummary, val sections: List<RoutingSection>)

/**
 * One leg of a road route.
 *
 * @property mode The vehicle, or the traveller's own power, in the navigator's vocabulary.
 * @property actions The manoeuvres to perform, which is what the screen for this profile is a list
 *   of.
 * @property polyline The drawable shape, in HERE flexible encoding.
 */
data class RoutingSection(
    val summary: RouteSummary,
    val mode: String,
    val actions: List<RouteAction> = emptyList(),
    val departure: RouteDeparture? = null,
    val arrival: RouteDeparture? = null,
    val polyline: String? = null,
    val tollSystems: List<RouteTollSystem> = emptyList(),
    val tolls: List<RouteTollCost> = emptyList(),
)
