package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.search.model.GeoArea
import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.presentation.place.MapCamera
import com.takaotech.ktravel.ui.shared.map.MAP_STYLE_URI
import com.takaotech.ktravel.ui.shared.map.rememberMapReady
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.FeaturesClickHandler
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/** Base style of the search map, hoisted so it is not reallocated on every recomposition. */
private val SEARCH_MAP_STYLE = BaseStyle.Uri(MAP_STYLE_URI)

/** The feature property a marker carries its candidate's id in. */
private const val MARKER_ID_PROPERTY = "id"

/**
 * The map the traveller browses while adding places: every place of the list on show as a marker,
 * the selected ones drawn larger and in the primary colour.
 *
 * It reports the area it frames only once the camera settles, so a pan is one request rather than
 * one per frame. [cameraRequest] moves it; a new request for the same place moves it again.
 *
 * @param places The places of the list on show.
 * @param selected The places picked so far, drawn whether or not they are in [places].
 * @param isGesturesEnabled `false` while a sheet is dragged over the map, so the two do not compete.
 * @param onMarkerClick A marker was tapped.
 * @param onViewportChange The camera settled on a new area, with its centre.
 * @param onMapClick The map was tapped away from any marker.
 */
// TODO drop a custom point with a long press on the map (`onMapLongClick`) once the screen offers
//  it; until then typed coordinates are the only way to add a point with no name.
@Composable
internal fun PlaceSearchMap(
    initialCamera: MapCamera,
    cameraRequest: MapCamera?,
    places: ImmutableList<PlaceCandidate>,
    selected: ImmutableList<PlaceCandidate>,
    isGesturesEnabled: Boolean,
    onMarkerClick: (PlaceCandidate) -> Unit,
    onViewportChange: (area: GeoArea, center: GeoCoordinate) -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMapReady = rememberMapReady()
    if (LocalInspectionMode.current || !isMapReady) {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant) {}
        return
    }

    val cameraState = rememberCameraState(firstPosition = initialCamera.toCameraPosition())
    val currentOnViewportChange by rememberUpdatedState(onViewportChange)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)

    LaunchedEffect(cameraRequest) {
        cameraRequest?.let { cameraState.animateTo(it.toCameraPosition()) }
    }

    LaunchedEffect(cameraState) {
        snapshotFlow { cameraState.viewport?.takeUnless { cameraState.isCameraMoving } }
            .filterNotNull()
            .distinctUntilChanged { old, new -> old.visibleBoundingBox == new.visibleBoundingBox }
            .collect { viewport ->
                val box = viewport.visibleBoundingBox
                val target = cameraState.position.target
                currentOnViewportChange(
                    GeoArea(west = box.west, south = box.south, east = box.east, north = box.north),
                    GeoCoordinate(lat = target.latitude, lng = target.longitude),
                )
            }
    }

    val selectedIds = remember(selected) { selected.mapTo(HashSet()) { it.id } }
    val byId = remember(places, selected) { (places + selected).associateBy { it.id } }
    val unselectedFeatures = remember(places, selectedIds) { places.filter { it.id !in selectedIds }.toFeatures() }
    val selectedFeatures = remember(selected) { selected.toFeatures() }
    val markerClick: FeaturesClickHandler = { features ->
        val id = features.firstOrNull()?.properties?.get(MARKER_ID_PROPERTY)?.jsonPrimitive?.contentOrNull
        byId[id]?.let(currentOnMarkerClick)
        ClickResult.Consume
    }

    val colors = MaterialTheme.colorScheme
    MaplibreMap(
        modifier = modifier,
        baseStyle = SEARCH_MAP_STYLE,
        cameraState = cameraState,
        options = MapOptions(
            gestureOptions = if (isGesturesEnabled) GestureOptions.Standard else GestureOptions.AllDisabled,
        ),
        onMapClick = { _, _ ->
            onMapClick()
            ClickResult.Pass
        },
    ) {
        CircleLayer(
            id = "place-markers",
            source = rememberGeoJsonSource(data = GeoJsonData.Features(unselectedFeatures)),
            radius = const(7.dp),
            color = const(colors.surface),
            strokeColor = const(colors.primary),
            strokeWidth = const(2.5.dp),
            onClick = markerClick,
        )
        CircleLayer(
            id = "selected-place-markers",
            source = rememberGeoJsonSource(data = GeoJsonData.Features(selectedFeatures)),
            radius = const(9.dp),
            color = const(colors.primary),
            strokeColor = const(colors.onPrimary),
            strokeWidth = const(2.5.dp),
            onClick = markerClick,
        )
    }
}

private fun MapCamera.toCameraPosition() = CameraPosition(
    target = Position(longitude = target.lng, latitude = target.lat),
    zoom = zoom,
)

private fun List<PlaceCandidate>.toFeatures(): FeatureCollection<Point, JsonObject> = FeatureCollection(
    map { candidate ->
        Feature(
            geometry = Point(Position(longitude = candidate.coordinate.lng, latitude = candidate.coordinate.lat)),
            properties = buildJsonObject { put(MARKER_ID_PROPERTY, candidate.id) },
        )
    },
)
