package com.takaotech.ktravel.ui.plan.transport.component

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.RoutingProfileInfo
import com.takaotech.ktravel.domain.routing.RoutingProfileOption
import com.takaotech.ktravel.ui.plan.transport.component.ProfileRow
import com.takaotech.ktravel.ui.plan.transport.component.ReachabilityPill
import com.takaotech.ktravel.ui.plan.transport.component.RoutingModeChip
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.flag
import ktravel.composeapp.generated.resources.place
import ktravel.composeapp.generated.resources.planning_transport_arrival
import ktravel.composeapp.generated.resources.planning_transport_departure
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** One end of a leg: the word over it, the place under it, and the icon that tells the two apart. */
internal data class PlaceEndpointPreviewState(val label: StringResource, val name: String, val icon: DrawableResource)

/**
 * The departure, the arrival, an end nobody has chosen yet, and a name longer than the row.
 *
 * Label and icon travel together, so the two real pairs are kept rather than crossed. The empty
 * name is what the composer shows while the second place is still missing, and the label has to
 * hold its place with nothing under it; the long one meets a `maxLines = 1` with no overflow set,
 * so it is cut rather than trailed off.
 */
internal class PlaceEndpointPreviewParams : PreviewParameterProvider<PlaceEndpointPreviewState> {
    override val values = sequenceOf(
        PlaceEndpointPreviewState(
            label = Res.string.planning_transport_departure,
            name = "Kyoto Station",
            icon = Res.drawable.place,
        ),
        PlaceEndpointPreviewState(
            label = Res.string.planning_transport_arrival,
            name = "Fushimi Inari Taisha",
            icon = Res.drawable.flag,
        ),
        PlaceEndpointPreviewState(
            label = Res.string.planning_transport_arrival,
            name = "",
            icon = Res.drawable.flag,
        ),
        PlaceEndpointPreviewState(
            label = Res.string.planning_transport_arrival,
            name = "Fushimi Inari Taisha, Senbon Torii entrance",
            icon = Res.drawable.flag,
        ),
    )
}

/** A profile, and whether the row draws it as the chosen one. */
internal data class ProfileRowPreviewState(val option: RoutingProfileOption, val selected: Boolean)

/**
 * Every shape a profile row takes.
 *
 * The unavailable ones matter more than the available ones: the reason line under the name is why
 * the row exists instead of a dropdown entry, and it is only visible in these states.
 */
internal class ProfileRowPreviewParams : PreviewParameterProvider<ProfileRowPreviewState> {
    override val values = sequenceOf(
        ProfileRowPreviewState(
            option = RoutingProfileOption(HERE_ROUTING, ProfileAvailability.Available),
            selected = true,
        ),
        ProfileRowPreviewState(
            option = RoutingProfileOption(HERE_TRANSIT, ProfileAvailability.Available),
            selected = false,
        ),
        ProfileRowPreviewState(
            option = RoutingProfileOption(HERE_TRANSIT, ProfileAvailability.NotServed),
            selected = false,
        ),
        ProfileRowPreviewState(
            option = RoutingProfileOption(HERE_TRANSIT, ProfileAvailability.MissingApiKey),
            selected = false,
        ),
        ProfileRowPreviewState(
            option = RoutingProfileOption(
                HERE_ROUTING,
                ProfileAvailability.NavigatorUnreachable("connection refused"),
            ),
            selected = false,
        ),
        // A profile from a newer navigator: no localized name, no description, no icons.
        ProfileRowPreviewState(
            option = RoutingProfileOption(UNKNOWN_PROFILE, ProfileAvailability.Available),
            selected = false,
        ),
    )
}

/** What the pill knows about the navigator at one moment. */
internal data class ReachabilityPillPreviewState(
    val isChecking: Boolean,
    val isReachable: Boolean,
    val latencyMillis: Long?,
)

/** Checking, answered, answered without a timing, and silent. */
internal class ReachabilityPillPreviewParams : PreviewParameterProvider<ReachabilityPillPreviewState> {
    override val values = sequenceOf(
        ReachabilityPillPreviewState(isChecking = true, isReachable = false, latencyMillis = null),
        ReachabilityPillPreviewState(isChecking = false, isReachable = true, latencyMillis = 87),
        ReachabilityPillPreviewState(isChecking = false, isReachable = true, latencyMillis = null),
        ReachabilityPillPreviewState(isChecking = false, isReachable = false, latencyMillis = null),
    )
}

/** One mode, as the chip is asked to draw it. */
internal data class RoutingModeChipPreviewState(val mode: RoutingMode, val selected: Boolean, val enabled: Boolean)

/**
 * The chip states, plus both fallbacks.
 *
 * PEDESTRIAN has a name and no icon, HIKING has neither: a chip that reads as a word rather than as
 * a wrong picture, and one that reads as its raw identifier, are both normal answers.
 */
internal class RoutingModeChipPreviewParams : PreviewParameterProvider<RoutingModeChipPreviewState> {
    override val values = sequenceOf(
        RoutingModeChipPreviewState(mode = RoutingMode("CAR"), selected = true, enabled = true),
        RoutingModeChipPreviewState(mode = RoutingMode("SUBWAY"), selected = false, enabled = true),
        RoutingModeChipPreviewState(mode = RoutingMode("PEDESTRIAN"), selected = false, enabled = true),
        RoutingModeChipPreviewState(mode = RoutingMode("FERRY"), selected = false, enabled = false),
        RoutingModeChipPreviewState(mode = RoutingMode("HIKING"), selected = false, enabled = true),
    )
}

internal val HERE_ROUTING = RoutingProfileInfo(
    id = RoutingProfileId(provider = "here", profile = "routing"),
    displayName = "HERE Routing",
    options = RoutingOptionsSpec.RoutingSingleMode(
        modes = listOf(RoutingMode("CAR"), RoutingMode("TRUCK"), RoutingMode("TAXI")),
        maxAlternatives = 3,
        modesSupportingShortest = setOf(RoutingMode("CAR"), RoutingMode("TRUCK")),
        supportsTolls = true,
    ),
    requiresApiKey = true,
)

internal val HERE_TRANSIT = RoutingProfileInfo(
    id = RoutingProfileId(provider = "here", profile = "transit"),
    displayName = "HERE Transit",
    options = RoutingOptionsSpec.TransitFilter(
        modes = listOf(
            RoutingMode("SUBWAY"),
            RoutingMode("BUS"),
            RoutingMode("REGIONAL_TRAIN"),
            RoutingMode("FERRY"),
        ),
        maxAlternatives = 5,
    ),
    requiresApiKey = true,
)

internal val UNKNOWN_PROFILE = RoutingProfileInfo(
    id = RoutingProfileId(provider = "gunzou", profile = "hiking"),
    displayName = "Gunzou Hiking",
    options = RoutingOptionsSpec.RoutingSingleMode(
        modes = listOf(RoutingMode("HIKING")),
        maxAlternatives = 1,
    ),
)
