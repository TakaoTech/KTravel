package com.takaotech.ktravel

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun PanelHorizontalDivided(
    modifier: Modifier = Modifier,
    leftBox : @Composable BoxScope.() -> Unit,
    rightBox : @Composable BoxScope.() -> Unit,
){
    val density = LocalDensity.current

    var offsetX by remember { mutableStateOf(0f) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                componentSize = layoutCoordinates.size
                if (offsetX == 0f) {
                    offsetX = componentSize.width / 2f
                }
            }
    ) {

        Row(modifier = Modifier.fillMaxSize()) {
            val leftWidth = with(density) { offsetX.toDp() }
            val rightWidth = with(density) { (componentSize.width - offsetX).toDp() }

            // LEFT BOX
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(leftWidth),
                contentAlignment = Alignment.Center,
                content = leftBox
            )

            // DRAG HANDLE
            VerticalDragHandle(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            offsetX = (offsetX + delta).coerceIn(
                                with(density) { 48.dp.toPx() },
                                componentSize.width.toFloat() - with(density) { 48.dp.toPx() },
                            )
                        },
                    )
//                        .systemGestureExclusion()
            )


            // RIGHT BOX
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(rightWidth),
                content = rightBox
            )
        }
    }
}