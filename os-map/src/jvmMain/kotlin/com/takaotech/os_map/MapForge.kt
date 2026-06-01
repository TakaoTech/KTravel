package com.takaotech.os_map

import androidx.compose.runtime.Composable
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


@Composable
fun MapForge(
    modifier: Modifier,
    showFps: Boolean,
    geoJsonPath: String? = null
) {
    SwingPanel(
        modifier = modifier,
        factory = {
            MapForgePanel().also { panel ->
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

//                mapView.model.mapViewPosition.setCenter(LatLong(45.182521, 9.138684))
                mapView.model.mapViewPosition.zoomLevel = 4

                panel.setup(mapView)
                panel.updateGeoJson(geoJsonPath)
            }
        },
        update = { panel ->
            panel.updateGeoJson(geoJsonPath)
        }
    )
}

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


private class MapForgePanel : JPanel(), MouseListener, MouseWheelListener, MouseMotionListener {
    private lateinit var mapView: MapView
    private val geoJsonLayers = mutableListOf<Polyline>()

    fun setup(mv: MapView) {
        mapView = mv
        layout = BorderLayout()
        add(mapView, BorderLayout.CENTER)
        subscribeToMouseEvents(mapView)
    }

    fun updateGeoJson(geoJson: String?) {
        if (!::mapView.isInitialized) return
        geoJsonLayers.forEach { mapView.layerManager.layers.remove(it) }
        geoJsonLayers.clear()
        if (geoJson != null) {
            val polylines = buildPolylines(geoJson)
            geoJsonLayers.addAll(polylines)
            polylines.forEach { mapView.layerManager.layers.add(it) }
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

    private fun dispatchToCompose(e: MouseEvent) {
        if (e.id == MouseEvent.MOUSE_ENTERED || e.id == MouseEvent.MOUSE_EXITED) return
        parent?.dispatchEvent(e)
    }
}
