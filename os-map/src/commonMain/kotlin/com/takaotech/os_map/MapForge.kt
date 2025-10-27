package com.takaotech.os_map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MapForge(
    modifier: Modifier = Modifier,
    showFps: Boolean = false
)