package com.takaotech.ktravel.data.routing

import com.takaotech.gunzou.api.catalog.NavigatorProfile
import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.client.NavigatorClient
import com.takaotech.gunzou.client.NavigatorResult
import com.takaotech.gunzou.client.NavigatorTarget
import com.takaotech.ktravel.data.navigator.NavigatorTargetResolver
import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RouteTimeChoice
import com.takaotech.ktravel.domain.routing.RoutingCatalog
import com.takaotech.ktravel.domain.routing.RoutingFailure
import com.takaotech.ktravel.domain.routing.RoutingProfileOption
import com.takaotech.ktravel.domain.routing.RoutingService
import com.takaotech.ktravel.domain.routing.model.RouteResult
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.TimeSource

/**
 * The one thing in the app that computes routes.
 *
 * It knows the navigator contract and nothing above it does: the screen picks a profile out of a
 * catalog and gets [Routes] back, without ever learning which engine answered or whether the answer
 * came from this process or from a server across the network.
 */
@ContributesBinding(PlanningGraphScope::class)
@Inject
class NavigatorRoutingService(
    private val client: NavigatorClient,
    private val targets: NavigatorTargetResolver,
    private val settingsRepository: SettingsRepository,
) : RoutingService {

    /**
     * Asks a navigator what it serves, and joins that with what this build can express.
     *
     * The join is the whole design. The contract module declares every profile this version knows;
     * the navigator declares which of them it mounts. Showing only the intersection would tell the
     * traveller that a profile does not exist when the truth is that this deployment does not serve
     * it — a different problem, with a different thing to do about it.
     */
    override suspend fun catalog(kind: NavigatorKind): RoutingCatalog = withContext(Dispatchers.Default) {
        val known = NavigatorProfile.ALL
        val target = runCatching { targets.resolve(kind) }.getOrNull()
            ?: return@withContext unreachableCatalog(known, "No navigator is configured")

        val started = TimeSource.Monotonic.markNow()
        val response = client.profiles(target)
        val elapsed = started.elapsedNow().inWholeMilliseconds

        when (response) {
            is NavigatorResult.Success -> {
                val servedIds = response.value.profiles.map { it.toProfileId() }.toSet()
                val hasApiKey = settingsRepository.settings.hereApiKey.isNotBlank()

                RoutingCatalog(
                    options = known.map { profile ->
                        val info = profile.toProfileInfo()
                        RoutingProfileOption(
                            profile = info,
                            availability = when {
                                info.id !in servedIds -> ProfileAvailability.NotServed

                                // Checked here rather than at calculation time so the reason is
                                // visible while the traveller is still choosing, and points at the
                                // screen that fixes it.
                                info.requiresApiKey && !hasApiKey -> ProfileAvailability.MissingApiKey

                                else -> ProfileAvailability.Available
                            },
                        )
                    },
                    navigatorVersion = response.value.version.orEmpty()
                        .ifBlank { UNNAMED_VERSION },
                    latencyMillis = elapsed,
                )
            }

            is NavigatorResult.ServerError -> unreachableCatalog(known, response.error.message)

            is NavigatorResult.TransportError -> unreachableCatalog(
                known,
                response.cause.reachabilityMessage(),
            )
        }
    }

    override suspend fun routes(
        kind: NavigatorKind,
        origin: String,
        destination: String,
        selection: RouteSelection,
        time: RouteTimeChoice,
        dayDate: LocalDate,
    ): RouteResult = withContext(Dispatchers.Default) {
        val from = origin.toGeoPoint()
        val to = destination.toGeoPoint()
        val apiKey = settingsRepository.settings.hereApiKey.takeIf { it.isNotBlank() }

        val routeTime = time.toRouteTime(dayDate, TimeZone.currentSystemDefault())

        // The one place a profile is dispatched on, and now also the one place the kind of answer is
        // decided. The two halves cannot drift apart: each branch calls the method whose answer its
        // own result variant is built from, and neither type fits the other.
        when (selection) {
            is RouteSelection.Routing -> RouteResult.Routing(
                callWithRecovery(kind) { target ->
                    client.hereRouting(
                        selection.mode.toHereTransportMode(),
                        selection.toRoutingRequest(from, to, routeTime),
                        apiKey,
                        target,
                    )
                }.orThrow().toDomain(),
            )

            is RouteSelection.Transit -> RouteResult.Transit(
                callWithRecovery(kind) { target ->
                    client.hereTransit(
                        selection.toTransitRouteRequest(from, to, routeTime),
                        apiKey,
                        target,
                    )
                }.orThrow().toDomain(),
            )
        }
    }

    /**
     * Runs a call, and gives an embedded server exactly one second chance.
     *
     * The embedded server does not survive the app being suspended: on iOS the socket is gone when
     * the app returns to the foreground, and the only symptom is a connection refused. Restarting it
     * and asking again turns that into nothing the traveller ever sees. A remote navigator gets no
     * such retry — it did not answer, and asking the same host again in the same breath is a second
     * failure rather than a recovery.
     */
    private suspend fun <T : Any> callWithRecovery(
        kind: NavigatorKind,
        call: suspend (NavigatorTarget) -> NavigatorResult<T>,
    ): NavigatorResult<T> {
        val first = call(targets.resolve(kind))
        if (first !is NavigatorResult.TransportError || kind != NavigatorKind.EMBEDDED) return first

        return call(targets.recover(kind))
    }

    /**
     * A catalog for a navigator that said nothing.
     *
     * Every profile is listed, all of them unavailable for the same reason. Listing none would be
     * indistinguishable from a navigator that serves nothing, and the two call for opposite things:
     * fix the connection, or pick a different navigator.
     */
    private fun unreachableCatalog(known: List<NavigatorProfile>, reason: String): RoutingCatalog = RoutingCatalog(
        options = known.map {
            RoutingProfileOption(
                profile = it.toProfileInfo(),
                availability = ProfileAvailability.NavigatorUnreachable(reason),
            )
        },
    )
}

/** The version string to show for a navigator that answered without naming itself. */
private const val UNNAMED_VERSION = "unknown"

/** A transport failure, said in a way that describes the situation rather than the exception class. */
private fun Throwable.reachabilityMessage(): String = message?.takeIf { it.isNotBlank() } ?: "No answer"

/**
 * The routes, or the reason there are none.
 *
 * Each code becomes its own failure because each has its own remedy, and the screen has to say which
 * one applies: a rejected access token is fixed in the app's settings, a rejected provider key in the
 * trip's, and an unreachable host in neither.
 */
private fun <T : Any> NavigatorResult<T>.orThrow(): T = when (this) {
    is NavigatorResult.Success -> value

    is NavigatorResult.TransportError -> throw RoutingFailure.NavigatorUnreachable(cause)

    is NavigatorResult.ServerError -> throw when (error.code) {
        ErrorCode.UNAUTHENTICATED -> RoutingFailure.NotAuthenticated(error.message)

        ErrorCode.MISSING_CREDENTIALS, ErrorCode.PROVIDER_UNAUTHORIZED -> RoutingFailure.ProviderCredentials(
            error.message,
        )

        ErrorCode.PROVIDER_RATE_LIMITED -> RoutingFailure.RateLimited(error.message)

        ErrorCode.PROVIDER_UNAVAILABLE -> RoutingFailure.ProviderUnavailable(error.message)

        ErrorCode.NO_ROUTE_FOUND -> RoutingFailure.NoRouteFound(error.message)

        ErrorCode.INVALID_REQUEST, ErrorCode.UNSUPPORTED_OPTION -> RoutingFailure.InvalidRequest(
            error.message,
        )

        ErrorCode.INTERNAL -> RoutingFailure.Unexpected(error.message)
    }
}
