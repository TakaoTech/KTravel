package com.takaotech.ktravel.ui.plan.day.stepspane.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.photo_camera
import org.jetbrains.compose.resources.painterResource

/** Placeholder of the place picture: tonal square with a camera glyph. */
@Composable
internal fun PlaceThumbnail(modifier: Modifier = Modifier) {
    // TODO Show the place photo once places carry one.
    Box(
        modifier = modifier
            .size(ThumbnailSize)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            painter = painterResource(Res.drawable.photo_camera),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
