package com.takaotech.ktravel.ui.plan.day.stepspane.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.plan.day.stepspane.GutterWidth
import com.takaotech.ktravel.ui.plan.day.stepspane.LineThickness
import com.takaotech.ktravel.ui.plan.day.stepspane.NodeCenterY
import com.takaotech.ktravel.ui.plan.day.stepspane.NodeSize

/**
 * Timeline row: gutter with a continuous vertical line and the centered [node], and [content] on
 * the right. The line is trimmed at the node when [isFirst] / [isLast].
 */
@Composable
internal fun TimelineRow(
    isFirst: Boolean,
    isLast: Boolean,
    node: @Composable BoxScope.() -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
    ) {
        Box(modifier = Modifier.width(GutterWidth).fillMaxHeight()) {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(LineThickness)
                    .align(Alignment.TopCenter),
            ) {
                val centerY = NodeCenterY.toPx()
                val top = if (isFirst) centerY else 0f
                val bottom = if (isLast) centerY else size.height
                drawLine(
                    color = lineColor,
                    start = Offset(size.width / 2f, top),
                    end = Offset(size.width / 2f, bottom),
                    strokeWidth = size.width,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = NodeCenterY - NodeSize / 2)
                    .size(NodeSize),
                contentAlignment = Alignment.Center,
                content = node,
            )
        }

        Row(
            modifier = Modifier
//                .weight(1f)
                .padding(vertical = 8.dp)
                .padding(end = 16.dp),
            content = content,
        )
    }
}
