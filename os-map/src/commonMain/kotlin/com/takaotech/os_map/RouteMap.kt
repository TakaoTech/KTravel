package com.takaotech.os_map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

internal const val DEFAULT_STYLE_URI = "https://tiles.openfreemap.org/styles/liberty"

/** Coordinata geografica per posizionare un marker singolo sulla mappa. */
data class LatLng(val lat: Double, val lng: Double)

@Composable
expect fun RouteMap(
    modifier: Modifier = Modifier,
    enable: Boolean = false,
    styleUri: String = DEFAULT_STYLE_URI,
    geoJsonPath: String? = null,
    marker: LatLng? = null,
    cameraState: CameraState = rememberCameraState(),
)

@Composable
internal fun MobileRouteMapContent(
    modifier: Modifier = Modifier,
    enable: Boolean,
    styleUri: String = DEFAULT_STYLE_URI,
    geoJsonPath: String? = null,
    marker: LatLng? = null,
    cameraState: CameraState = rememberCameraState(),
) {
    LaunchedEffect(geoJsonPath) {
        if (geoJsonPath != null) {
            computeBoundingBox(geoJsonPath)?.let { bbox ->
                cameraState.animateTo(bbox, padding = PaddingValues(48.dp))
            }
        }
    }

    LaunchedEffect(marker) {
        if (marker != null) {
            cameraState.position = CameraPosition(
                target = Position(longitude = marker.lng, latitude = marker.lat),
                zoom = 14.0,
            )
        }
    }

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri(styleUri),
        cameraState = cameraState,
    ) {
        if (geoJsonPath != null) {
            val pathLine = rememberGeoJsonSource(
                data = GeoJsonData.JsonString(geoJsonPath),
            )
            LineLayer("path", source = pathLine)
        }
        if (marker != null) {
            val markerSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(
                    Point(Position(longitude = marker.lng, latitude = marker.lat)),
                ),
            )
            CircleLayer(
                id = "marker",
                source = markerSource,
                radius = const(8.dp),
                color = const(Color.Red),
                strokeColor = const(Color.White),
                strokeWidth = const(2.dp),
            )
        }
    }
}

private fun computeBoundingBox(geoJson: String): BoundingBox? = runCatching {
    val root = Json.parseToJsonElement(geoJson).jsonObject
    val coords: JsonArray = when (root["type"]?.jsonPrimitive?.contentOrNull) {
        "Feature" -> root["geometry"]!!.jsonObject["coordinates"]!!.jsonArray
        "LineString" -> root["coordinates"]!!.jsonArray
        else -> return null
    }
    var minLng = Double.MAX_VALUE
    var maxLng = -Double.MAX_VALUE
    var minLat = Double.MAX_VALUE
    var maxLat = -Double.MAX_VALUE
    coords.forEach { elem ->
        val c = elem.jsonArray
        val lng = c[0].jsonPrimitive.double
        val lat = c[1].jsonPrimitive.double
        if (lng < minLng) minLng = lng
        if (lng > maxLng) maxLng = lng
        if (lat < minLat) minLat = lat
        if (lat > maxLat) maxLat = lat
    }
    BoundingBox(west = minLng, south = minLat, east = maxLng, north = maxLat)
}.getOrNull()
