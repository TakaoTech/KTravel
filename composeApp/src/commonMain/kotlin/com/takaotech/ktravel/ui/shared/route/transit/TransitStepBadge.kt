package com.takaotech.ktravel.ui.shared.route.transit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.model.TransitStep

/** One step of the journey reduced to what fits on a badge: the line, or how long the walk is. */
@Composable
internal fun TransitStepBadge(step: TransitStep, modifier: Modifier = Modifier) {
    val label by remember(step) {
        derivedStateOf {
            when (step) {
                is TransitStep.Walk -> AnnotatedString(step.summary.durationSeconds.toString())

                is TransitStep.Ride -> buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            step.line.shortName ?: step.line.name ?: step.line.category.orEmpty(),
                        )
                    }

                    append(" ")
                    append(step.summary.durationSeconds.toString())
                }
            }
        }
    }
    val background = step.lineColor() ?: MaterialTheme.colorScheme.surfaceVariant

    // TODO Add support for show step icon, TransitModeType is a candidate
//    if(step is TransitStep.Ride){
//        step.line.mode.iconOrNull()
//    }

    Text(
        text = label,
        modifier = modifier
            .background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelMedium,
        color = step.textColor() ?: MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

//region Preview
