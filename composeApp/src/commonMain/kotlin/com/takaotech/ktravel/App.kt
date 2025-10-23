package com.takaotech.ktravel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.takaotech.os_map.MapForge
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        PanelHorizontalDivided(
            modifier = Modifier.fillMaxSize(),
            leftBox = {
                Text("Left Box")
            },
            rightBox = {
                MapForge(
                    modifier = Modifier.fillMaxSize(),
                    showFps = true
                )
            }
        )
    }
}