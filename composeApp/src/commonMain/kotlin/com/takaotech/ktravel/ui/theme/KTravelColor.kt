@file:Suppress("UndocumentedPublicProperty")

package com.takaotech.ktravel.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * KTravel Design System — color schemes.
 *
 * Generated 1:1 from the design-system color tokens (`tokens/colors.css`).
 * Prefer the [androidx.compose.material3.MaterialTheme.colorScheme] roles over the brand
 * shortcuts below.
 */

// ---- Brand shortcuts ----
val KtvBrandSky = Color(0xFF82C3FA)
val KtvBrandAzure = Color(0xFF56B7F4)
val KtvBrandDeep = Color(0xFF064973)

// ---- Light scheme (default) ----
val KTravelLightColors = lightColorScheme(
    primary = Color(0xFF064973),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE8FD),
    onPrimaryContainer = Color(0xFF001D32),
    secondary = Color(0xFF1F6493),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E9FB),
    onSecondaryContainer = Color(0xFF0B2536),
    tertiary = Color(0xFF875300),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDDBA),
    onTertiaryContainer = Color(0xFF2A1700),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFCFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFAFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41474D),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFC1C7CE),
    surfaceBright = Color(0xFFFAFCFF),
    surfaceDim = Color(0xFFD8DAE0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F4F9),
    surfaceContainer = Color(0xFFECEEF4),
    surfaceContainerHigh = Color(0xFFE6E9EE),
    surfaceContainerHighest = Color(0xFFE1E3E9),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFF9BCBFF),
    surfaceTint = Color(0xFF064973),
    scrim = Color(0xFF000000),
)

// ---- Dark scheme ----
val KTravelDarkColors = darkColorScheme(
    primary = Color(0xFF9BCBFF),
    onPrimary = Color(0xFF003353),
    primaryContainer = Color(0xFF064973),
    onPrimaryContainer = Color(0xFFCFE8FD),
    secondary = Color(0xFF9CCBF8),
    onSecondary = Color(0xFF0B2536),
    secondaryContainer = Color(0xFF1F6493),
    onSecondaryContainer = Color(0xFFD6E9FB),
    tertiary = Color(0xFFFFB870),
    onTertiary = Color(0xFF472A00),
    tertiaryContainer = Color(0xFF663E00),
    onTertiaryContainer = Color(0xFFFFDDBA),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF101418),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF41474D),
    onSurfaceVariant = Color(0xFFC1C7CE),
    outline = Color(0xFF8B9197),
    outlineVariant = Color(0xFF41474D),
    surfaceBright = Color(0xFF36393E),
    surfaceDim = Color(0xFF101418),
    surfaceContainerLowest = Color(0xFF0B0E12),
    surfaceContainerLow = Color(0xFF181C20),
    surfaceContainer = Color(0xFF1C2024),
    surfaceContainerHigh = Color(0xFF272A2F),
    surfaceContainerHighest = Color(0xFF31353A),
    inverseSurface = Color(0xFFE2E2E6),
    inverseOnSurface = Color(0xFF2F3033),
    inversePrimary = Color(0xFF064973),
    surfaceTint = Color(0xFF9BCBFF),
    scrim = Color(0xFF000000),
)
