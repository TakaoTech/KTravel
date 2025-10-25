package com.takaotech.os_map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
expect fun MapForge(
    modifier: Modifier = Modifier,
    showFps: Boolean = false
)

@Preview(showBackground = true)
@Composable
private fun MapForgePreview(){
    Box(modifier = Modifier.size(128.dp, 128.dp)) {
        MapForge(
            modifier = Modifier.fillMaxSize(),
        )
    }
}