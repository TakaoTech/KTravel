package com.takaotech.ktravel.gunzou.server.endpoint.search

/**
 * One call to a `/{provider}/search/id/{id}` path, assembled from the path and the query string.
 *
 * It has no body to be received as, which is why it is a type of the server rather than of the
 * contract: the client spells the same two values as a path segment and a query parameter.
 *
 * @property id The provider's identifier of the place, as another search of the same provider
 *   returned it, already decoded from the path.
 * @property language IETF BCP 47 tag of the language to answer in, such as `it-IT`.
 */
data class PlaceLookupCall(val id: String, val language: String)
