package com.takaotech.ktravel.gunzou.server.endpoint.here.search

import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.search.place.PlaceDetails
import com.takaotech.ktravel.gunzou.server.endpoint.NavigatorException
import com.takaotech.ktravel.gunzou.server.endpoint.ProviderCredentials
import com.takaotech.ktravel.gunzou.server.endpoint.here.HereClientPool
import com.takaotech.ktravel.gunzou.server.endpoint.here.orThrowNavigatorException
import com.takaotech.ktravel.gunzou.server.endpoint.search.PlaceLookupCall

/**
 * `/v1/here/search/id/{id}`, served by HERE Geocoding and Search v7 Lookup.
 *
 * Short on purpose, like the autocomplete: the two translations live in
 * `HerePlaceLookupRequestMapping` and `HerePlaceLookupResponseMapping`.
 */
class LiveHerePlaceLookupEndpoint(private val clients: HereClientPool) : HerePlaceLookupEndpoint {

    override suspend fun search(request: PlaceLookupCall, credentials: ProviderCredentials?): PlaceDetails {
        val apiKey = credentials?.apiKey
            ?: throw NavigatorException(
                code = ErrorCode.MISSING_CREDENTIALS,
                message = "${descriptor.service} cannot be served without a HERE key",
            )

        // Built before borrowing a client, so a request HERE's own DTO refuses never holds one.
        val hereRequest = request.toHereLookupRequest()

        // A 404 from Lookup is an identifier HERE does not know, not a route it could not find.
        return clients.withClient(apiKey) { client ->
            client.lookup.lookup(hereRequest)
        }.orThrowNavigatorException(notFound = ErrorCode.PLACE_NOT_FOUND).toPlaceDetails()
    }
}
