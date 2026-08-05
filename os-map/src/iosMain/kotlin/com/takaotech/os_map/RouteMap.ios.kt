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
    MobileRouteMapContent(
        modifier = modifier,
        enable = enable,
        styleUri = styleUri,
        geoJsonPath = geoJsonPath,
        marker = marker,
        cameraState = cameraState,
    )
}
