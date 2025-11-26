package com.takaotech.ktravel.core

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.kdroidfilter.platformtools.getOperatingSystem

/**
 * CompositionLocal che fornisce informazioni sul sistema operativo corrente.
 * Utilizza getOperatingSystem() dalla libreria Platform-Tools.
 */
val LocalOperatingSystem = staticCompositionLocalOf {
    getOperatingSystem()
}