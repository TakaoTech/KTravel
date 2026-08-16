package com.takaotech.ktravel.presentation.planning.transport

import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.routing.RouteSelection
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * What the option controls have built, for the Calculate button of the transport screen to send.
 *
 * Held here rather than in the composition because the screen leaves it: computing a route navigates
 * to the route preview, and a traveller coming back to change a vehicle expects to find their
 * choices as they left them. A `rememberRetained` inside the options presenter would not survive
 * that trip.
 *
 * The state *is* the [RouteSelection], with nothing duplicated alongside it. The presenter of each
 * family owns what goes in; the screen only reads whether there is a request to send.
 */
@SingleIn(PlanningGraphScope::class)
@Inject
class RouteOptionsDraft {

    private val current = MutableStateFlow<RouteSelection?>(null)

    /** The request as configured so far, or null before a profile has been chosen. */
    val selection: StateFlow<RouteSelection?> = current.asStateFlow()

    fun update(selection: RouteSelection) {
        current.value = selection
    }

    /**
     * Forgets the request, which is what changing profile means.
     *
     * The options are not carried across: modes are declared per profile and their vocabularies
     * collide by name without meaning the same thing, so a mode kept from the previous profile would
     * be a value from another API's list that happens to spell the same.
     */
    fun clear() {
        current.value = null
    }
}
