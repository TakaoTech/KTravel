package com.takaotech.ktravel.ui.planning.transport

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.ModeSelection
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.RoutingProfileInfo
import com.takaotech.ktravel.domain.routing.RoutingProfileOption
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_transport_status_checking
import ktravel.composeapp.generated.resources.planning_transport_status_offline
import ktravel.composeapp.generated.resources.planning_transport_status_online
import org.jetbrains.compose.resources.stringResource


@Preview(showBackground = true)
@Composable
private fun ProfileRowPreview(
    @PreviewParameter(ProfileRowPreviewParams::class) state: ProfileRowPreviewState,
) = KTravelTheme {
    Surface {
        ProfileRow(
            modifier = Modifier.padding(12.dp),
            option = state.option,
            selected = state.selected,
            onSelect = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReachabilityPillPreview(
    @PreviewParameter(ReachabilityPillPreviewParams::class) state: ReachabilityPillPreviewState,
) = KTravelTheme {
    Surface {
        ReachabilityPill(
            modifier = Modifier.padding(12.dp),
            isChecking = state.isChecking,
            isReachable = state.isReachable,
            latencyMillis = state.latencyMillis,
            checkingText = stringResource(Res.string.planning_transport_status_checking),
            reachableText = stringResource(Res.string.planning_transport_status_online),
            unreachableText = stringResource(Res.string.planning_transport_status_offline),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ModeChipPreview(
    @PreviewParameter(ModeChipPreviewParams::class) state: ModeChipPreviewState,
) = KTravelTheme {
    Surface {
        ModeChip(
            modifier = Modifier.padding(12.dp),
            mode = state.mode,
            selected = state.selected,
            enabled = state.enabled,
            onClick = {},
        )
    }
}

/** A profile, and whether the row draws it as the chosen one. */
internal data class ProfileRowPreviewState(
    val option: RoutingProfileOption,
    val selected: Boolean,
)

/**
 * Every shape a profile row takes.
 *
 * The unavailable ones matter more than the available ones: the reason line under the name is why
 * the row exists instead of a dropdown entry, and it is only visible in these states.
 */
internal class ProfileRowPreviewParams : PreviewParameterProvider<ProfileRowPreviewState> {
    override val values = sequenceOf(
        ProfileRowPreviewState(
            option = RoutingProfileOption(HERE_CAR, ProfileAvailability.Available),
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
                HERE_CAR,
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
internal class ReachabilityPillPreviewParams :
    PreviewParameterProvider<ReachabilityPillPreviewState> {
    override val values = sequenceOf(
        ReachabilityPillPreviewState(isChecking = true, isReachable = false, latencyMillis = null),
        ReachabilityPillPreviewState(isChecking = false, isReachable = true, latencyMillis = 87),
        ReachabilityPillPreviewState(isChecking = false, isReachable = true, latencyMillis = null),
        ReachabilityPillPreviewState(isChecking = false, isReachable = false, latencyMillis = null),
    )
}

/** One mode, as the chip is asked to draw it. */
internal data class ModeChipPreviewState(
    val mode: RoutingMode,
    val selected: Boolean,
    val enabled: Boolean,
)

/**
 * The chip states, plus both fallbacks.
 *
 * PEDESTRIAN has a name and no icon, HIKING has neither: a chip that reads as a word rather than as
 * a wrong picture, and one that reads as its raw identifier, are both normal answers.
 */
internal class ModeChipPreviewParams : PreviewParameterProvider<ModeChipPreviewState> {
    override val values = sequenceOf(
        ModeChipPreviewState(mode = RoutingMode("CAR"), selected = true, enabled = true),
        ModeChipPreviewState(mode = RoutingMode("SUBWAY"), selected = false, enabled = true),
        ModeChipPreviewState(mode = RoutingMode("PEDESTRIAN"), selected = false, enabled = true),
        ModeChipPreviewState(mode = RoutingMode("FERRY"), selected = false, enabled = false),
        ModeChipPreviewState(mode = RoutingMode("HIKING"), selected = false, enabled = true),
    )
}

private val HERE_CAR = RoutingProfileInfo(
    id = RoutingProfileId(provider = "here", profile = "car"),
    displayName = "HERE Routing",
    modes = listOf(RoutingMode("CAR"), RoutingMode("TRUCK"), RoutingMode("TAXI")),
    modeSelection = ModeSelection.SINGLE,
    modesSupportingShortest = setOf(RoutingMode("CAR"), RoutingMode("TRUCK")),
    supportsTolls = true,
    maxAlternatives = 3,
    requiresApiKey = true,
)

private val HERE_TRANSIT = RoutingProfileInfo(
    id = RoutingProfileId(provider = "here", profile = "transit"),
    displayName = "HERE Transit",
    modes = listOf(
        RoutingMode("SUBWAY"),
        RoutingMode("BUS"),
        RoutingMode("REGIONAL_TRAIN"),
        RoutingMode("FERRY"),
    ),
    modeSelection = ModeSelection.FILTER,
    requiresApiKey = true,
)

private val UNKNOWN_PROFILE = RoutingProfileInfo(
    id = RoutingProfileId(provider = "gunzou", profile = "hiking"),
    displayName = "Gunzou Hiking",
    modes = listOf(RoutingMode("HIKING")),
    modeSelection = ModeSelection.SINGLE,
)
