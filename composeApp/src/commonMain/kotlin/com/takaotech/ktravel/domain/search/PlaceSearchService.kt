package com.takaotech.ktravel.domain.search

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidate

/**
 * Finding places by name, without knowing who answers.
 *
 * The navigator that answers is the one this trip prefers, embedded or remote, and the trip's key
 * pays for it. Unlike routing there is no choice to offer on the screen: a search is cheap to repeat,
 * and the traveller picks a provider, not a server.
 */
@OpenForMokkery
interface PlaceSearchService {

    /**
     * The search providers the trip's navigator serves, joined with the ones this build can call.
     *
     * Never fails, for the same reason as [com.takaotech.ktravel.domain.routing.RoutingService.catalog]:
     * a navigator that does not answer is something to show, so every provider comes back
     * [ProfileAvailability.NavigatorUnreachable].
     */
    suspend fun catalog(): PlaceSearchCatalog

    /**
     * The places matching [query], as the given provider ranks them.
     *
     * Only places come back: the category and chain suggestions a provider may also offer need a
     * follow-up search the navigator does not expose yet.
     *
     * @throws PlaceSearchFailure when the provider could not be asked or refused to answer.
     */
    suspend fun autocomplete(provider: PlaceSearchProvider, query: PlaceSearchQuery): List<PlaceCandidate>
}

/**
 * What the traveller typed, and the context that ranks the answer.
 *
 * @property text The words searched for.
 * @property near Where the traveller is looking: results closer to it rank higher, and their
 *   distance is measured from it.
 * @property language An IETF BCP 47 tag, such as `it-IT`, the answer is written in.
 * @property limit The most results to return.
 */
data class PlaceSearchQuery(
    val text: String,
    val near: GeoCoordinate,
    val language: String,
    val limit: Int = DEFAULT_LIMIT,
) {
    private companion object {
        const val DEFAULT_LIMIT = 20
    }
}

/**
 * A search provider, as the traveller picks it.
 *
 * @property id The provider's identifier in the navigator contract, such as `here`.
 * @property name The brand, as it is written: not translated.
 */
data class PlaceSearchProvider(val id: String, val name: String)

/**
 * A provider, and whether it can be picked now.
 *
 * @property provider The provider itself.
 * @property availability Why it cannot be used, or [ProfileAvailability.Available].
 */
data class PlaceSearchProviderOption(val provider: PlaceSearchProvider, val availability: ProfileAvailability) {
    /** Whether a search can be sent to this provider right now. */
    val isSelectable: Boolean get() = availability == ProfileAvailability.Available
}

/**
 * Every search provider this build knows, and whether the chosen navigator lets each be used.
 *
 * Unavailable providers are listed too, so the selector can say why one cannot be picked.
 *
 * @property options Every provider, in the order they should be offered.
 */
data class PlaceSearchCatalog(val options: List<PlaceSearchProviderOption>) {
    /** The providers that can actually be picked, in the order they should be offered. */
    val selectable: List<PlaceSearchProviderOption> get() = options.filter { it.isSelectable }
}

/**
 * Why a search could not be answered.
 *
 * Mirrors [com.takaotech.ktravel.domain.routing.RoutingFailure]: each case has its own remedy, and
 * the screen has to say which one applies.
 */
sealed class PlaceSearchFailure(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The navigator did not answer at all. */
    class NavigatorUnreachable(cause: Throwable?) : PlaceSearchFailure("The navigator could not be reached", cause)

    /** The navigator refused the caller. */
    class NotAuthenticated(message: String) : PlaceSearchFailure(message)

    /** The provider refused the traveller's key, or none was configured for this trip. */
    class ProviderCredentials(message: String) : PlaceSearchFailure(message)

    /** The provider is throttling this caller. */
    class RateLimited(message: String) : PlaceSearchFailure(message)

    /** The provider is there but not answering usefully. */
    class ProviderUnavailable(message: String) : PlaceSearchFailure(message)

    /** This build has no way to call the chosen provider. */
    class UnsupportedProvider(providerId: String) : PlaceSearchFailure("No search is wired for provider '$providerId'")

    /** The request was not one the navigator accepts, which is a bug in the app. */
    class InvalidRequest(message: String) : PlaceSearchFailure(message)

    /** Anything else the navigator reported. */
    class Unexpected(message: String, cause: Throwable? = null) : PlaceSearchFailure(message, cause)
}
