package com.takaotech.ktravel.gunzou.server.endpoint.here.search

import com.takaotech.gunzou.api.search.SearchProfile
import com.takaotech.gunzou.api.search.SearchProfileDescriptor
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteRequest
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteResponse
import com.takaotech.ktravel.gunzou.server.endpoint.search.SearchEndpoint

/**
 * Whatever is mounted at `/v1/here/search/autocomplete`.
 *
 * An interface of its own so the path has a stable type to be resolved by: every autocomplete
 * provider is a `SearchEndpoint<AutocompleteRequest, AutocompleteResponse>`, which a container keyed
 * on the erased class could not tell apart once Photon sits beside it.
 *
 * The descriptor comes from [SearchProfile.HereAutocomplete], which the contract module owns so that
 * a client knows it without asking.
 */
interface HereAutocompleteEndpoint : SearchEndpoint<AutocompleteRequest, AutocompleteResponse> {
    override val descriptor: SearchProfileDescriptor get() = SearchProfile.HereAutocomplete.descriptor
}
