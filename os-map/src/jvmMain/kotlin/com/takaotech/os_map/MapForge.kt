package com.takaotech.os_map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.awt.graphics.AwtGraphicFactory
import org.mapsforge.map.awt.util.AwtUtil
import org.mapsforge.map.awt.view.MapView
import org.mapsforge.map.layer.cache.TileCache
import org.mapsforge.map.layer.download.TileDownloadLayer
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik
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
actual fun MapForge(
    modifier: Modifier,
    showFps: Boolean
) {
//    var uri = File(Res.getUri("files/world.map"))

    SwingPanel(
        modifier = modifier,
        factory = {
            InteropPanel().also { panel ->
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

                panel.setLayout(BorderLayout())
                panel.add(mapView, BorderLayout.CENTER)
                panel.add(mapView)
                panel.subscribeToMouseEvents(mapView)
            }


        }
    )
}


private class InteropPanel : JPanel(), MouseListener, MouseWheelListener, MouseMotionListener {
    override fun mouseClicked(e: MouseEvent) = dispatchToCompose(e)
    override fun mousePressed(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseReleased(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseEntered(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseExited(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseDragged(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseMoved(e: MouseEvent) = dispatchToCompose(e)
    override fun mouseWheelMoved(e: MouseWheelEvent) = dispatchToCompose(e)

    fun subscribeToMouseEvents(component: Component) {
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
        when (e.id) {
            MouseEvent.MOUSE_ENTERED, MouseEvent.MOUSE_EXITED -> return
        }

        // WARNING: it depends on implementation details that might be changed in the future without notice
        parent.dispatchEvent(e)
    }
}