package com.takaotech.ktravel.domain.routing

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.model.RouteResult
import kotlinx.datetime.LocalDate

/**
 * Computing routes, without knowing who computes them.
 *
 */
@OpenForMokkery
interface RoutingService {

    /**
     * What the given navigator can route with, joined with what this build knows how to ask for.
     *
     * Never fails: a navigator that does not answer produces a catalog whose profiles are all
     * [ProfileAvailability.NavigatorUnreachable], because "we could not ask" is something the
     * traveller has to be shown rather than an error to be thrown past them.
     */
    suspend fun catalog(kind: NavigatorKind): RoutingCatalog

    /**
     * Computes how to get between two places, expressed as `lat,lng`.
     *
     * The answer is a [RouteResult] and not one route type, because the two profiles do not answer
     * the same thing: which variant comes back follows from [selection] and is what decides the
     * screen that draws it.
     *
     * [time] and [dayDate] travel beside [selection] rather than inside it: when the traveller wants
     * to be moving does not depend on which profile computes the route, and a change of profile
     * drops the selection while leaving the hour they picked alone.
     *
     * @param dayDate The day the leg belongs to, which is what turns [time] into an instant.
     * @throws RoutingFailure when nothing can be produced. Typed, because each case has its own
     *   remedy and the screen has to say which one applies.
     */
    suspend fun routes(
        kind: NavigatorKind,
        origin: String,
        destination: String,
        selection: RouteSelection,
        time: RouteTimeChoice,
        dayDate: LocalDate,
    ): RouteResult
}

/**
 * Why a route could not be computed.
 *
 * An exception rather than a result type only because it crosses the same boundary the previous
 * provider did; what matters is that it is *typed*. The previous implementation threw an untyped
 * error that nothing caught, so a failed calculation left the screen spinning forever — which stops
 * being a rare accident once a remote navigator is involved and the reasons include a rejected token,
 * an unreachable host and a rate limit.
 */
sealed class RoutingFailure(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The navigator did not answer at all. */
    class NavigatorUnreachable(cause: Throwable?) : RoutingFailure("The navigator could not be reached", cause)

    /** It answered, and refused the caller: the access token is missing or not one it accepts. */
    class NotAuthenticated(message: String) : RoutingFailure(message)

    /** The provider refused the traveller's key, or none was configured for this trip. */
    class ProviderCredentials(message: String) : RoutingFailure(message)

    /** The provider is throttling this caller. */
    class RateLimited(message: String) : RoutingFailure(message)

    /** The provider is there but not answering usefully. */
    class ProviderUnavailable(message: String) : RoutingFailure(message)

    /** There is no route between these two places under these options. */
    class NoRouteFound(message: String) : RoutingFailure(message)

    /** The request was not one this navigator accepts, which is a bug in the app rather than a choice. */
    class InvalidRequest(message: String) : RoutingFailure(message)

    /** Anything else the navigator reported. */
    class Unexpected(message: String, cause: Throwable? = null) : RoutingFailure(message, cause)
}
