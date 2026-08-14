package com.takaotech.navigator.api.catalog

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.here.HereTransportMode

/**
 * Every profile this version of the contract supports.
 *
 * This is the half of the catalog that does not travel: a client and a server built against the same
 * contract already know which profiles exist and what may be asked of each. What the client cannot
 * know is which of them a particular navigator actually mounts, and that is what `GET /v1/profiles`
 * answers. Subtracting one from the other is how a caller tells "this deployment does not serve it"
 * from "this version does not have it", which are different problems with different remedies.
 *
 * Keeping the requestable modes here rather than in [ProviderProfileDescriptor] is deliberate. They
 * are declared in the vocabulary of the profile's own upstream API — [HereTransportMode] for the road
 * profile, [TransitMode] for the transit one — and those vocabularies must not be merged into a
 * single set. `bus`, `privateBus` and `pedestrian` appear in both with the same spelling and do not
 * mean the same thing: HERE's road `bus` is a coach the traveller drives, its transit `bus` is a
 * scheduled service; road `pedestrian` is a route you ask for, transit walking is how the answer
 * describes the legs between stops. A common enum would spell them once and lose exactly the
 * distinction that makes them two endpoints.
 *
 * A profile is added here in the same change that adds its request model, so a UI driven by this list
 * shows a new engine as soon as the contract can express it — greyed out until a navigator serves it.
 */
sealed class NavigatorProfile {

    /** What the navigator publishes about this profile. */
    abstract val descriptor: ProviderProfileDescriptor

    /** Its identity, which is what a catalog response is matched on. */
    val id: ProfileKey get() = ProfileKey(descriptor.provider, descriptor.profile)

    /**
     * The HERE road profile: one request, one vehicle, chosen from [modes].
     *
     * Limits from the HERE Routing v8 documentation: six alternatives and twenty via waypoints. They
     * are enforced before any upstream call, so a request that would be rejected is not paid for.
     */
    data object HereCar : NavigatorProfile() {

        override val descriptor: ProviderProfileDescriptor = ProviderProfileDescriptor(
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

        /**
         * What may be asked for, as a single choice: HERE's `transportMode` is a required parameter
         * with exactly one value.
         */
        val modes: List<HereTransportMode> = HereTransportMode.entries

        /**
         * The modes HERE refuses to optimize for distance.
         *
         * The Routing v8 specification supports `routingMode=short` on cars, trucks and taxis only;
         * asking for it on any other mode is a request the provider rejects, so the choice is not
         * offered rather than being sent and refused.
         */
        val modesSupportingShortest: Set<HereTransportMode> = setOf(
            HereTransportMode.CAR,
            HereTransportMode.TRUCK,
        )
    }

    /**
     * The HERE public transport profile: a journey between two places, filtered by [modeFilter].
     *
     * Limits from the HERE Public Transit v8 documentation: at most five alternatives, and no
     * intermediate stops — a journey is planned between two places and the transfers are the
     * provider's to choose.
     */
    data object HereTransit : NavigatorProfile() {

        override val descriptor: ProviderProfileDescriptor = ProviderProfileDescriptor(
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

        /**
         * What may be asked for, as a *filter* rather than a choice: HERE's `modes` is an optional
         * set restricting which vehicles the answer may use, and an empty filter admits them all.
         *
         * [TransitMode.OTHER] is excluded because it carries no meaning in a request: it exists so a
         * vehicle this contract does not name yet still decodes in a response.
         */
        val modeFilter: List<TransitMode> = TransitMode.entries.filterNot { it == TransitMode.OTHER }
    }

    companion object {

        /**
         * Every profile, in the order a selector should offer them.
         *
         * Lazy, and it has to be. This companion is initialized when [NavigatorProfile] is first
         * touched, which happens *before* the objects nested inside it: building the list eagerly
         * captures each of them while its own initializer has not run yet, and the list ends up full
         * of nulls that no type in the signature admits.
         */
        val ALL: List<NavigatorProfile> by lazy { listOf(HereCar, HereTransit) }

        /** The profile with this identity, or `null` when the caller knows one this version does not. */
        fun find(provider: ProviderId, profile: ProviderProfile): NavigatorProfile? =
            ALL.firstOrNull { it.descriptor.provider == provider && it.descriptor.profile == profile }

        /** The profile a served [descriptor] refers to, matched on identity rather than on contents. */
        fun find(descriptor: ProviderProfileDescriptor): NavigatorProfile? =
            find(descriptor.provider, descriptor.profile)
    }
}

/**
 * What identifies a profile across the wire and inside a client.
 *
 * A pair rather than a single string because the two halves answer different questions — who computes
 * the route, and which of their APIs does it — and a caller groups by the first while dispatching on
 * both.
 */
data class ProfileKey(val provider: ProviderId, val profile: ProviderProfile) {
    override fun toString(): String = "$provider/$profile"
}
