package com.takaotech.ktravel.data.routing

import com.takaotech.ktravel.data.navigator.NavigatorBaseUrlResolver
import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.ktravel.domain.routing.RoutingProvider
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.ktravel.domain.routing.model.Routes
import com.takaotech.navigator.api.response.RouteResponse
import com.takaotech.navigator.client.NavigatorClient
import com.takaotech.navigator.client.NavigatorException
import com.takaotech.navigator.client.NavigatorResult
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Routes through gunzo-navigator.
 *
 * Replaces the provider that called HERE directly, and the difference is the point of the exercise:
 * nothing in this class names a routing engine. It builds a contract request, sends it, and
 * translates the answer — and whether HERE, Valhalla or something not chosen yet computed it, and
 * whether that happened in this process or on a server, is decided somewhere it cannot see.
 *
 * Still in [PlanningGraphScope] because the provider key belongs to the plan. The key now travels on
 * the request rather than being baked into an HTTP client, which also removes the reason the old
 * provider had to rebuild its client on every call.
 */
@Named("HERE")
@ContributesBinding(PlanningGraphScope::class)
@Inject
class NavigatorRoutingProvider(
    private val client: NavigatorClient,
    private val navigatorBaseUrl: NavigatorBaseUrlResolver,
    private val settingsRepository: SettingsRepository,
) : RoutingProvider {

    override suspend fun getRoutes(origin: String, destination: String, settings: RoutingProviderSettings): Routes =
        withContext(Dispatchers.Default) {
            require(settings is RoutingProviderSettings.Here) {
                "Expected Here settings but got: ${settings::class.simpleName}"
            }

            val request = settings.toCarRouteRequest(
                origin = origin.toGeoPoint(),
                destination = destination.toGeoPoint(),
            )
            val apiKey = settingsRepository.settings.hereApiKey

            route { client.hereCar(request, apiKey) }.toDomainRoutes()
        }

    /**
     * Sends a request, and gives an unreachable navigator exactly one second chance.
     *
     * This is where the embedded server's one real weakness is handled. On iOS the process is
     * suspended and the socket does not come back with it: the first request after the app returns
     * to the foreground is refused, on a port nobody is listening on any more. Restarting and asking
     * again is the whole recovery, and it has to happen here rather than inside the client, because
     * only this side knows there is an embedded server that *can* be restarted.
     *
     * Only a transport failure is retried. A navigator that answered and refused — a missing key, no
     * route, an unsupported option — will refuse again, and retrying would double every such call
     * and restart a perfectly healthy server while doing it.
     */
    private suspend fun route(call: suspend () -> NavigatorResult<RouteResponse>): RouteResponse {
        val first = call()
        if (first !is NavigatorResult.TransportError) return first.orThrow()

        navigatorBaseUrl.recover()

        return call().orThrow(afterRestart = true)
    }
}

/**
 * The routes, or the failure as an exception the planning screen already knows how to show.
 *
 * @param afterRestart Whether this is the second attempt, so an unreachable navigator says which of
 *   the two it is: one that has not been restarted yet, or one that was and still is not there.
 */
private fun NavigatorResult<RouteResponse>.orThrow(afterRestart: Boolean = false): RouteResponse = when (this) {
    is NavigatorResult.Success -> value

    is NavigatorResult.ServerError -> throw NavigatorException(
        message = "The navigator refused the request: ${error.code} — ${error.message}",
    )

    is NavigatorResult.TransportError -> throw NavigatorException(
        message = if (afterRestart) {
            "The navigator could not be reached, even after being restarted"
        } else {
            "The navigator could not be reached"
        },
        cause = cause,
    )
}
