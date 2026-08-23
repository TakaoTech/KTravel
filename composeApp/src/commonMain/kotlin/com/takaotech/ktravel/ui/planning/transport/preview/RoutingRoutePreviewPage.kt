package com.takaotech.ktravel.ui.planning.transport.preview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RoutingRoutes
import com.takaotech.navigator.api.geometry.PolylineEncoderDecoder
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

/**
 * A road route: the manoeuvres to perform, and the line they run along.
 *
 * The whole screen is a list of instructions, which is what a road answer is. Clicking one moves the
 * camera to where it happens, resolved from the offset the navigator put on it — the reason the
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

/** Room for "12,3 km" over "1h 3m", which is the widest a manoeuvre's figures ever get. */
private val HOW_FAR_COLUMN_WIDTH = 80.dp

/**
 * The manoeuvres of one section.
 *
 * Takes the [actions] and the [polyline] rather than a section, because the two things that carry
 * manoeuvres — the live answer and the route as the plan saved it — are different types holding the
 * same two fields, and this list has no reason to know which one it is drawing.
 *
 * A manoeuvre is only clickable when both halves of the answer are there: the geometry to look the
 * point up in, and the offset saying where in it. Without either, tapping the row would move the
 * camera somewhere arbitrary, so it does not react at all.
 */
@Composable
fun RoutingStepSection(
    actions: List<RouteAction>,
    polyline: String?,
    onActionClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        for (action in actions) {
            val offset = action.offset

            RoutingStep(
                action = action,
                onActionClick = if (polyline == null || offset == null) {
                    null
                } else {
                    {
                        runCatching { PolylineEncoderDecoder.getCoordinateAtOffset(polyline, offset) }
                            .onSuccess(onActionClick)
                    }
                },
            )
        }
    }
}

/** One instruction, with how far and how long it runs. */
@Composable
fun RoutingStep(action: RouteAction, onActionClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.then(
            if (onActionClick != null) Modifier.clickable(onClick = onActionClick) else Modifier,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.action.replaceFirstChar { it.uppercaseChar() },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (!action.instruction.isNullOrBlank()) {
                    Text(text = action.instruction, style = MaterialTheme.typography.bodyMedium)
                }
                if (!action.direction.isNullOrBlank()) {
                    Text(
                        text = action.direction,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            // Fixed width, not wrap-content: an instruction is a sentence and a distance is never
            // more than a few characters, so letting this column grow is what used to squeeze the
            // instruction beside it down to one letter per line.
            Column(
                modifier = Modifier.width(HOW_FAR_COLUMN_WIDTH),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = action.distanceMeters.formatDistance(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = action.durationSeconds.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}
