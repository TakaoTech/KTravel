package com.takaotech.os_map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position
import org.mapsforge.core.graphics.Cap
import org.mapsforge.core.graphics.Style
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.awt.graphics.AwtGraphicFactory
import org.mapsforge.map.awt.util.AwtUtil
import org.mapsforge.map.awt.view.MapView
import org.mapsforge.map.layer.cache.TileCache
import org.mapsforge.map.layer.download.TileDownloadLayer
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik
import org.mapsforge.map.layer.overlay.Polyline
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import javax.swing.JPanel
import kotlin.io.path.Path
import kotlin.math.roundToInt


@Composable
fun MapForge(
    modifier: Modifier,
    enable: Boolean,
    showFps: Boolean,
    geoJsonPath: String? = null,
    cameraState: CameraState = rememberCameraState(),
) {
    val panelState = remember { mutableStateOf<MapForgePanel?>(null) }

    LaunchedEffect(panelState.value, cameraState) {
        val panel = panelState.value ?: return@LaunchedEffect
        snapshotFlow { cameraState.position }
            .collect { pos -> panel.applyExternalPosition(pos) }
    }

    SwingPanel(
        modifier = modifier,
        factory = {
            MapForgePanel(cameraState).also { panel ->
                val mapView = MapView()
                mapView.fpsCounter.isVisible = showFps
                mapView.mapScaleBar.isVisible = true
                mapView.model.displayModel.setFixedTileSize(256)

                val tileCache: TileCache = AwtUtil.createTileCache(
                    mapView.model.displayModel.tileSize,
                    mapView.model.frameBufferModel.overdrawFactor,
                    1,
                    Path(".").toFile()
                )

                val tileSource = OpenStreetMapMapnik.INSTANCE.apply {
                    setUserAgent("mapsforge-compose-desktop-sample")
                }

                val downloadLayer = TileDownloadLayer(
                    tileCache,
                    mapView.model.mapViewPosition,
                    tileSource,
                    AwtGraphicFactory.INSTANCE
                )

                mapView.layerManager.layers.add(downloadLayer)
                downloadLayer.start()

                mapView.model.mapViewPosition.zoomLevel = 4

                panel.setup(mapView)
                panel.updateGeoJson(geoJsonPath)
                panelState.value = panel
            }
        },
        update = { panel ->
            panel.isEnabled = enable
            panel.cameraState = cameraState
            panel.updateGeoJson(geoJsonPath)
        }
    )
}

private data class LatLngBounds(
    val centerLat: Double,
    val centerLng: Double,
    val spanLat: Double,
    val spanLng: Double
)

private fun computeLatLngBounds(geoJson: String): LatLngBounds? = runCatching {
    val root = Json.parseToJsonElement(geoJson).jsonObject

    fun extractCoords(obj: JsonObject): List<Pair<Double, Double>> {
        val result = mutableListOf<Pair<Double, Double>>()
        fun addCoords(coords: JsonArray) {
            coords.forEach { elem ->
                val c = elem.jsonArray
                result.add(c[0].jsonPrimitive.double to c[1].jsonPrimitive.double)
            }
        }

        fun processGeometry(geometry: JsonObject) {
            when (geometry["type"]?.jsonPrimitive?.contentOrNull) {
                "LineString" -> geometry["coordinates"]?.jsonArray?.let { addCoords(it) }
                "MultiLineString" -> geometry["coordinates"]?.jsonArray?.forEach { line ->
                    addCoords(line.jsonArray)
                }
            }
        }
        when (obj["type"]?.jsonPrimitive?.contentOrNull) {
            "FeatureCollection" -> obj["features"]?.jsonArray?.forEach { f ->
                f.jsonObject["geometry"]?.jsonObject?.let { processGeometry(it) }
            }

            "Feature" -> obj["geometry"]?.jsonObject?.let { processGeometry(it) }
            else -> processGeometry(obj)
        }
        return result
    }

    val coords = extractCoords(root)
    if (coords.isEmpty()) return null
    var minLng = Double.MAX_VALUE;
    var maxLng = -Double.MAX_VALUE
    var minLat = Double.MAX_VALUE;
    var maxLat = -Double.MAX_VALUE
    coords.forEach { (lng, lat) ->
        if (lng < minLng) minLng = lng; if (lng > maxLng) maxLng = lng
        if (lat < minLat) minLat = lat; if (lat > maxLat) maxLat = lat
    }
    LatLngBounds(
        centerLat = (minLat + maxLat) / 2,
        centerLng = (minLng + maxLng) / 2,
        spanLat = maxLat - minLat,
        spanLng = maxLng - minLng
    )
}.getOrNull()

private fun spanToZoom(spanDeg: Double): Byte {
    var zoom = 0
    var span = 360.0
    while (span / 2 > spanDeg && zoom < 18) {
        span /= 2
        zoom++
    }
    return zoom.coerceIn(2, 18).toByte()
}

private fun toCameraPosition(center: LatLong, zoom: Byte) =
    CameraPosition(target = Position(center.longitude, center.latitude), zoom = zoom.toDouble())

private fun CameraPosition.toLatLong() = LatLong(target.latitude, target.longitude)

private fun CameraPosition.toZoomByte() = zoom.roundToInt().coerceIn(0, 18).toByte()

private fun buildPolylines(geoJson: String): List<Polyline> {
    val result = mutableListOf<Polyline>()

    fun makePaint() = AwtGraphicFactory.INSTANCE.createPaint().also { paint ->
        paint.color = (0xFF shl 24) or (0x21 shl 16) or (0x96 shl 8) or 0xF3
        paint.strokeWidth = 8f
        paint.setStyle(Style.STROKE)
        paint.setStrokeCap(Cap.ROUND)
    }

    fun coordsToLatLongs(coords: JsonArray): List<LatLong> =
        coords.map { elem ->
            val c = elem.jsonArray
            LatLong(c[1].jsonPrimitive.double, c[0].jsonPrimitive.double)
        }

    fun addPolyline(coords: JsonArray) {
        val polyline = Polyline(makePaint(), AwtGraphicFactory.INSTANCE)
        polyline.latLongs.addAll(coordsToLatLongs(coords))
        result.add(polyline)
    }

    fun processGeometry(geometry: JsonObject) {
        when (geometry["type"]?.jsonPrimitive?.contentOrNull) {
            "LineString" -> geometry["coordinates"]?.jsonArray?.let { addPolyline(it) }
            "MultiLineString" -> geometry["coordinates"]?.jsonArray?.forEach { line ->
                addPolyline(line.jsonArray)
            }
        }
    }

    fun processElement(obj: JsonObject) {
        when (obj["type"]?.jsonPrimitive?.contentOrNull) {
            "FeatureCollection" -> obj["features"]?.jsonArray?.forEach { feature ->
                processElement(feature.jsonObject)
            }

            "Feature" -> obj["geometry"]?.jsonObject?.let { processGeometry(it) }
            else -> processGeometry(obj)
        }
    }

    runCatching {
        processElement(Json.parseToJsonElement(geoJson).jsonObject)
    }
    return result
}

private class MapForgePanel(
    var cameraState: CameraState
) : JPanel(), MouseListener, MouseWheelListener, MouseMotionListener {
    private lateinit var mapView: MapView
    private val geoJsonLayers = mutableListOf<Polyline>()
    private var suppressObserver = false

    override fun isEnabled(): Boolean {
        return mapView.isEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        mapView.isEnabled = enabled
        super.setEnabled(enabled)
    }

    fun setup(mv: MapView) {
        mapView = mv
        layout = BorderLayout()
        add(mapView, BorderLayout.CENTER)
        subscribeToMouseEvents(mapView)

        mapView.model.mapViewPosition.addObserver {
            if (!suppressObserver) {
                val pos = toCameraPosition(
                    mapView.model.mapViewPosition.center,
                    mapView.model.mapViewPosition.zoomLevel
                )
                cameraState.position = pos
            }
        }
    }

    fun applyExternalPosition(pos: CameraPosition) {
        if (!::mapView.isInitialized) return
        val targetCenter = pos.toLatLong()
        val targetZoom = pos.toZoomByte()
        if (mapView.model.mapViewPosition.center == targetCenter &&
            mapView.model.mapViewPosition.zoomLevel == targetZoom
        ) return
        suppressObserver = true
        mapView.model.mapViewPosition.center = targetCenter
        mapView.model.mapViewPosition.zoomLevel = targetZoom
        suppressObserver = false
    }

    fun updateGeoJson(geoJson: String?) {
        if (!::mapView.isInitialized) return
        geoJsonLayers.forEach { mapView.layerManager.layers.remove(it) }
        geoJsonLayers.clear()
        if (geoJson != null) {
            val polylines = buildPolylines(geoJson)
            geoJsonLayers.addAll(polylines)
            polylines.forEach { mapView.layerManager.layers.add(it) }
            computeLatLngBounds(geoJson)?.let { bounds ->
                mapView.model.mapViewPosition.center = LatLong(bounds.centerLat, bounds.centerLng)
                val zoom = spanToZoom(maxOf(bounds.spanLat, bounds.spanLng))
                mapView.model.mapViewPosition.zoomLevel = zoom
            }
        }
    }

    override fun mouseClicked(e: MouseEvent) = dispatchToCompose(e)
    override fun mousePressed(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseReleased(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseEntered(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseExited(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseDragged(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseMoved(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseWheelMoved(e: MouseWheelEvent) = dispatchToCompose(e)

    private fun subscribeToMouseEvents(component: Component) {
        component.addMouseListener(this)
        component.addMouseMotionListener(this)
        component.addMouseWheelListener(this)
    }

    fun unsubscribeFromMouseEvents(component: Component) {
        component.removeMouseListener(this)
        component.removeMouseMotionListener(this)
        component.removeMouseWheelListener(this)
    }


    private fun dispatchToCompose(e: MouseEvent) {
        if (e.id == MouseEvent.MOUSE_ENTERED || e.id == MouseEvent.MOUSE_EXITED) return
        parent?.dispatchEvent(e)
    }
}
