package com.takaotech.ktravel

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import ktravel.os_map.generated.resources.Res
import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.util.LatLongUtils
import org.mapsforge.map.awt.graphics.AwtGraphicFactory
import org.mapsforge.map.awt.util.AwtUtil
import org.mapsforge.map.awt.view.MapView
import org.mapsforge.map.layer.cache.TileCache
import org.mapsforge.map.layer.download.TileDownloadLayer
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik
import java.io.File
import kotlin.io.path.Path


@Composable
fun MapForge(
    modifier: Modifier = Modifier,
    showFps: Boolean = false
) {
    var uri = File(Res.getUri("files/world.map"))

    SwingPanel(
        modifier = modifier,
        factory = {
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

            // Crea e avvia il layer di download
            val downloadLayer = TileDownloadLayer(
                tileCache,
                mapView.model.mapViewPosition,
                tileSource,
                AwtGraphicFactory.INSTANCE // usa HttpClient di default; in alcune versioni puoi passare un factory custom
            )

            mapView.layerManager.layers.add(downloadLayer)
            downloadLayer.start()

            mapView.model.mapViewPosition.setCenter(LatLong(45.182521, 9.138684))
            mapView.model.mapViewPosition.zoomLevel = 4

            mapView
        },
        update = { /* lascia vuoto per non ricreare i layer */ }
    )
}