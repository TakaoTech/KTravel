package com.takaotech.os_map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.maplibre.compose.camera.CameraState

@Composable
actual fun RouteMap(
    modifier: Modifier,
    enable: Boolean,
    styleUri: String,
    geoJsonPath: String?,
    marker: LatLng?,
    cameraState: CameraState,
) {
    // TODO: rendere il marker anche su Desktop (Mapsforge Marker/LatLong). Per ora ignorato.
    MapForge(
        modifier = modifier,
        enable = enable,
        showFps = false,
        geoJsonPath = geoJsonPath,
        cameraState = cameraState,
    )
}
