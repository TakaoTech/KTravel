package com.takaotech.ktravel.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.Platform
import io.github.kdroidfilter.platformtools.getOperatingSystem
import io.github.kdroidfilter.platformtools.getPlatform

/**
 * The operating system the composition runs on, as Platform-Tools reports it.
 *
 * Static because it cannot change while the application is running: nothing that reads it has to be
 * recomposed for it.
 */
val LocalOperatingSystem = staticCompositionLocalOf {
    getOperatingSystem()
}

/**
 * The platform the composition runs on, as Platform-Tools reports it, which is a finer distinction
 * than [LocalOperatingSystem] draws.
 *
 * Static for the same reason.
 */
val LocalPlatform = staticCompositionLocalOf {
    getPlatform()
}

/**
 * Puts [LocalOperatingSystem] and [LocalPlatform] in the composition, once, around the application.
 *
 * Both are parameters rather than being read on the spot so that a preview or a test can render the
 * tree as another platform without running on one.
 *
 * @param currentOs Operating system every composable below reads, the host's by default.
 * @param platform Platform every composable below reads, the host's by default.
 * @param content What is drawn with the two in scope, which is the application itself.
 */
@Composable
fun KTravelPlatform(
    currentOs: OperatingSystem = getOperatingSystem(),
    platform: Platform = getPlatform(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalOperatingSystem provides currentOs,
        LocalPlatform provides platform,
        content = content,
    )
}
