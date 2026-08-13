package com.takaotech.ktravel.endpoint.here

import com.takaotech.ktravel.endpoint.NavigationEndpoint
import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.catalog.ProviderProfileDescriptor
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TravelMode
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
    override val descriptor: ProviderProfileDescriptor get() = DESCRIPTOR

    /** The published capabilities of the HERE public transport profile. */
    companion object {
        /**
         * Limits taken from the HERE Public Transit v8 documentation: at most five alternatives, and
         * no intermediate stops — a journey is planned between two places and the transfers are the
         * provider's to choose.
         */
        val DESCRIPTOR: ProviderProfileDescriptor = ProviderProfileDescriptor(
            provider = ProviderId.HERE,
            profile = ProviderProfile.TRANSIT,
            path = NavigatorApi.HERE_TRANSIT,
            displayName = "HERE public transit",
            supportedModes = listOf(TravelMode.TRANSIT, TravelMode.PEDESTRIAN),
            maxAlternatives = 5,
            maxVia = 0,
            supportsArriveBy = true,
            supportsTolls = false,
            requiresApiKey = true,
        )
    }
}
