package com.takaotech.ktravel.ui.shared.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.takaotech.gunzou.api.geometry.GeoJsonConverter
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/** What a stretch is drawn in when neither the operator nor the caller names a colour. */
private const val DEFAULT_PATH_COLOUR = 0xFF3B6EF5

/** Inset kept between the drawn path's bounding box and the map edges when framing it. */
private val ROUTE_FIT_PADDING = 48.dp

/**
 * One stretch of the answer as the map draws it.
 *
 * A colour per stretch and not one line for the whole thing, because a journey is read by its
 * colours: the operator publishes them, the traveller matches them against the signage, and a
 * timeline that colours its steps next to a map that does not is two drawings of one journey.
 *
 * @property polyline HERE flexible encoding, which is the only one the decoder reads.
 * @property color Null falls back to the theme, which is what a road route wants.
 */
data class RoutePreviewPath(val polyline: String, val color: Color? = null)

/**
 * The answer drawn, framed on everything it covers.
 *
 * [enabled] turns the gestures off rather than the map: the caller passes `false` while a sheet is
 * expanded over it, and a map that still panned underneath would swallow the drag.
 *
 * [cameraState] is hoisted because the list beside the map drives the camera when a step or a stop
 * is clicked; [focus] is the coordinate that click resolved to, marked on the map.
 */
@Composable
fun RoutePreviewMap(
    enabled: Boolean,
    paths: ImmutableList<RoutePreviewPath>,
    cameraState: CameraState,
    focus: PolylineEncoderDecoder.LatLngZ?,
    modifier: Modifier = Modifier,
    fallbackColor: Color = Color(DEFAULT_PATH_COLOUR),
) {
    val geoJson by remember(paths) {
        derivedStateOf { paths.map { GeoJsonConverter.mergePolylinesToGeoJson(listOf(it.polyline)) } }
    }

    LaunchedEffect(geoJson) {
        pathsBoundingBox(geoJson)?.let {
            cameraState.animateTo(
                it,
                padding = PaddingValues(ROUTE_FIT_PADDING),
            )
        }
    }

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri(MAP_STYLE_URI),
        cameraState = cameraState,
        options = MapOptions(gestureOptions = if (enabled) GestureOptions.Standard else GestureOptions.AllDisabled),
    ) {
        geoJson.forEachIndexed { index, shape ->
            val source = rememberGeoJsonSource(data = GeoJsonData.JsonString(shape))
            LineLayer(
                id = "path-$index",
                source = source,
                color = const(paths[index].color ?: fallbackColor),
                width = const(4.dp),
            )
        }

        focus?.let { point ->
            val markerSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(
                    Point(
                        Position(
                            longitude = point.lng,
                            latitude = point.lat,
                        ),
                    ),
                ),
            )
            CircleLayer(
                id = "focus-marker",
                source = markerSource,
                radius = const(8.dp),
                color = const(Color.Red),
                strokeColor = const(Color.White),
                strokeWidth = const(2.dp),
            )
        }
    }
}

/**
 * The box covering every drawn stretch, or `null` when none of them can be read.
 *
 * Only the shape [GeoJsonConverter.mergePolylinesToGeoJson] produces is handled — a `Feature` whose
 * geometry is a `LineString` — because that is the only producer feeding these screens.
 */
private fun pathsBoundingBox(shapes: List<String>): BoundingBox? {
    val positions = shapes.flatMap { readLineString(it) }
    if (positions.isEmpty()) return null

    val longitudes = positions.map { it.first }
    val latitudes = positions.map { it.second }

    return BoundingBox(
        west = longitudes.min(),
        south = latitudes.min(),
        east = longitudes.max(),
        north = latitudes.max(),
    )
}

/** GeoJSON positions are `[longitude, latitude]` per RFC 7946. */
private fun readLineString(geoJson: String): List<Pair<Double, Double>> = runCatching {
    val root = Json.parseToJsonElement(geoJson).jsonObject
    if (root["type"]?.jsonPrimitive?.contentOrNull != "Feature") return@runCatching emptyList()
    val coordinates: JsonArray = root["geometry"]?.jsonObject?.get("coordinates")?.jsonArray
        ?: return@runCatching emptyList()

    coordinates.map { it.jsonArray }
        .map { it[0].jsonPrimitive.double to it[1].jsonPrimitive.double }
}.getOrDefault(emptyList())
