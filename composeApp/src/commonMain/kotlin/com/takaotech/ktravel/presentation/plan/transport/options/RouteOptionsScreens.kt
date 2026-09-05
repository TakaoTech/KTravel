package com.takaotech.ktravel.presentation.plan.transport.options

import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileInfo

/**
 * Which options controls the transport screen draws for a profile.
 *
 * The one place that decides it, and the reason the screen itself no longer knows what a mode or an
 * option is: it renders whatever comes back through `CircuitContent`, and the presenter and UI
 * behind it are resolved from the factories Metro generates.
 *
 * Exhaustive over [RoutingOptionsSpec] on purpose. A family added to the contract does not compile
 * here until it is given a screen, which is the difference between adding a provider and silently
 * offering it the wrong controls.
 *
 * Null means there is nothing to configure — a profile that takes no mode, or one whose catalog
 * entry arrived without any.
 */
fun RoutingProfileInfo.routeOptionsScreen(travelId: String): Screen? = when (val spec = options) {
    RoutingOptionsSpec.None -> null

    is RoutingOptionsSpec.RoutingSingleMode ->
        if (spec.modes.isEmpty()) null else RoutingRouteOptionsScreen.of(travelId, id, spec)

    is RoutingOptionsSpec.TransitFilter ->
        if (spec.modes.isEmpty()) null else TransitRouteOptionsScreen.of(travelId, id, spec)
}
