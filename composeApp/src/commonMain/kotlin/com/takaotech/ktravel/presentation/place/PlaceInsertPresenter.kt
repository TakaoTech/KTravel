package com.takaotech.ktravel.presentation.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.search.NearbyPlacesService
import com.takaotech.ktravel.domain.search.PlaceSearchCatalog
import com.takaotech.ktravel.domain.search.PlaceSearchFailure
import com.takaotech.ktravel.domain.search.PlaceSearchProviderOption
import com.takaotech.ktravel.domain.search.PlaceSearchQuery
import com.takaotech.ktravel.domain.search.PlaceSearchService
import com.takaotech.ktravel.domain.search.model.GeoArea
import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import com.takaotech.ktravel.presentation.plan.day.AddPlaceScreen
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** How long typing, or moving the map, has to pause before a request is sent. */
internal val PLACE_SEARCH_DEBOUNCE: Duration = 330.milliseconds

/** How many search results the dropdown under the search bar shows. */
internal const val SUGGESTION_COUNT = 5

/**
 * The «add places» screen: search, browse the framed area, pick several places, add them at once.
 *
 * Searches go out as the traveller types, once typing pauses for [PLACE_SEARCH_DEBOUNCE]: a new
 * keystroke restarts the effect, which cancels the request that was waiting or running. The framed
 * area is refreshed the same way when the map settles.
 *
 * The selection lives here, not in the lists, because the lists come and go — a search result picked
 * a moment ago is no longer on screen once the query is cleared, and must still be added.
 */
@CircuitInject(AddPlaceScreen::class, AppScope::class)
@Composable
fun PlaceInsertPresenter(
    screen: AddPlaceScreen,
    navigator: Navigator,
    planningGraphStore: PlanningGraphStore,
): PlaceInsertUiState {
    val graph = remember(screen.travelId) { planningGraphStore.getOrCreate(screen.travelId) }
    val scope = rememberCoroutineScope()
    val initialCamera = remember(graph) { graph.travelPlanRepository.planningState.value.openingCamera(screen.dayId) }

    val query = rememberRetained { mutableStateOf("") }
    val selectedProviderId = rememberRetained { mutableStateOf<String?>(null) }
    val category = rememberRetained { mutableStateOf<PlaceCategory?>(null) }
    val viewport = rememberRetained { mutableStateOf<GeoArea?>(null) }
    val center = rememberRetained { mutableStateOf(initialCamera.target) }
    val selection = rememberRetained { mutableStateOf<PersistentList<PlaceCandidate>>(persistentListOf()) }
    val isInventoryOpen = rememberRetained { mutableStateOf(false) }
    val detail = rememberRetained { mutableStateOf<PlaceCandidate?>(null) }
    val camera = rememberCameraRequests()
    var isSaving by rememberRetained { mutableStateOf(false) }

    val catalog = rememberSearchCatalog(graph.placeSearchService)
    val providerOption = catalog?.let { loaded ->
        loaded.options.firstOrNull { it.provider.id == selectedProviderId.value }
            ?: loaded.selectable.firstOrNull()
            ?: loaded.options.firstOrNull()
    }
    val coordinate = remember(query.value) { CoordinateParser.parse(query.value) }
    val search = rememberSearchResults(
        service = graph.placeSearchService,
        query = query.value,
        isCatalogLoaded = catalog != null,
        providerOption = providerOption,
        isCoordinate = coordinate != null,
        center = center,
    )
    val nearby = rememberNearbyPlaces(graph.nearbyPlacesService, viewport.value, category.value)

    return PlaceInsertUiState(
        query = query.value,
        suggestions = search.places.take(SUGGESTION_COUNT).toImmutableList(),
        coordinateCandidate = coordinate?.let { CoordinateParser.candidateFor(it) },
        search = search,
        providers = catalog?.options.orEmpty().toImmutableList(),
        selectedProvider = providerOption?.provider,
        category = category.value,
        nearby = nearby,
        selected = selection.value,
        isInventoryOpen = isInventoryOpen.value,
        detail = detail.value,
        initialCamera = initialCamera,
        cameraRequest = camera.latest,
        isSaving = isSaving,
    ) { event ->
        when (event) {
            is PlaceInsertEvent.QueryChanged -> {
                query.value = event.query
                isInventoryOpen.value = false
            }

            PlaceInsertEvent.QueryCleared -> query.value = ""

            is PlaceInsertEvent.SuggestionChosen -> {
                selection.value = selection.value.withAdded(event.candidate)
                camera.moveTo(event.candidate.coordinate)
                // Typed coordinates have nothing left to search for once they are a place.
                if (event.candidate.source == PlaceCandidateSource.COORDINATES) query.value = ""
            }

            is PlaceInsertEvent.ProviderSelected -> selectedProviderId.value = event.provider.id

            is PlaceInsertEvent.CategorySelected -> category.value = event.category

            is PlaceInsertEvent.ViewportChanged -> {
                viewport.value = event.area
                center.value = event.center
            }

            is PlaceInsertEvent.SelectionToggled -> selection.value = selection.value.withToggled(event.candidate)

            PlaceInsertEvent.InventoryToggled -> isInventoryOpen.value = !isInventoryOpen.value

            is PlaceInsertEvent.DetailOpened -> detail.value = event.candidate

            PlaceInsertEvent.DetailDismissed -> detail.value = null

            is PlaceInsertEvent.CenterOn -> camera.moveTo(event.candidate.coordinate)

            PlaceInsertEvent.Confirm -> if (selection.value.isNotEmpty() && !isSaving) {
                isSaving = true
                val places = selection.value
                scope.launch {
                    try {
                        graph.savePlaceUseCase(places, screen.dayId)
                        navigator.pop()
                    } finally {
                        isSaving = false
                    }
                }
            }

            PlaceInsertEvent.Exit -> navigator.pop()
        }
    }
}

/** The catalog of search providers, loaded once for the lifetime of the screen. */
@Composable
private fun rememberSearchCatalog(service: PlaceSearchService): PlaceSearchCatalog? {
    var catalog by rememberRetained { mutableStateOf<PlaceSearchCatalog?>(null) }
    LaunchedEffect(service) {
        if (catalog == null) catalog = service.catalog()
    }
    return catalog
}

/**
 * The answer to [query], sent to [providerOption] once typing pauses.
 *
 * [center] is read when the request leaves rather than keyed on, so panning the map does not send
 * the same search again.
 */
@Composable
private fun rememberSearchResults(
    service: PlaceSearchService,
    query: String,
    isCatalogLoaded: Boolean,
    providerOption: PlaceSearchProviderOption?,
    isCoordinate: Boolean,
    center: State<GeoCoordinate>,
): PlaceListState {
    var results by rememberRetained { mutableStateOf(PlaceListState()) }
    val language = remember { Locale.current.toLanguageTag() }
    val currentCenter by rememberUpdatedState(center.value)

    LaunchedEffect(query, providerOption, isCoordinate, isCatalogLoaded) {
        val text = query.trim()
        // Still loading the catalog: this effect restarts when it arrives.
        if (text.isEmpty() || isCoordinate || !isCatalogLoaded) {
            results = PlaceListState()
            return@LaunchedEffect
        }

        delay(PLACE_SEARCH_DEBOUNCE)

        if (providerOption == null || !providerOption.isSelectable) {
            results = PlaceListState(problem = providerOption.unavailableProblem())
            return@LaunchedEffect
        }

        results = results.copy(isLoading = true, problem = null)
        results = fetch {
            service.autocomplete(
                provider = providerOption.provider,
                query = PlaceSearchQuery(text = text, near = currentCenter, language = language),
            )
        }
    }
    return results
}

/** The places inside [area], of [category], fetched once the map settles. */
@Composable
private fun rememberNearbyPlaces(
    service: NearbyPlacesService,
    area: GeoArea?,
    category: PlaceCategory?,
): PlaceListState {
    var places by rememberRetained { mutableStateOf(PlaceListState()) }
    LaunchedEffect(area, category) {
        if (area == null) return@LaunchedEffect
        delay(PLACE_SEARCH_DEBOUNCE)

        places = places.copy(isLoading = true, problem = null)
        places = fetch { service.placesIn(area, category) }
    }
    return places
}

/** The latest request to move the map, each one numbered so asking twice moves it twice. */
private class CameraRequests(private val latestState: MutableState<MapCamera?>, private val count: MutableState<Int>) {
    val latest: MapCamera? get() = latestState.value

    fun moveTo(target: GeoCoordinate) {
        count.value += 1
        latestState.value = MapCamera(target = target, zoom = MapCamera.PLACE_ZOOM, requestId = count.value)
    }
}

@Composable
private fun rememberCameraRequests(): CameraRequests {
    val latest = rememberRetained { mutableStateOf<MapCamera?>(null) }
    val count = rememberRetained { mutableIntStateOf(0) }
    return remember(latest, count) { CameraRequests(latest, count) }
}

/** Runs [block], turning a search failure into the problem the list shows. */
private suspend fun fetch(block: suspend () -> List<PlaceCandidate>): PlaceListState = try {
    PlaceListState(places = block().toImmutableList())
} catch (failure: PlaceSearchFailure) {
    PlaceListState(problem = failure.toProblem())
}

private fun PersistentList<PlaceCandidate>.withAdded(candidate: PlaceCandidate): PersistentList<PlaceCandidate> =
    if (any { it.id == candidate.id }) this else adding(candidate)

private fun PersistentList<PlaceCandidate>.withToggled(candidate: PlaceCandidate): PersistentList<PlaceCandidate> =
    firstOrNull { it.id == candidate.id }?.let { removing(it) } ?: adding(candidate)

/** The problem to show when searches cannot be sent to [this] provider, or to none at all. */
private fun PlaceSearchProviderOption?.unavailableProblem(): PlaceSearchProblem = when (this?.availability) {
    ProfileAvailability.MissingApiKey -> PlaceSearchProblem.MISSING_API_KEY
    is ProfileAvailability.NavigatorUnreachable -> PlaceSearchProblem.NAVIGATOR_UNREACHABLE
    ProfileAvailability.NotServed, ProfileAvailability.Available, null -> PlaceSearchProblem.PROVIDER_UNAVAILABLE
}

private fun PlaceSearchFailure.toProblem(): PlaceSearchProblem = when (this) {
    is PlaceSearchFailure.ProviderCredentials -> PlaceSearchProblem.MISSING_API_KEY

    is PlaceSearchFailure.NavigatorUnreachable -> PlaceSearchProblem.NAVIGATOR_UNREACHABLE

    is PlaceSearchFailure.NotAuthenticated -> PlaceSearchProblem.NOT_AUTHENTICATED

    is PlaceSearchFailure.RateLimited -> PlaceSearchProblem.RATE_LIMITED

    is PlaceSearchFailure.ProviderUnavailable, is PlaceSearchFailure.UnsupportedProvider ->
        PlaceSearchProblem.PROVIDER_UNAVAILABLE

    is PlaceSearchFailure.InvalidRequest, is PlaceSearchFailure.Unexpected -> PlaceSearchProblem.UNEXPECTED
}
