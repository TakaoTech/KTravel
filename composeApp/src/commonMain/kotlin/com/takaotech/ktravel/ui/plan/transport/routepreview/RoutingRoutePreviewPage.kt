package com.takaotech.ktravel.ui.plan.transport.routepreview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.takaotech.ktravel.domain.routing.model.RoutingRoutes
import com.takaotech.ktravel.ui.shared.map.RoutePreviewMap
import com.takaotech.ktravel.ui.shared.map.RoutePreviewPath
import com.takaotech.ktravel.ui.shared.route.RoutingStepSection
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

/**
 * A road route: the manoeuvres to perform, and the line they run along.
 *
 * The whole screen is a list of instructions, which is what a road answer is. Clicking one moves
 * the camera to where it happens, resolved from the offset the navigator put on it — the reason the
 * offset travels at all.
 */
@Composable
fun RoutingRoutePreviewPage(
    routes: RoutingRoutes,
    selectedRouteIndex: Int,
    onRouteChange: (Int) -> Unit,
    onNavigationBackClick: () -> Unit,
    onRouteConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected by remember(routes, selectedRouteIndex) {
        derivedStateOf { routes.routes.getOrNull(selectedRouteIndex) }
    }

    val cameraState = rememberCameraState()
    val scope = rememberCoroutineScope()
    var focus by remember { mutableStateOf<PolylineEncoderDecoder.LatLngZ?>(null) }

    // Switching route would otherwise leave the marker on a path that is no longer drawn.
    LaunchedEffect(selectedRouteIndex) { focus = null }

    val onStepClick: (PolylineEncoderDecoder.LatLngZ) -> Unit = { step ->
        focus = step
        scope.launch {
            cameraState.animateTo(
                CameraPosition(target = Position(longitude = step.lng, latitude = step.lat), zoom = STEP_FOCUS_ZOOM),
            )
        }
    }

    RoutePreviewScaffold(
        modifier = modifier,
        alternatives = routes.routes.size,
        selectedIndex = selectedRouteIndex,
        onSelect = onRouteChange,
        onConfirm = onRouteConfirm,
        list = {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(selected?.sections.orEmpty()) { section ->
                    RoutingStepSection(
                        actions = section.actions,
                        polyline = section.polyline,
                        onActionClick = onStepClick,
                    )
                }
            }
        },
        onNavigationBackClick = onNavigationBackClick,
        map = { enabled ->
            RoutePreviewMap(
                modifier = Modifier.fillMaxSize(),
                enabled = enabled,
                paths = selected?.sections.orEmpty().mapNotNull { it.polyline }.map { RoutePreviewPath(it) }
                    .toPersistentList(),
                cameraState = cameraState,
                focus = focus,
            )
        },
    )
}

/** Zoom applied when framing a single manoeuvre, close enough to read the junction. */
private const val STEP_FOCUS_ZOOM = 16.0
