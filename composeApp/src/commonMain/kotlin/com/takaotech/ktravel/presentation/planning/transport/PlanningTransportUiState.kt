package com.takaotech.ktravel.presentation.planning.transport

import androidx.compose.runtime.Stable
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.RouteTimeChoice
import com.takaotech.ktravel.domain.routing.RoutingCatalog
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.RoutingProfileInfo
import com.takaotech.ktravel.domain.routing.RoutingProfileOption
import com.takaotech.ktravel.domain.routing.model.RouteResult
import com.takaotech.ktravel.presentation.planning.StepUi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

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

    // ---- when to travel -------------------------------------------------------------------------
    /**
     * When the traveller wants to be moving, which every profile is asked the same question about.
     *
     * Kept here rather than in the options block: it does not depend on which profile computes the
     * route, and changing profile drops the options while leaving the hour alone.
     */
    val timeChoice: RouteTimeChoice = RouteTimeChoice.Now,
    /** The day this leg belongs to, which is what turns [timeChoice] into an instant. */
    val dayDate: LocalDate? = null,
    /** The hour the previous stop is left at, when it has one. */
    val departureSuggestion: LocalTime? = null,
    /** The hour the next stop is due to start at, when it has one. */
    val arrivalSuggestion: LocalTime? = null,

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
    /**
     * What came back, as one of the two things it can be.
     *
     * The sealed type is what picks the screen: a road route is drawn as a list of manoeuvres, a
     * journey as a timeline of departures, and neither view can render the other's model.
     */
    val result: RouteResult? = null,
    val selectedRouteIndex: Int = 0,
) {

    /** The profile the traveller picked, together with whether it can be used. */
    val selectedOption: RoutingProfileOption?
        get() = catalog?.options?.firstOrNull { it.profile.id == selectedProfileId }

    val selectedProfile: RoutingProfileInfo? get() = selectedOption?.profile

    /**
     * Whether the chosen profile can plan backwards from an arrival time.
     *
     * True while no profile is chosen yet, rather than false: the option is only ever *withdrawn* by
     * a profile that says it cannot honour it, and a catalog that has not answered yet has said no
     * such thing. Nothing can be computed in that state anyway — [canCalculate] sees to that — and a
     * choice made before the profile arrives is dropped by [PlanningTransportViewModel.selectProfile]
     * if that profile turns out to refuse it.
     */
    val supportsArriveBy: Boolean get() = selectedProfile?.supportsArriveBy != false

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
            dayDate != null &&
            selectedOption?.isSelectable == true &&
            isRequestReady
}
