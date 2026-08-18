package com.takaotech.ktravel.presentation.planning.transport.options

import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.RoutingProfileInfo

/** Profiles shaped like the ones the catalog produces, for the tests around the options block. */
internal object RoutingProfileInfoFixtures {

    fun road(spec: RoutingOptionsSpec.RoadSingleMode): RoutingProfileInfo = RoutingProfileInfo(
        id = RoutingProfileId(provider = "here", profile = "routing"),
        displayName = "HERE road routing",
        options = spec,
    )

    fun transit(spec: RoutingOptionsSpec.TransitFilter): RoutingProfileInfo = RoutingProfileInfo(
        id = RoutingProfileId(provider = "here", profile = "transit"),
        displayName = "HERE public transit",
        options = spec,
    )

    fun none(): RoutingProfileInfo = RoutingProfileInfo(
        id = RoutingProfileId(provider = "gunzou", profile = "straight-line"),
        displayName = "Straight line",
        options = RoutingOptionsSpec.None,
    )
}
