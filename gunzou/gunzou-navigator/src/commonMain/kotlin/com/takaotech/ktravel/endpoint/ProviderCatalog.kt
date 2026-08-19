package com.takaotech.ktravel.endpoint

import com.takaotech.navigator.api.catalog.ProviderCatalogResponse
import com.takaotech.navigator.api.catalog.ProviderProfileDescriptor

/**
 * What this server can route with, assembled from the endpoints that are actually mounted.
 *
 * Built from the endpoints rather than written out by hand on purpose: a catalog maintained beside
 * the routes would eventually advertise a profile nobody serves, and the client that trusted it
 * would fail on a path returning 404. Here a profile is listed because an endpoint exists for it.
 *
 * This is not a registry and does not dispatch: the routes are wired one by one and Ktor picks
 * between them by path. The catalog only ever reads the descriptors.
 */
class ProviderCatalog(endpoints: List<NavigationEndpoint<*, *>>) {
    /** The mounted profiles, in the order the endpoints were declared. */
    val descriptors: List<ProviderProfileDescriptor> = endpoints.map { it.descriptor }

    /**
     * The body of `GET /v1/profiles`.
     *
     * @param version The build serving it. Answered here as well as on the health endpoint because a
     *   client loading the catalog has already proved the navigator is reachable, and asking again
     *   just to learn its name would be a round trip for nothing.
     */
    fun toResponse(version: String): ProviderCatalogResponse =
        ProviderCatalogResponse(profiles = descriptors, version = version)
}
