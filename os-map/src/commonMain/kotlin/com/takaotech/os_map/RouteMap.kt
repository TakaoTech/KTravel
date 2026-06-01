package com.takaotech.os_map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle

internal const val DEFAULT_STYLE_URI = "https://tiles.openfreemap.org/styles/liberty"

@Composable
expect fun RouteMap(
    modifier: Modifier = Modifier,
    styleUri: String = DEFAULT_STYLE_URI,
    geoJsonPath: String? = null
)

@Composable
internal fun MobileRouteMapContent(
    modifier: Modifier = Modifier,
    styleUri: String = DEFAULT_STYLE_URI,
    geoJsonPath: String? = null
) {
    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri(styleUri)
    ) {
        if (geoJsonPath != null) {
            val pathLine = rememberGeoJsonSource(
                data = GeoJsonData.JsonString(geoJsonPath)
            )
            LineLayer("path", source = pathLine)
        }
    }
}
