package com.takaotech.ktravel.endpoint.here

import com.takaotech.ktravel.endpoint.NavigationEndpoint
import com.takaotech.navigator.api.catalog.NavigatorProfile
import com.takaotech.navigator.api.catalog.ProviderProfileDescriptor
import com.takaotech.navigator.api.here.HereTransitRouteRequest

/**
 * Whatever is mounted at `/v1/here/transit`.
 *
 * A separate path from [HereCarEndpoint] because it is a separate upstream API — a different host,
 * a different request, a different answer — and that is the rule the contract follows: one path per
 * distinct provider API, never one per mode of transport. It is also the second profile, which is
 * what makes the shared [com.takaotech.navigator.api.response.RouteResponse] worth anything: until
 * two genuinely different engines fit in it, a common response model is only a claim.
 */
interface HereTransitEndpoint : NavigationEndpoint<HereTransitRouteRequest> {
    override val descriptor: ProviderProfileDescriptor get() = NavigatorProfile.HereTransit.descriptor
}
