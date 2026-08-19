package com.takaotech.ktravel.endpoint.here

import com.takaotech.ktravel.endpoint.NavigationEndpoint
import com.takaotech.navigator.api.catalog.NavigatorProfile
import com.takaotech.navigator.api.catalog.ProviderProfileDescriptor
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import com.takaotech.navigator.api.response.TransitJourneyResponse

/**
 * Whatever is mounted at `/v1/here/transit`.
 *
 * A separate path from [HereRoutingEndpoint] because it is a separate upstream API — a different
 * host, a different request, a different answer — and that is the rule the contract follows: one
 * path per distinct provider API, never one per mode of transport.
 *
 * The answer is separate for the same reason. It was one shape for a while, and what that bought was
 * every road section carrying an empty list of stops and every journey leg an empty list of tolls;
 * see [com.takaotech.navigator.api.response.TransitJourneyResponse] for how the two differ.
 */
interface HereTransitEndpoint : NavigationEndpoint<HereTransitRouteRequest, TransitJourneyResponse> {
    override val descriptor: ProviderProfileDescriptor get() = NavigatorProfile.HereTransit.descriptor
}
