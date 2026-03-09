package com.takaotech.ktravel.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.presentation.planning.Place

open class PlacePreviewParams(val items: Int) : PreviewParameterProvider<Place> {

    override val values: Sequence<Place>
        get() = (1..items).map {
            generatePlace(it)
        }.asSequence()

    private fun generatePlace(index: Int): Place {
        return Place(
            name = "Place $index",
            lat = 45.0 + index * 0.1,
            lng = 9.0 + index * 0.1,
        )
    }
}