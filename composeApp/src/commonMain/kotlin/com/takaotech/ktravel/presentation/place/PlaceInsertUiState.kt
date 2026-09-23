package com.takaotech.ktravel.presentation.place

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.takaotech.ktravel.domain.search.PlaceSearchProvider
import com.takaotech.ktravel.domain.search.PlaceSearchProviderOption
import com.takaotech.ktravel.domain.search.model.GeoArea
import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Everything the «add places» screen draws.
 *
 * @property query What is typed in the search bar.
 * @property suggestions The first few search results, for the dropdown under the bar.
 * @property coordinateCandidate The place [query] spells out when it is a `lat, lng` pair.
 * @property search The full answer to [query], for the list area.
 * @property providers Every search provider this build knows, selectable or not.
 * @property selectedProvider The provider searches are sent to.
 * @property category The kind of place the framed area is narrowed to, or every kind when `null`.
 * @property nearby The places inside the framed area of the map.
 * @property selected The places that will be added on confirm, in the order they were picked.
 * @property isInventoryOpen Whether the list area shows [selected] instead of a list of places.
 * @property detail The place whose card is open over the map.
 * @property initialCamera Where the map opens.
 * @property cameraRequest The latest place the map was asked to move to; a new value means a new move.
 * @property isSaving Whether the selection is being written to the trip.
 * @property eventSink Where the screen sends what the traveller did.
 */
@Immutable
data class PlaceInsertUiState(
    val query: String = "",
    val suggestions: ImmutableList<PlaceCandidate> = persistentListOf(),
    val coordinateCandidate: PlaceCandidate? = null,
    val search: PlaceListState = PlaceListState(),
    val providers: ImmutableList<PlaceSearchProviderOption> = persistentListOf(),
    val selectedProvider: PlaceSearchProvider? = null,
    val category: PlaceCategory? = null,
    val nearby: PlaceListState = PlaceListState(),
    val selected: ImmutableList<PlaceCandidate> = persistentListOf(),
    val isInventoryOpen: Boolean = false,
    val detail: PlaceCandidate? = null,
    val initialCamera: MapCamera = MapCamera.DEFAULT,
    val cameraRequest: MapCamera? = null,
    val isSaving: Boolean = false,
    val eventSink: (PlaceInsertEvent) -> Unit = {},
) : CircuitUiState {

    /** Which list the area beside, or under, the map shows. */
    val listContent: PlaceListContent
        get() = when {
            isInventoryOpen -> PlaceListContent.INVENTORY
            query.isNotBlank() -> PlaceListContent.SEARCH_RESULTS
            else -> PlaceListContent.NEARBY
        }

    /** Whether confirming would add anything. */
    val canConfirm: Boolean get() = selected.isNotEmpty() && !isSaving

    /** Whether the place with [id] is part of the selection. */
    fun isSelected(id: String): Boolean = selected.any { it.id == id }
}

/** The three lists that take turns in the same area of the screen. */
enum class PlaceListContent {
    /** The places inside the framed area of the map. */
    NEARBY,

    /** The answer to what is typed in the search bar. */
    SEARCH_RESULTS,

    /** The places picked so far. */
    INVENTORY,
}

/**
 * A list of places being fetched.
 *
 * The previous places stay while a new request is running, so the list does not flash empty at every
 * keystroke or every pan of the map.
 *
 * @property places The places of the last answer.
 * @property isLoading Whether a request is running.
 * @property problem Why the last request produced nothing, or `null` when it did not fail.
 */
@Immutable
data class PlaceListState(
    val places: ImmutableList<PlaceCandidate> = persistentListOf(),
    val isLoading: Boolean = false,
    val problem: PlaceSearchProblem? = null,
)

/** Why a list of places could not be filled, each with its own remedy. */
enum class PlaceSearchProblem {
    /** The provider needs the trip's API key, and there is none or it was refused. */
    MISSING_API_KEY,

    /** The navigator did not answer. */
    NAVIGATOR_UNREACHABLE,

    /** The navigator refused the app. */
    NOT_AUTHENTICATED,

    /** The provider is throttling this caller. */
    RATE_LIMITED,

    /** The provider is not answering, or the navigator does not serve it. */
    PROVIDER_UNAVAILABLE,

    /** Anything else. */
    UNEXPECTED,
}

/**
 * A position of the map camera.
 *
 * @property target The point the camera looks at.
 * @property zoom The zoom level, as MapLibre counts it.
 * @property requestId Distinguishes two requests for the same place, so centring on a place twice
 *   moves the map back to it after the traveller panned away.
 */
@Immutable
data class MapCamera(val target: GeoCoordinate, val zoom: Double, val requestId: Int = 0) {
    /** The zooms the screen moves to, and the place it opens on without a better one. */
    companion object {
        /** Zoom close enough to tell the streets around one place apart. */
        const val PLACE_ZOOM: Double = 14.0

        /** Zoom showing a city and its surroundings. */
        const val AREA_ZOOM: Double = 11.0

        /** Where the map opens when the trip has no place to open on: Italy as a whole. */
        val DEFAULT: MapCamera = MapCamera(target = GeoCoordinate(lat = 42.5, lng = 12.5), zoom = 5.0)
    }
}

/** What the traveller does on the «add places» screen. */
sealed interface PlaceInsertEvent : CircuitUiEvent {
    /** The text in the search bar changed to [query]. */
    data class QueryChanged(val query: String) : PlaceInsertEvent

    /** The search bar was emptied, which brings the framed area back in the list. */
    data object QueryCleared : PlaceInsertEvent

    /** A row of the dropdown was chosen: [candidate] is a search result or the typed coordinates. */
    data class SuggestionChosen(val candidate: PlaceCandidate) : PlaceInsertEvent

    /** Searches are to be sent to [provider] from now on. */
    data class ProviderSelected(val provider: PlaceSearchProvider) : PlaceInsertEvent

    /** The framed area was narrowed to [category], or widened back to every kind with `null`. */
    data class CategorySelected(val category: PlaceCategory?) : PlaceInsertEvent

    /** The map settled on [area], looking at [center]. */
    data class ViewportChanged(val area: GeoArea, val center: GeoCoordinate) : PlaceInsertEvent

    /** [candidate] was added to the selection, or removed when it was already in it. */
    data class SelectionToggled(val candidate: PlaceCandidate) : PlaceInsertEvent

    /** The inventory of selected places was opened or closed. */
    data object InventoryToggled : PlaceInsertEvent

    /** The card of [candidate] was opened over the map. */
    data class DetailOpened(val candidate: PlaceCandidate) : PlaceInsertEvent

    /** The open card was closed. */
    data object DetailDismissed : PlaceInsertEvent

    /** The map was asked to move to [candidate]. */
    data class CenterOn(val candidate: PlaceCandidate) : PlaceInsertEvent

    /** The selection is to be added to the trip. */
    data object Confirm : PlaceInsertEvent

    /** The traveller left without adding anything. */
    data object Exit : PlaceInsertEvent
}
