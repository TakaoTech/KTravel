package com.takaotech.ktravel

import androidx.compose.ui.window.ComposeUIViewController
import com.takaotech.ktravel.di.startKTravelKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        startKTravelKoin()
    },
) { App() }