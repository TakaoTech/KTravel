package com.takaotech.ktravel.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.Platform
import io.github.kdroidfilter.platformtools.getOperatingSystem
import io.github.kdroidfilter.platformtools.getPlatform

/**
 * CompositionLocal che fornisce informazioni sul sistema operativo corrente.
 * Utilizza getOperatingSystem() dalla libreria Platform-Tools.
 */
val LocalOperatingSystem = staticCompositionLocalOf {
    getOperatingSystem()
}

val LocalPlatform = staticCompositionLocalOf {
    getPlatform()
}

@Composable
fun KTravelPlatform(
    currentOs: OperatingSystem = getOperatingSystem(),
    platform: Platform = getPlatform(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalOperatingSystem provides currentOs,
        LocalPlatform provides platform,
        content = content
    )
}