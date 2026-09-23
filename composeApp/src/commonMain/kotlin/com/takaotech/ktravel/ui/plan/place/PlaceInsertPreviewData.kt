package com.takaotech.ktravel.ui.plan.place

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.search.PlaceSearchProvider
import com.takaotech.ktravel.domain.search.PlaceSearchProviderOption
import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import com.takaotech.ktravel.presentation.place.CoordinateParser
import com.takaotech.ktravel.presentation.place.PlaceInsertUiState
import com.takaotech.ktravel.presentation.place.PlaceListState
import com.takaotech.ktravel.presentation.place.PlaceSearchProblem
import kotlinx.collections.immutable.persistentListOf

internal val previewHere = PlaceSearchProvider(id = "here", name = "HERE")

/** HERE usable, Photon listed but not served: the selector has to draw both. */
internal val previewProviders = persistentListOf(
    PlaceSearchProviderOption(previewHere, ProfileAvailability.Available),
    PlaceSearchProviderOption(PlaceSearchProvider(id = "photon", name = "Photon"), ProfileAvailability.NotServed),
)

internal val previewTorrazzo = PlaceCandidate(
    id = "SEARCH:torrazzo",
    title = "Torrazzo di Cremona",
    coordinate = GeoCoordinate(lat = 45.1334, lng = 10.0246),
    source = PlaceCandidateSource.SEARCH,
    locality = "Cremona",
    addressLabel = "Piazza del Comune, 26100 Cremona CR, Italia",
    category = PlaceCategory.SIGHTS_AND_MUSEUMS,
    distanceMeters = 420,
)

internal val previewTrattoria = PlaceCandidate(
    id = "NEARBY:trattoria",
    title = "Trattoria del Torrazzo",
    coordinate = GeoCoordinate(lat = 45.1345, lng = 10.0261),
    source = PlaceCandidateSource.NEARBY,
    locality = "Cremona",
    category = PlaceCategory.EAT_AND_DRINK,
    distanceMeters = 1_250,
)

/** A name longer than any row, with no category and no distance: what a bare address looks like. */
internal val previewLongAddress = PlaceCandidate(
    id = "SEARCH:long",
    title = "Via Alzaia Naviglio Pavese angolo Strada Provinciale per Binasco, civico 124",
    coordinate = GeoCoordinate(lat = 45.3301, lng = 9.1031),
    source = PlaceCandidateSource.SEARCH,
    locality = "Binasco",
)

internal val previewCoordinates = CoordinateParser.candidateFor(GeoCoordinate(lat = 45.26, lng = 9.34))

/** The screen's three list areas, with the states each can be in. */
internal class PlaceInsertPreviewParams : PreviewParameterProvider<PlaceInsertUiState> {
    override val values = sequenceOf(
        // Nothing typed, the framed area answered with nothing: the stub behind it today.
        PlaceInsertUiState(providers = previewProviders, selectedProvider = previewHere),
        // A search with results, one of them already picked.
        PlaceInsertUiState(
            query = "torrazzo",
            providers = previewProviders,
            selectedProvider = previewHere,
            search = PlaceListState(places = persistentListOf(previewTorrazzo, previewTrattoria, previewLongAddress)),
            selected = persistentListOf(previewTorrazzo),
        ),
        // A search the trip cannot pay for.
        PlaceInsertUiState(
            query = "duomo",
            providers = previewProviders,
            selectedProvider = previewHere,
            search = PlaceListState(problem = PlaceSearchProblem.MISSING_API_KEY),
        ),
        // The inventory, with the card of one place open.
        PlaceInsertUiState(
            providers = previewProviders,
            selectedProvider = previewHere,
            selected = persistentListOf(previewTorrazzo, previewCoordinates),
            isInventoryOpen = true,
            detail = previewTorrazzo,
        ),
    )
}
