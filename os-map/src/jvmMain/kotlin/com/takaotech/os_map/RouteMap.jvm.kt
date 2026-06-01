package com.takaotech.os_map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun RouteMap(modifier: Modifier, styleUri: String, geoJsonPath: String?) {
    MapForge(
        modifier = modifier,
        showFps = false,
        geoJsonPath = geoJsonPath,
    )
}
