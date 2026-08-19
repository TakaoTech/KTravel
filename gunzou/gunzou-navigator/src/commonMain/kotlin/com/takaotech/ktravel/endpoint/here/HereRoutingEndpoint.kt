package com.takaotech.ktravel.endpoint.here

import com.takaotech.ktravel.endpoint.NavigationEndpoint
import com.takaotech.navigator.api.catalog.NavigatorProfile
import com.takaotech.navigator.api.catalog.ProviderProfileDescriptor
import com.takaotech.navigator.api.response.RoutingRouteResponse

/**
 * Whatever is mounted at `/v1/here/routing/{transportMode}`.
 *
 * The interface exists so the path has one stable type to be resolved by, independent of which
 * implementation is behind it: a deterministic fake while the routes are being built, the real HERE
 * client afterwards. Without it, both would be a `NavigationEndpoint<HereRoutingCall, …>`, which
 * erases to the same class and cannot be told apart by a container that keys on it.
 *
 * The descriptor comes from [NavigatorProfile.HereRouting] rather than being declared here: what the
 * catalog advertises is a property of the profile, which the contract module owns so that a client
 * knows it without asking. Swapping the fake for the real client must not change what callers were
 * told, and neither must the server being the only side that can read it.
 */
interface HereRoutingEndpoint : NavigationEndpoint<HereRoutingCall, RoutingRouteResponse> {
    override val descriptor: ProviderProfileDescriptor get() = NavigatorProfile.HereRouting.descriptor
}
