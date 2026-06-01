package com.takaotech.os_map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun RouteMap(modifier: Modifier, styleUri: String, geoJsonPath: String?) {
    MobileRouteMapContent(modifier = modifier, styleUri = styleUri, geoJsonPath = geoJsonPath)
}
