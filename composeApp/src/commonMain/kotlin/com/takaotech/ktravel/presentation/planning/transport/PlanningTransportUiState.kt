package com.takaotech.ktravel.presentation.planning.transport

import androidx.compose.runtime.Stable
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.RoutingCatalog
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.RoutingProfileInfo
import com.takaotech.ktravel.domain.routing.RoutingProfileOption
import com.takaotech.ktravel.domain.routing.model.Routes
import com.takaotech.ktravel.presentation.planning.StepUi

sealed interface PlanningTransportNavigationEvent {
    data object NavigateToRoutePreview : PlanningTransportNavigationEvent
}

/**
 * Why a calculation failed, as something the screen can say.
 *
 * An enum and not a message because each of these is fixed somewhere different, and the text has to
 * point there: an access token lives in the app's settings, a provider key in the trip's, and an
 * unreachable navigator in neither.
 */
enum class TransportFailureReason {
    /** Nothing answered. */
    NAVIGATOR_UNREACHABLE,

    /** The navigator refused the caller: no access token, or not one it accepts. */
    NOT_AUTHENTICATED,

    /** The routing provider refused the trip's key, or none was configured. */
    PROVIDER_CREDENTIALS,

    /** Too many requests from this caller. */
    RATE_LIMITED,

    /** The provider is there but not answering usefully. */
    PROVIDER_UNAVAILABLE,

    /** There is no route between these places under these options. */
    NO_ROUTE_FOUND,

    /** The navigator rejected the request, which is a bug rather than a choice. */
    INVALID_REQUEST,

    /** Anything else. */
    UNEXPECTED,
}

@Stable
data class PlanningTransportUiState(
    val startPlace: StepUi.Place? = null,
    val endPlace: StepUi.Place? = null,

    // ---- which navigator ------------------------------------------------------------------------
    /** The one being used right now. Seeded from the plan's preference and not written back. */
    val navigatorKind: NavigatorKind = NavigatorKind.EMBEDDED,
    /** Whether a remote one exists to switch to at all. */
    val isRemoteConfigured: Boolean = false,
    val isCatalogLoading: Boolean = false,
    /** What that navigator serves, or null before the first answer. */
    val catalog: RoutingCatalog? = null,

    // ---- what to ask it for ---------------------------------------------------------------------
    val selectedProfileId: RoutingProfileId? = null,
    /**
     * The options controls to draw for the chosen profile, or null when there is nothing to
     * configure.
     *
     * A [Screen] and not a set of fields: which controls a profile takes depends on the family of
     * API behind it and, within a road profile, on the vehicle chosen — so the screen renders it
     * through `CircuitContent` and the presenter behind it owns both the choices and the rules.
     */
    val routeOptionsScreen: Screen? = null,
    /** Whether those controls have produced a request that can be sent. */
    val isRequestReady: Boolean = false,

    // ---- the answer -----------------------------------------------------------------------------
    val isLoading: Boolean = false,
    val failure: TransportFailureReason? = null,
    val routes: Routes? = null,
    val selectedRouteIndex: Int = 0,
) {

    /** The profile the traveller picked, together with whether it can be used. */
    val selectedOption: RoutingProfileOption?
        get() = catalog?.options?.firstOrNull { it.profile.id == selectedProfileId }

    val selectedProfile: RoutingProfileInfo? get() = selectedOption?.profile

    /**
     * Whether pressing Calculate can produce anything.
     *
     * The button was previously always enabled, and pressing it on a provider that did not exist
     * threw straight past the screen. Everything it takes to build a request is checked here
     * instead — including [isRequestReady], which is the options block saying it has one.
     */
    val canCalculate: Boolean
        get() = !isLoading &&
            startPlace != null &&
            endPlace != null &&
            selectedOption?.isSelectable == true &&
            isRequestReady
}
