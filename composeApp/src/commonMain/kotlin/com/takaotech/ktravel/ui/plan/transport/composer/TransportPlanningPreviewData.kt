package com.takaotech.ktravel.ui.plan.transport.composer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.routing.RoutingCatalog
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.RoutingProfileInfo
import com.takaotech.ktravel.domain.routing.RoutingProfileOption
import com.takaotech.ktravel.presentation.plan.transport.TransportPlanningUiState

internal val PREVIEW_HERE_CAR = RoutingProfileInfo(
    id = RoutingProfileId(provider = "here", profile = "routing"),
    displayName = "HERE Routing",
    options = RoutingOptionsSpec.RoutingSingleMode(
        modes = listOf(RoutingMode("CAR"), RoutingMode("TRUCK"), RoutingMode("TAXI")),
        maxAlternatives = 3,
        modesSupportingShortest = setOf(RoutingMode("CAR"), RoutingMode("TRUCK")),
        supportsTolls = true,
    ),
    supportsArriveBy = true,
    requiresApiKey = true,
)

internal val PREVIEW_HERE_TRANSIT = RoutingProfileInfo(
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

/** A profile from a newer navigator: no localized name, no description. */
internal val PREVIEW_UNKNOWN = RoutingProfileInfo(
    id = RoutingProfileId(provider = "gunzou", profile = "hiking"),
    displayName = "Gunzou Hiking",
    options = RoutingOptionsSpec.RoutingSingleMode(
        modes = listOf(RoutingMode("HIKING")),
        maxAlternatives = 1,
    ),
)

internal val PREVIEW_CATALOG = RoutingCatalog(
    options = listOf(
        RoutingProfileOption(PREVIEW_HERE_CAR, ProfileAvailability.Available),
        RoutingProfileOption(PREVIEW_HERE_TRANSIT, ProfileAvailability.MissingApiKey),
        RoutingProfileOption(PREVIEW_UNKNOWN, ProfileAvailability.NotServed),
    ),
    navigatorVersion = "1.4.0",
    latencyMillis = 87,
)

/** The same call, unanswered: a catalog without a version is one nothing was reached for. */
internal val PREVIEW_CATALOG_UNREACHABLE =
    RoutingCatalog(options = emptyList(), navigatorVersion = null)

/**
 * The navigator block through the run of a switch to a remote one.
 *
 * The first two are the embedded navigator with and without a remote to switch to, which is the
 * only difference the second segment ever shows — a disabled half is easy to draw as an enabled
 * one. The last three are the remote navigator asking, answering and staying silent: the pill and
 * the retry button appear together, and the version line is filled or missing with them.
 */
internal class NavigatorBlockPreviewParams : PreviewParameterProvider<TransportPlanningUiState> {
    override val values = sequenceOf(
        TransportPlanningUiState(),
        TransportPlanningUiState(isRemoteConfigured = true, catalog = PREVIEW_CATALOG),
        TransportPlanningUiState(
            navigatorKind = NavigatorKind.REMOTE,
            isRemoteConfigured = true,
            isCatalogLoading = true,
        ),
        TransportPlanningUiState(
            navigatorKind = NavigatorKind.REMOTE,
            isRemoteConfigured = true,
            catalog = PREVIEW_CATALOG,
        ),
        TransportPlanningUiState(
            navigatorKind = NavigatorKind.REMOTE,
            isRemoteConfigured = true,
            catalog = PREVIEW_CATALOG_UNREACHABLE,
        ),
    )
}

/**
 * Everything the profile block puts where the rows go.
 *
 * The two failed catalogs are the pair worth keeping: the card is the same, but only the remote one
 * offers the way out of it, and a button that appears on the wrong navigator would send a traveller
 * back to where they already are. [PREVIEW_CATALOG] carries an unavailable profile of each kind, so
 * the reason lines under the names are drawn rather than assumed, and the state before any answer
 * is the one where the block is nothing but its own label.
 */
internal class ProfileBlockPreviewParams : PreviewParameterProvider<TransportPlanningUiState> {
    override val values = sequenceOf(
        TransportPlanningUiState(),
        TransportPlanningUiState(isCatalogLoading = true),
        TransportPlanningUiState(
            catalog = PREVIEW_CATALOG,
            selectedProfileId = PREVIEW_HERE_CAR.id,
        ),
        TransportPlanningUiState(
            navigatorKind = NavigatorKind.REMOTE,
            isRemoteConfigured = true,
            catalog = PREVIEW_CATALOG_UNREACHABLE,
        ),
        TransportPlanningUiState(catalog = PREVIEW_CATALOG_UNREACHABLE),
    )
}
