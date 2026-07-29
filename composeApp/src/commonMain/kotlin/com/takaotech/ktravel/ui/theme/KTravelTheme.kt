package com.takaotech.ktravel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * KTravel Design System theme wrapper.
 *
 * Applies the brand color schemes, the Material 3 type scale and the corner radius scale.
 * Wrap the whole app (and every preview) in it so standard Material 3 components inherit
 * the KTravel brand automatically.
 */
@Composable
fun KTravelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) KTravelDarkColors else KTravelLightColors,
        typography = KTravelTypography,
        shapes = KTravelShapes,
        content = content,
    )
}
