package com.takaotech.ktravel.ui.plan.transport.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun LocalContentColorOf(enabled: Boolean): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else DISABLED_ALPHA)

/** Material's own disabled opacity, used for anything the screen dims rather than hides. */
internal const val DISABLED_ALPHA = 0.38f

/** The mode strip is a hint, not a control, so it sits below the text it belongs to. */
internal const val MODE_ICON_ALPHA = 0.85f
