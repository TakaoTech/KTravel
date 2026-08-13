package com.takaotech.ktravel.endpoint.here

import com.takaotech.ktravel.endpoint.NavigationEndpoint
import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.catalog.ProviderProfileDescriptor
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.here.HereCarRouteRequest

/**
 * Whatever is mounted at `/v1/here/car`.
 *
 * The interface exists so the path has one stable type to be resolved by, independent of which
 * implementation is behind it: a deterministic fake while the routes are being built, the real HERE
 * client afterwards. Without it, both would be a `NavigationEndpoint<HereCarRouteRequest>`, which
 * erases to the same class and cannot be told apart by a container that keys on it.
 *
 * [DESCRIPTOR] belongs to the interface and not to an implementation for the same reason: what the
 * catalog advertises is a property of the profile, and swapping the fake for the real client must
 * not change what clients were told.
 */
interface HereCarEndpoint : NavigationEndpoint<HereCarRouteRequest> {
    override val descriptor: ProviderProfileDescriptor get() = DESCRIPTOR

    /** The published capabilities of the HERE road profile. */
    companion object {
        /**
         * Limits taken from the HERE Routing v8 documentation: six alternatives and twenty via
         * waypoints. They are enforced in [com.takaotech.ktravel.endpoint.requireWithinLimits]
         * before any upstream call, so a request that would be rejected is not paid for.
         */
        val DESCRIPTOR: ProviderProfileDescriptor = ProviderProfileDescriptor(
            provider = ProviderId.HERE,
            profile = ProviderProfile.CAR,
            path = NavigatorApi.HERE_CAR,
            displayName = "HERE road routing",
            supportedModes = listOf(
                TravelMode.CAR,
                TravelMode.TRUCK,
                TravelMode.TAXI,
                TravelMode.BUS,
                TravelMode.PEDESTRIAN,
                TravelMode.BICYCLE,
                TravelMode.SCOOTER,
            ),
            maxAlternatives = 6,
            maxVia = 20,
            supportsArriveBy = true,
            supportsTolls = true,
            requiresApiKey = true,
        )
    }
}
