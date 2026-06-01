package com.takaotech.os_map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.getOperatingSystem
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle

private const val DEFAULT_STYLE_URI = "https://tiles.openfreemap.org/styles/liberty"

@Composable
fun RouteMap(
    modifier: Modifier = Modifier,
    styleUri: String = DEFAULT_STYLE_URI,
    geoJsonPath: String? = null
) {
    val os = remember { getOperatingSystem() }

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri(styleUri)
    ) {
        if (geoJsonPath != null) {
            // TODO: GeoJSON layer not yet supported on desktop by maplibre-compose
            when (os) {
                OperatingSystem.ANDROID,
                OperatingSystem.IOS -> {
                    val pathLine = rememberGeoJsonSource(
                        data = GeoJsonData.JsonString(geoJsonPath)
                    )
                    LineLayer("path", source = pathLine)
                }

                else -> Unit
            }
        }
    }
}
