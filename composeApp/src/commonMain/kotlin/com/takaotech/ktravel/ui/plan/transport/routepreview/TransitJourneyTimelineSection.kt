package com.takaotech.ktravel.ui.plan.transport.routepreview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.ui.shared.route.transit.TransitStepRow
import com.takaotech.ktravel.ui.theme.KTravelTheme

@Composable
internal fun TransitJourneyTimelineSection(
    selected: TransitJourney?,
    onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        selected?.let { journey ->
            // TODO Evaluate this is correct decision make it sticky header
            stickyHeader { TransitJourneySummaryCard(journey = journey) }
            journey.steps.forEach { step ->
                item { TransitStepRow(step = step, onStopClick = onStopClick) }
            }
        }
    }
}

//region Previews

/**
 * Two ways across Milan, the first with a change on the way.
 *
 * `@PreviewScreenSizes` is what makes this worth having: the scaffold puts the timeline beside the
 * map on a wide window and inside a bottom sheet on a narrow one, and only both widths together say
 * whether a row that carries a time, a line, an operator and a stop count still reads.
 *
 * The steps carry no geometry. A polyline in a fixture would have to be a real HERE flexible
 * encoding to decode into anything, and what is being looked at here is the timeline — so the map
 * draws its base style with nothing on it, which is also what an answer without geometry looks
 * like.
 */
@PreviewScreenSizes
@Composable
private fun TransitJourneyPreviewPagePreview() = KTravelTheme {
    Surface {
        TransitJourneyTimelineSection(
            selected = PREVIEW_WITH_CHANGE,
            onStopClick = { },
        )
    }
}

/**
 * The second alternative selected: one vehicle, no change, and a ride whose operator publishes no
 * colour — the two branches the first preview never reaches.
 */
@PreviewScreenSizes
@Composable
private fun TransitJourneyPreviewPageDirectPreview() = KTravelTheme {
    Surface {
        TransitJourneyTimelineSection(
            selected = PREVIEW_DIRECT,
            onStopClick = { },
        )
    }
}
//endregion Previews
