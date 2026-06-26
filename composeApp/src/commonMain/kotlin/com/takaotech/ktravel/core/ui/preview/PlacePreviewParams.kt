package com.takaotech.ktravel.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.presentation.planning.PlaceUi

open class PlacePreviewParams(val items: Int) : PreviewParameterProvider<PlaceUi> {

    override val values: Sequence<PlaceUi>
        get() = (1..items).map {
            generatePlace(it)
        }.asSequence()

    private fun generatePlace(index: Int): PlaceUi {
        return PlaceUi(
            name = "Place $index",
            lat = 45.0 + index * 0.1,
            lng = 9.0 + index * 0.1,
        )
    }
}