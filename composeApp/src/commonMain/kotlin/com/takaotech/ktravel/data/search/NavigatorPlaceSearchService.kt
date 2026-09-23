package com.takaotech.ktravel.data.search

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.common.SearchProviderId
import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.search.SearchProfile
import com.takaotech.gunzou.api.search.SearchService
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteRequest
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteSuggestion
import com.takaotech.gunzou.client.NavigatorClient
import com.takaotech.gunzou.client.NavigatorResult
import com.takaotech.gunzou.client.NavigatorTarget
import com.takaotech.ktravel.data.navigator.NavigatorTargetResolver
import com.takaotech.ktravel.data.navigator.callWithRecovery
import com.takaotech.ktravel.data.navigator.reachabilityMessage
import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.search.PlaceSearchCatalog
import com.takaotech.ktravel.domain.search.PlaceSearchFailure
import com.takaotech.ktravel.domain.search.PlaceSearchProvider
import com.takaotech.ktravel.domain.search.PlaceSearchProviderOption
import com.takaotech.ktravel.domain.search.PlaceSearchQuery
import com.takaotech.ktravel.domain.search.PlaceSearchService
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Place search through the navigator, the same one that computes routes.
 *
 * Like [com.takaotech.ktravel.data.routing.NavigatorRoutingService], it is the only place that
 * knows the contract: the screen picks a provider out of a catalog and gets candidates back.
 */
@ContributesBinding(PlanningGraphScope::class)
@Inject
class NavigatorPlaceSearchService(
    private val client: NavigatorClient,
    private val targets: NavigatorTargetResolver,
    private val settingsRepository: SettingsRepository,
) : PlaceSearchService {

    /**
     * Joins the autocomplete profiles this build knows with the ones the navigator mounts, the same
     * join the routing catalog makes and for the same reason.
     */
    override suspend fun catalog(): PlaceSearchCatalog = withContext(Dispatchers.Default) {
        val known = SearchProfile.ALL.filter { it.descriptor.service == SearchService.AUTOCOMPLETE }
        val target = resolveTarget(navigatorKind())
            ?: return@withContext unreachableCatalog(known, "No navigator is configured")

        when (val response = client.searchProfiles(target)) {
            is NavigatorResult.Success -> {
                val served = response.value.profiles
                    .filter { it.service == SearchService.AUTOCOMPLETE }
                    .map { it.provider }
                    .toSet()
                val hasApiKey = settingsRepository.settings.hereApiKey.isNotBlank()

                PlaceSearchCatalog(
                    options = known.map { profile ->
                        PlaceSearchProviderOption(
                            provider = profile.descriptor.provider.toDomain(),
                            availability = when {
                                profile.descriptor.provider !in served -> ProfileAvailability.NotServed
                                profile.descriptor.requiresApiKey && !hasApiKey -> ProfileAvailability.MissingApiKey
                                else -> ProfileAvailability.Available
                            },
                        )
                    },
                )
            }

            is NavigatorResult.ServerError -> unreachableCatalog(known, response.error.message)

            is NavigatorResult.TransportError -> unreachableCatalog(known, response.cause.reachabilityMessage())
        }
    }

    override suspend fun autocomplete(provider: PlaceSearchProvider, query: PlaceSearchQuery): List<PlaceCandidate> =
        withContext(Dispatchers.Default) {
            val apiKey = settingsRepository.settings.hereApiKey.takeIf { it.isNotBlank() }
            val request = AutocompleteRequest(
                query = query.text,
                language = query.language,
                // No area: the framed rectangle would drop the one result the traveller is looking
                // for as soon as it lies just outside it. The position only ranks the answer.
                at = GeoPoint(lat = query.near.lat, lng = query.near.lng),
                limit = query.limit,
            )

            val response = when (SearchProviderId.from(provider.id)) {
                SearchProviderId.Here -> targets.callWithRecovery(navigatorKind()) { target ->
                    client.hereAutocomplete(request, apiKey, target)
                }

                // TODO wire Photon once the navigator serves it and the client has a call for it.
                else -> throw PlaceSearchFailure.UnsupportedProvider(provider.id)
            }

            response.orThrow().suggestions
                // TODO category and chain suggestions need a follow-up search the contract does not
                //  expose yet; until then only places can be offered.
                .filterIsInstance<AutocompleteSuggestion.Place>()
                .map { it.toCandidate() }
        }

    /**
     * The navigator this trip prefers, or the embedded one when the preference names a remote server
     * that is not configured — the same fallback the transport screen opens on.
     */
    private fun navigatorKind(): NavigatorKind = targets.defaultKind()
        .takeUnless { it == NavigatorKind.REMOTE && !targets.isRemoteConfigured() }
        ?: NavigatorKind.EMBEDDED

    /** Where to call, or `null` when [kind] names a navigator with no address. */
    private suspend fun resolveTarget(kind: NavigatorKind): NavigatorTarget? = try {
        targets.resolve(kind)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: IllegalStateException) {
        null
    }

    private fun unreachableCatalog(known: List<SearchProfile>, reason: String): PlaceSearchCatalog = PlaceSearchCatalog(
        options = known.map {
            PlaceSearchProviderOption(
                provider = it.descriptor.provider.toDomain(),
                availability = ProfileAvailability.NavigatorUnreachable(reason),
            )
        },
    )
}

/** The answer, or the reason there is none, typed so the screen can name the remedy. */
private fun <T : Any> NavigatorResult<T>.orThrow(): T = when (this) {
    is NavigatorResult.Success -> value

    is NavigatorResult.TransportError -> throw PlaceSearchFailure.NavigatorUnreachable(cause)

    is NavigatorResult.ServerError -> throw when (error.code) {
        ErrorCode.UNAUTHENTICATED -> PlaceSearchFailure.NotAuthenticated(error.message)

        ErrorCode.MISSING_CREDENTIALS, ErrorCode.PROVIDER_UNAUTHORIZED ->
            PlaceSearchFailure.ProviderCredentials(error.message)

        ErrorCode.PROVIDER_RATE_LIMITED -> PlaceSearchFailure.RateLimited(error.message)

        ErrorCode.PROVIDER_UNAVAILABLE -> PlaceSearchFailure.ProviderUnavailable(error.message)

        ErrorCode.INVALID_REQUEST, ErrorCode.UNSUPPORTED_OPTION -> PlaceSearchFailure.InvalidRequest(error.message)

        ErrorCode.NO_ROUTE_FOUND, ErrorCode.INTERNAL -> PlaceSearchFailure.Unexpected(error.message)
    }
}
