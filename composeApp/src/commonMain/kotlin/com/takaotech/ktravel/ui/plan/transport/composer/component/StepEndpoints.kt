package com.takaotech.ktravel.ui.plan.transport.composer.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.takaotech.ktravel.presentation.plan.StepUi
import com.takaotech.ktravel.ui.plan.transport.component.PlaceEndpoint
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.flag
import ktravel.composeapp.generated.resources.place
import ktravel.composeapp.generated.resources.planning_transport_arrival
import ktravel.composeapp.generated.resources.planning_transport_departure
import org.jetbrains.compose.resources.stringResource

/**
 * The two places, with the paper aeroplane still flying between them.
 *
 * The animation is the one thing on this screen that predates the redesign and stays: it is what
 * the leg reads as, and a static connector would be a plainer screen for no reason.
 */
@Composable
internal fun StepEndpoints(start: StepUi.Place?, end: StepUi.Place?, modifier: Modifier = Modifier) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()

    if (windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        Row(
            modifier = modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaceEndpoint(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.planning_transport_departure),
                name = start?.name.orEmpty(),
                icon = Res.drawable.place,
            )

            val composition by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/lottie_paper_airplane.json").decodeToString(),
                )
            }

            val width =
                if (windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
                        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
                    )
                ) {
                    Modifier.width(128.dp)
                } else {
                    Modifier.width(56.dp)
                }

            Image(
                modifier = Modifier.padding(horizontal = 8.dp) then width,
                painter = rememberLottiePainter(
                    composition = composition,
                    iterations = Compottie.IterateForever,
                ),
                contentDescription = null,
            )

            PlaceEndpoint(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.planning_transport_arrival),
                name = end?.name.orEmpty(),
                icon = Res.drawable.flag,
            )
        }
    } else {
        Column(modifier = modifier) {
            PlaceEndpoint(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.planning_transport_departure),
                name = start?.name.orEmpty(),
                icon = Res.drawable.place,
            )

            // TODO Add icon for indicate destination
            Spacer(modifier = Modifier.height(16.dp))

            PlaceEndpoint(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.planning_transport_arrival),
                name = end?.name.orEmpty(),
                icon = Res.drawable.flag,
            )
        }
    }
}

//region Previews

/**
 * The pair under and over the medium width breakpoint, which is what picks the layout.
 *
 * [currentWindowAdaptiveInfoV2] reads the window rather than the parent, so the width has to come
 * from the preview itself: 360dp stacks the two places, 840dp puts them side by side with the
 * aeroplane between them. The aeroplane only flies in an interactive preview — a static one catches
 * a frame of it, or nothing at all while its JSON is still being read.
 */
@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 840)
@Composable
private fun StepEndpointsPreview(
    @PreviewParameter(StepEndpointsPreviewParams::class) state: StepEndpointsPreviewState,
) = KTravelTheme {
    Surface {
        StepEndpoints(
            start = state.start,
            end = state.end,
            modifier = Modifier.padding(16.dp),
        )
    }
}

/** The two ends of the leg being composed, either of which may not have been chosen yet. */
internal data class StepEndpointsPreviewState(val start: StepUi.Place?, val end: StepUi.Place?)

/**
 * Both places chosen, only the first, and two names long enough to fight over the row.
 *
 * A leg is composed one end at a time, so the half filled pair is not an error state: the second
 * label has to hold its place with nothing under it. The long names are what the `weight(1f)` split
 * is there for, once the aeroplane has taken its 128dp out of the middle.
 */
internal class StepEndpointsPreviewParams : PreviewParameterProvider<StepEndpointsPreviewState> {
    override val values = sequenceOf(
        StepEndpointsPreviewState(
            start = previewPlace(name = "Kyoto Station"),
            end = previewPlace(name = "Fushimi Inari Taisha"),
        ),
        StepEndpointsPreviewState(start = previewPlace(name = "Kyoto Station"), end = null),
        StepEndpointsPreviewState(
            start = previewPlace(name = "Kyoto Station Karasuma central exit"),
            end = previewPlace(name = "Fushimi Inari Taisha, Senbon Torii entrance"),
        ),
    )
}

/** A place the preview only reads the name off, which is all these two endpoints show. */
private fun previewPlace(name: String): StepUi.Place = StepUi.Place(name = name, lat = 0.0, lng = 0.0)
//endregion Previews
