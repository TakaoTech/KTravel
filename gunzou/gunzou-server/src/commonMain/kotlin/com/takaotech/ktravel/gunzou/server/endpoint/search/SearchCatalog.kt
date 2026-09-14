package com.takaotech.ktravel.gunzou.server.endpoint.search

import com.takaotech.gunzou.api.search.SearchCatalogResponse
import com.takaotech.gunzou.api.search.SearchProfileDescriptor

/**
 * What this server can search with, assembled from the search endpoints that are actually mounted.
 *
 * Built from the endpoints for the same reason as
 * [com.takaotech.ktravel.gunzou.server.endpoint.ProviderCatalog]: a list written beside the routes
 * would eventually advertise a service nobody serves.
 */
class SearchCatalog(endpoints: List<SearchEndpoint<*, *>>) {
    /** The mounted services, in the order the endpoints were declared. */
    val descriptors: List<SearchProfileDescriptor> = endpoints.map { it.descriptor }

    /** The body of `GET /v1/search/profiles`, answered by the build named [version]. */
    fun toResponse(version: String): SearchCatalogResponse =
        SearchCatalogResponse(profiles = descriptors, version = version)
}
