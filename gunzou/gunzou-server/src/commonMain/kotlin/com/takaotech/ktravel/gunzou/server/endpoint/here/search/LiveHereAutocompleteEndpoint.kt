package com.takaotech.ktravel.gunzou.server.endpoint.here.search

import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteRequest
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteResponse
import com.takaotech.ktravel.gunzou.server.endpoint.NavigatorException
import com.takaotech.ktravel.gunzou.server.endpoint.ProviderCredentials
import com.takaotech.ktravel.gunzou.server.endpoint.here.HereClientPool
import com.takaotech.ktravel.gunzou.server.endpoint.here.orThrowNavigatorException
import com.takaotech.ktravel.gunzou.server.endpoint.search.requireWithinLimits

/**
 * `/v1/here/search/autocomplete`, served by HERE Geocoding and Search v7 Autosuggest.
 *
 * Autosuggest rather than Autocomplete because the contract promises a position on every place, and
 * Autocomplete returns none: it would take a Lookup per suggestion, paid for on every keystroke.
 *
 * Short on purpose, like the routing endpoints: the two translations live in
 * `HereAutocompleteRequestMapping` and `HereAutocompleteResponseMapping`.
 */
class LiveHereAutocompleteEndpoint(private val clients: HereClientPool) : HereAutocompleteEndpoint {

    override suspend fun search(request: AutocompleteRequest, credentials: ProviderCredentials?): AutocompleteResponse {
        descriptor.requireWithinLimits(limit = request.limit, at = request.at, area = request.area)

        val apiKey = credentials?.apiKey
            ?: throw NavigatorException(
                code = ErrorCode.MISSING_CREDENTIALS,
                message = "${descriptor.service} cannot be served without a HERE key",
            )

        // Built before borrowing a client, so a request HERE's own DTO refuses never holds one.
        val hereRequest = request.toHereAutosuggestRequest()

        // An empty answer is not turned into an error, unlike a route: nothing matching what has been
        // typed so far is an ordinary moment of typing.
        return clients.withClient(apiKey) { client ->
            client.autosuggest.autosuggest(hereRequest)
        }.orThrowNavigatorException().toAutocompleteResponse()
    }
}
