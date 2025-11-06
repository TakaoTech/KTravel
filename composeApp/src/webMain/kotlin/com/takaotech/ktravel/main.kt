package com.takaotech.ktravel

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.takaotech.ktravel.core.startKTravelKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKTravelKoin()
    ComposeViewport {
        App()
    }
}