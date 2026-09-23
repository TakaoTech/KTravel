package com.takaotech.ktravel.ui.plan.day.placestep.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.shared.map.MAP_STYLE_URI
import com.takaotech.ktravel.ui.shared.map.rememberMapReady
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
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/** Opening zoom of the place map: close enough to show the surrounding streets. */
internal const val MARKER_ZOOM = 14.0

/** Base style of the place map, hoisted so it is not reallocated on every recomposition. */
internal val MAP_BASE_STYLE = BaseStyle.Uri(MAP_STYLE_URI)

/** The map is a still picture of the place: the itinerary is where the map is driven. */
internal val MAP_OPTIONS = MapOptions(gestureOptions = GestureOptions.AllDisabled)

/**
 * Map of the place: a single marker, centred on first composition and re-centred whenever the
 * step's coordinates change.
 *
 * Held back until the screen has finished being animated in (see [rememberMapReady]); the
 * placeholder keeps the space so nothing jumps.
 */
@Composable
internal fun PlaceMap(lat: Double, lng: Double, modifier: Modifier = Modifier) {
    val isMapReady = rememberMapReady()

    if (LocalInspectionMode.current || !isMapReady) {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant) {}
        return
    }

    val firstPosition = remember {
        CameraPosition(target = Position(longitude = lng, latitude = lat), zoom = MARKER_ZOOM)
    }
    val cameraState = rememberCameraState(firstPosition = firstPosition)

    LaunchedEffect(lat, lng) {
        val position = CameraPosition(
            target = Position(longitude = lng, latitude = lat),
            zoom = MARKER_ZOOM,
        )
        // The camera opens on the step already: only a later change of coordinates moves it.
        if (position != firstPosition) cameraState.position = position
    }

    MaplibreMap(
        modifier = modifier,
        baseStyle = MAP_BASE_STYLE,
        cameraState = cameraState,
        options = MAP_OPTIONS,
    ) {
        val markerSource = rememberGeoJsonSource(
            data = GeoJsonData.Features(Point(Position(longitude = lng, latitude = lat))),
        )
        CircleLayer(
            id = "marker",
            source = markerSource,
            radius = const(8.dp),
            color = const(Color.Red),
            strokeColor = const(Color.White),
            strokeWidth = const(2.dp),
        )
    }
}
