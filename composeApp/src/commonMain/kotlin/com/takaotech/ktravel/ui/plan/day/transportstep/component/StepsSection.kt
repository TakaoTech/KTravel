package com.takaotech.ktravel.ui.plan.day.transportstep.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.ui.plan.day.transportstep.TransportStepTestTags
import com.takaotech.ktravel.ui.shared.route.RoutingStepSection
import com.takaotech.ktravel.ui.shared.route.transit.TransitStepRow
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.nacular.measured.units.times

/**
 * The way through, collapsible, in whichever of the two forms the answer was given.
 *
 * A road answer is a list of manoeuvres, which can run to hundreds of rows and is therefore
 * contributed to the caller's list one section at a time. A journey has no manoeuvres at all: it is
 * drawn with the same rows the preview screen uses, a handful of them, so it fits in a single item.
 */
internal fun LazyListScope.stepsSection(
    answer: TransportAnswer,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onPointClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
) {
    item(contentType = "transportHeader") {
        val stepCount = remember(answer) {
            when (answer) {
                is TransportAnswer.Routing -> answer.route.sections.sumOf { it.actions.size }
                is TransportAnswer.Transit -> answer.journey.steps.size
            }
        }
        StepsHeader(stepCount = stepCount, expanded = expanded, onToggle = onToggleExpanded)
    }

    if (expanded) {
        // TODO Change with AnimatedVisibility
        // TODO Review how to show this component
        when (answer) {
            is TransportAnswer.Routing -> items(answer.route.sections) { section ->
                RoutingStepSection(
                    modifier = Modifier.testTag(TransportStepTestTags.MANOEUVRES),
                    actions = section.actions,
                    polyline = section.polyline,
                    onActionClick = onPointClick,
                )
            }

            is TransportAnswer.Transit -> item {
                Column(modifier = Modifier.testTag(TransportStepTestTags.TRANSIT_TIMELINE)) {
                    answer.journey.steps.forEach { step ->
                        TransitStepRow(step = step, onStopClick = onPointClick)
                    }
                }
            }
        }
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun StepsSectionPreview(@PreviewParameter(StepsSectionPreviewParams::class) state: StepsSectionPreviewState) =
    KTravelTheme {
        Surface {
            LazyColumn(
                modifier = Modifier
                    .width(360.dp)
                    .heightIn(max = 420.dp),
            ) {
                stepsSection(
                    answer = state.answer,
                    expanded = state.expanded,
                    onToggleExpanded = {},
                    onPointClick = {},
                )
            }
        }
    }

/** An answer to lay out, and whether the rows under the header are shown. */
internal data class StepsSectionPreviewState(val answer: TransportAnswer, val expanded: Boolean)

/**
 * A road answer open and closed, and a journey open.
 *
 * The two answers do not draw the same rows — manoeuvres against a timeline — and they do not even
 * reach the list the same way, one item per section against a single item for the whole journey.
 * Closed is the third state because it is the one the header is folded onto.
 */
internal class StepsSectionPreviewParams : PreviewParameterProvider<StepsSectionPreviewState> {
    override val values = sequenceOf(
        StepsSectionPreviewState(answer = previewRoadStep().answer, expanded = true),
        StepsSectionPreviewState(answer = previewRoadStep().answer, expanded = false),
        StepsSectionPreviewState(answer = previewTransitStep().answer, expanded = true),
    )
}
//endregion Previews
