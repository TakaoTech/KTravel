@file:OptIn(ExperimentalMaterial3Api::class)

package com.takaotech.ktravel.ui.plan.day.transportstep

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.day.TransportStepEvent
import com.takaotech.ktravel.presentation.plan.day.TransportStepScreen
import com.takaotech.ktravel.presentation.plan.day.TransportStepUiState
import com.takaotech.ktravel.ui.shared.component.BackButton
import com.takaotech.ktravel.ui.shared.format.toColorOrNull
import com.takaotech.ktravel.ui.shared.map.RoutePreviewMap
import com.takaotech.ktravel.ui.shared.map.RoutePreviewPath
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

/**
 * Circuit `Ui` of the transport detail (registered through Metro `@CircuitInject`).
 *
 * Shows a leg the traveller already saved: where it starts and ends and when, how long and how far
 * it runs, the notes taken on it, and the way through — turn by turn for a road route, stop by stop
 * for a journey on scheduled services.
 */
@CircuitInject(TransportStepScreen::class, AppScope::class)
@Composable
fun TransportStepUi(state: TransportStepUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    val transport = state.transport
    if (transport == null) {
        TransportStepLoading(
            modifier = modifier,
            onBack = { sink(TransportStepEvent.NavigateBack) },
        )
        return
    }

    val cameraState = rememberCameraState()
    val scope = rememberCoroutineScope()
    var focus by remember { mutableStateOf<PolylineEncoderDecoder.LatLngZ?>(null) }
    LaunchedEffect(transport.answer) { focus = null }

    val paths = remember(transport.answer) {
        transport.answer.paths
            .map { RoutePreviewPath(polyline = it.polyline, color = it.colorHex?.toColorOrNull()) }
            .toPersistentList()
    }

    TransportStepContent(
        transport = transport,
        notes = state.notes,
        canRecalculate = state.canRecalculate,
        modifier = modifier,
        onBack = { sink(TransportStepEvent.NavigateBack) },
        onRecalculate = { sink(TransportStepEvent.Recalculate) },
        onNotesEvent = { sink(TransportStepEvent.Notes(it)) },
        onPointClick = { point ->
            focus = point
            scope.launch {
                cameraState.animateTo(
                    CameraPosition(
                        target = Position(longitude = point.lng, latitude = point.lat),
                        zoom = POINT_FOCUS_ZOOM,
                    ),
                )
            }
        },
        map = { mapModifier ->
            RoutePreviewMap(
                modifier = mapModifier.testTag(TransportStepTestTags.MAP),
                enabled = true,
                paths = paths,
                cameraState = cameraState,
                focus = focus,
            )
        },
    )
}

@Composable
internal fun TransportStepLoading(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(TransportStepTestTags.BACK_BUTTON),
                    )
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
