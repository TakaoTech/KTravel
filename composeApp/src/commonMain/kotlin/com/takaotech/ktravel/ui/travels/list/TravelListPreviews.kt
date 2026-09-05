package com.takaotech.ktravel.ui.travels.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.persistentSetOf

@PreviewScreenSizes
@Composable
private fun TravelListPagePreview() = KTravelTheme {
    TravelListContent(
        travelList = previewTravelList,
        onTravelClick = {},
        newTravelClick = {},
    )
}

@PreviewScreenSizes
@Composable
private fun TravelListPageSelectionModePreview() = KTravelTheme {
    TravelListContent(
        travelList = previewTravelList,
        isSelectionMode = true,
        selectedIds = persistentSetOf("1"),
        onTravelClick = {},
        newTravelClick = {},
    )
}
