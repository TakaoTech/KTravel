package com.takaotech.ktravel.gunzou.server.endpoint.here.search

import com.takaotech.gunzou.api.search.SearchProfile
import com.takaotech.gunzou.api.search.SearchProfileDescriptor
import com.takaotech.gunzou.api.search.place.PlaceDetails
import com.takaotech.ktravel.gunzou.server.endpoint.search.PlaceLookupCall
import com.takaotech.ktravel.gunzou.server.endpoint.search.SearchEndpoint

/**
 * Whatever is mounted at `/v1/here/search/id/{id}`.
 *
 * An interface of its own for the same reason as [HereAutocompleteEndpoint]: every provider of the
 * service is a `SearchEndpoint<PlaceLookupCall, PlaceDetails>`, which a container keyed on the
 * erased class could not tell apart.
 *
 * The descriptor comes from [SearchProfile.HerePlaceLookup], which the contract module owns.
 */
interface HerePlaceLookupEndpoint : SearchEndpoint<PlaceLookupCall, PlaceDetails> {
    override val descriptor: SearchProfileDescriptor get() = SearchProfile.HerePlaceLookup.descriptor
}
