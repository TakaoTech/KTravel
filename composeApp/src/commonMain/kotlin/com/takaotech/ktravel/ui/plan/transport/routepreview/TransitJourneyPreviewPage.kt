package com.takaotech.ktravel.ui.plan.transport.routepreview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.domain.routing.model.TransitJourneys
import com.takaotech.ktravel.ui.shared.map.RoutePreviewMap
import com.takaotech.ktravel.ui.shared.map.RoutePreviewPath
import com.takaotech.ktravel.ui.shared.route.transit.lineColor
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

/**
 * A journey on scheduled services: when to be where, on what, and who runs it.
 *
 * Not a list of manoeuvres, which is what this screen used to be handed and had nothing to draw
 * from — a journey has no manoeuvres, so it rendered as a column of empty headings. What a
 * traveller needs is the shape of the itinerary: how long it takes door to door, how many times
 * they change, and for each step the line, the direction and the two times that matter.
 */
@Composable
fun TransitJourneyPreviewPage(
    journeys: TransitJourneys,
    selectedJourneyIndex: Int,
    onJourneyChange: (Int) -> Unit,
    onNavigationBackClick: () -> Unit,
    onJourneyConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected by remember(journeys, selectedJourneyIndex) {
        derivedStateOf { journeys.journeys.getOrNull(selectedJourneyIndex) }
    }

    val cameraState = rememberCameraState()
    val scope = rememberCoroutineScope()
    var focus by remember { mutableStateOf<PolylineEncoderDecoder.LatLngZ?>(null) }

    LaunchedEffect(selectedJourneyIndex) { focus = null }

    val onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit = { stop ->
        focus = stop
        scope.launch {
            cameraState.animateTo(
                CameraPosition(
                    target = Position(longitude = stop.lng, latitude = stop.lat),
                    zoom = STOP_FOCUS_ZOOM,
                ),
            )
        }
    }

    RoutePreviewScaffold(
        modifier = modifier,
        alternatives = journeys.journeys.size,
        selectedIndex = selectedJourneyIndex,
        onSelect = onJourneyChange,
        onConfirm = onJourneyConfirm,
        list = {
            TransitJourneyTimelineSection(selected, onStopClick)
        },
        onNavigationBackClick = onNavigationBackClick,
        map = { enabled ->
            RoutePreviewMap(
                modifier = Modifier.fillMaxSize(),
                enabled = enabled,
                paths = selected?.steps.orEmpty()
                    .mapNotNull { step ->
                        step.polyline?.let {
                            RoutePreviewPath(
                                polyline = it,
                                color = step.lineColor(),
                            )
                        }
                    }
                    .toPersistentList(),
                cameraState = cameraState,
                focus = focus,
            )
        },
    )
}

/** Zoom applied when framing a single stop, close enough to find the entrance. */
private const val STOP_FOCUS_ZOOM = 16.0
