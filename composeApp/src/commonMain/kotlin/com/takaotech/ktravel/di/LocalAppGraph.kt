package com.takaotech.ktravel.di

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppGraph = staticCompositionLocalOf<AppGraph> { error("No AppGraph provided") }
