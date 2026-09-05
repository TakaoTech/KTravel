package com.takaotech.ktravel.ui.plan.day.placestep.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.takaotech.ktravel.ui.shared.map.MAP_STYLE_URI
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
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
import kotlin.time.Duration.Companion.seconds

/** Opening zoom of the place map: close enough to show the surrounding streets. */
internal const val MARKER_ZOOM = 14.0

/** Base style of the place map, hoisted so it is not reallocated on every recomposition. */
internal val MAP_BASE_STYLE = BaseStyle.Uri(MAP_STYLE_URI)

/** The map is a still picture of the place: the itinerary is where the map is driven. */
internal val MAP_OPTIONS = MapOptions(gestureOptions = GestureOptions.AllDisabled)

/**
 * How long the map waits for the navigation enter transition before giving up on it.
 *
 * Building the map costs a frame this screen cannot spare while it is being animated in, and the
 * entry being animated only reaches `RESUMED` once the transition ends. A host that never resumes
 * its content must not keep the map out forever, hence the bound.
 */
internal val MAP_TRANSITION_TIMEOUT = 1.seconds

/**
 * Map of the place: a single marker, centred on first composition and re-centred whenever the
 * step's coordinates change.
 *
 * Held back until the screen has finished being animated in: `MapLibre.getInstance` loads the
 * native library and `MapView` creates its surface, both on the main thread, and doing that mid
 * transition is what makes opening this page stutter. The placeholder keeps the space so nothing
 * jumps.
 */
@Composable
internal fun PlaceMap(lat: Double, lng: Double, modifier: Modifier = Modifier) {
    // Ready once this screen is resumed, and ready regardless after MAP_TRANSITION_TIMEOUT; never
    // goes back — a map that is up stays up.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var mapReady by remember { mutableStateOf(false) }
    LaunchedEffect(lifecycle) {
        if (!mapReady) {
            withTimeoutOrNull(MAP_TRANSITION_TIMEOUT) {
                lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            }
            mapReady = true
        }
    }

    if (LocalInspectionMode.current || !mapReady) {
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
