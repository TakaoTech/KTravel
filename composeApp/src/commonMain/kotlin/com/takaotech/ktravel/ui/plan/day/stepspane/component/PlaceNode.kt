package com.takaotech.ktravel.ui.plan.day.stepspane.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.place
import org.jetbrains.compose.resources.painterResource

/** Node of a place: filled circle with a "place" icon. */
@Composable
internal fun PlaceNode() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(Res.drawable.place),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
