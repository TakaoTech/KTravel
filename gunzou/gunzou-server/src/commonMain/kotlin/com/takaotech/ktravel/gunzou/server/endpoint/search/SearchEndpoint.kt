package com.takaotech.ktravel.gunzou.server.endpoint.search

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.search.SearchArea
import com.takaotech.gunzou.api.search.SearchProfileDescriptor
import com.takaotech.ktravel.gunzou.server.endpoint.NavigatorException
import com.takaotech.ktravel.gunzou.server.endpoint.ProviderCredentials

/**
 * One search service of one provider: everything behind a single `/{provider}/search/{service}` path.
 *
 * The search counterpart of [com.takaotech.ktravel.gunzou.server.endpoint.NavigationEndpoint], and
 * a separate interface for the same reason the catalogs are separate: what an endpoint publishes
 * about itself is a [SearchProfileDescriptor], which has none of the routing limits. What the two
 * share — credentials and the failure half of the contract — is shared by reusing
 * [ProviderCredentials] and [NavigatorException] rather than a common supertype.
 *
 * Unlike routing, every provider of a service takes the same [REQ] and answers the same [RES]: the
 * type parameters name the service, not the provider.
 */
interface SearchEndpoint<in REQ : Any, out RES : Any> {
    /** What this service can do, published verbatim through `GET /v1/search/profiles`. */
    val descriptor: SearchProfileDescriptor

    /**
     * Runs the search for [request].
     *
     * @param credentials The caller's key for the upstream provider, already checked to be present
     *   when [SearchProfileDescriptor.requiresApiKey] says so.
     * @throws NavigatorException for every failure a caller is meant to act on.
     */
    suspend fun search(request: REQ, credentials: ProviderCredentials?): RES
}

/**
 * Rejects what this service has published that it cannot do, before any upstream call is paid for.
 *
 * Validation that holds for every provider — a blank query, a coordinate off the globe — belongs in
 * the request validation plugin; this is the part that depends on the descriptor. It matters more
 * here than for routing: an autocomplete is called on every keystroke, and a request the provider
 * would refuse is a refusal paid for on every one of them.
 *
 * @throws NavigatorException [ErrorCode.INVALID_REQUEST] when a published limit is exceeded or the
 *   search is not located the way the service needs, and [ErrorCode.UNSUPPORTED_OPTION] when the
 *   area is a shape the service does not accept.
 */
fun SearchProfileDescriptor.requireWithinLimits(limit: Int, at: GeoPoint?, area: SearchArea?) {
    val isLocatingArea = area is SearchArea.Circle || area is SearchArea.BoundingBox
    val name = "$provider $service"

    val violation = when {
        limit > maxResults ->
            ErrorCode.INVALID_REQUEST to "$name returns at most $maxResults results, $limit asked"

        area != null && area.kind !in supportedAreas ->
            ErrorCode.UNSUPPORTED_OPTION to "$name cannot restrict a search to an area of type ${area.kind}"

        requiresLocation && at != null && isLocatingArea ->
            ErrorCode.INVALID_REQUEST to "$name takes either `at` or a circle or bounding box, not both"

        requiresLocation && at == null && !isLocatingArea ->
            ErrorCode.INVALID_REQUEST to "$name needs `at`, a circle or a bounding box to locate the search"

        else -> null
    }

    violation?.let { (code, message) -> throw NavigatorException(code = code, message = message) }
}
