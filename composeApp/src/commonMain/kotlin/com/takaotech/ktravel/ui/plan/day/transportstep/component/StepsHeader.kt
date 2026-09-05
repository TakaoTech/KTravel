package com.takaotech.ktravel.ui.plan.day.transportstep.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.plan.day.transportstep.TransportStepTestTags
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.nacular.measured.units.times
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.keyboard_arrow_down
import ktravel.composeapp.generated.resources.transport_detail_cd_collapse_steps
import ktravel.composeapp.generated.resources.transport_detail_cd_expand_steps
import ktravel.composeapp.generated.resources.transport_detail_steps_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StepsHeader(stepCount: Int, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag(TransportStepTestTags.STEPS_TOGGLE)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = pluralStringResource(
                Res.plurals.transport_detail_steps_title,
                stepCount,
                stepCount,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val animatedRotation by animateFloatAsState(
            targetValue = if (expanded) 0f else -90f,
            label = "rotationExansionNotes",
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )

        Icon(
            modifier = Modifier
                .graphicsLayer { rotationZ = animatedRotation },
            painter = painterResource(Res.drawable.keyboard_arrow_down),
            contentDescription = stringResource(
                if (expanded) {
                    Res.string.transport_detail_cd_collapse_steps
                } else {
                    Res.string.transport_detail_cd_expand_steps
                },
            ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun StepsHeaderPreview(@PreviewParameter(StepsHeaderPreviewParams::class) state: StepsHeaderPreviewState) =
    KTravelTheme {
        Surface(modifier = Modifier.width(320.dp)) {
            StepsHeader(stepCount = state.stepCount, expanded = state.expanded, onToggle = {})
        }
    }

/** A number of steps, and whether the list under the header is open. */
internal data class StepsHeaderPreviewState(val stepCount: Int, val expanded: Boolean)

/**
 * Open and closed, with many steps and with one.
 *
 * The count goes through a plural, so a single manoeuvre is a different sentence rather than the
 * same one with a 1 in it. The chevron is the only other thing that moves, and it moves by
 * rotating: a static preview shows it at rest, at either end of that rotation.
 */
internal class StepsHeaderPreviewParams : PreviewParameterProvider<StepsHeaderPreviewState> {
    override val values = sequenceOf(
        StepsHeaderPreviewState(stepCount = 12, expanded = true),
        StepsHeaderPreviewState(stepCount = 12, expanded = false),
        StepsHeaderPreviewState(stepCount = 1, expanded = false),
    )
}
//endregion Previews
