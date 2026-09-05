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
import com.takaotech.ktravel.presentation.plan.StepUi
import com.takaotech.ktravel.ui.plan.day.toIcon
import org.jetbrains.compose.resources.painterResource

/**
 * Node of a transport: the vehicle icon over a `surface` background that punches through the line.
 */
@Composable
internal fun TransportNode(step: StepUi.Transport) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(step.type.toIcon()),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
