package com.takaotech.ktravel.gunzou.server.endpoint.here

import com.takaotech.gunzou.api.catalog.NavigatorProfile
import com.takaotech.gunzou.api.catalog.ProviderProfileDescriptor
import com.takaotech.gunzou.api.here.HereTransitRouteRequest
import com.takaotech.gunzou.api.response.TransitJourneyResponse
import com.takaotech.ktravel.gunzou.server.endpoint.NavigationEndpoint

/**
 * Whatever is mounted at `/v1/here/transit`.
 *
 * A separate path from [HereRoutingEndpoint] because it is a separate upstream API — a different
 * host, a different request, a different answer — and that is the rule the contract follows: one
 * path per distinct provider API, never one per mode of transport.
 *
 * The answer is separate for the same reason. It was one shape for a while, and what that bought was
 * every road section carrying an empty list of stops and every journey step an empty list of tolls;
 * see [com.takaotech.gunzou.api.response.TransitJourneyResponse] for how the two differ.
 */
interface HereTransitEndpoint : NavigationEndpoint<HereTransitRouteRequest, TransitJourneyResponse> {
    override val descriptor: ProviderProfileDescriptor get() = NavigatorProfile.HereTransit.descriptor
}
