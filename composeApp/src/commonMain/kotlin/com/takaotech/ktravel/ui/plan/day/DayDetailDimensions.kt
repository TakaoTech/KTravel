package com.takaotech.ktravel.ui.plan.day

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Backlog pane sizing, from the design: the pane hugs the right edge and the itinerary is inset by
// the pane plus a gutter, so the two never touch.
internal val ExpandedPaneWidth = 300.dp
internal val MediumPaneWidth = 284.dp
internal val CompactPaneWidth = 296.dp
internal const val COMPACT_PANE_MAX_WIDTH_FRACTION = 0.92f
internal val PaneGutter = 12.dp
internal val PaneShape = RoundedCornerShape(topStart = 28.dp)
internal val PaneElevation = 8.dp
internal const val SCRIM_ALPHA = 0.32f

// 300ms on the emphasized curve, as the design animates the pane.
internal const val PANE_ANIMATION_MILLIS = 300
internal val PaneEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
