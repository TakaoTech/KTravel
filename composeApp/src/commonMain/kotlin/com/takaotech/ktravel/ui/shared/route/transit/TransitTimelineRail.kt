package com.takaotech.ktravel.ui.shared.route.transit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The vertical stroke that ties the rows into one itinerary. */
@Composable
internal fun TransitTimelineRail(color: Color, width: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(width)
            .size(width, 28.dp)
            .background(color, RoundedCornerShape(width / 2)),
    )
}
