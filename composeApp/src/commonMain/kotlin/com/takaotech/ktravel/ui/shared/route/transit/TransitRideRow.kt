package com.takaotech.ktravel.ui.shared.route.transit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.domain.routing.model.TransitStep
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.transit_preview_agency_label
import ktravel.composeapp.generated.resources.transit_preview_cd_collapse_stops
import ktravel.composeapp.generated.resources.transit_preview_cd_expand_stops
import ktravel.composeapp.generated.resources.transit_preview_direct
import ktravel.composeapp.generated.resources.transit_preview_line_website_label
import ktravel.composeapp.generated.resources.transit_preview_stops
import ktravel.composeapp.generated.resources.transit_preview_towards
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TransitRideRow(
    step: TransitStep.Ride,
    onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    modifier: Modifier = Modifier,
) {
    var stopsShown by remember(step) { mutableStateOf(false) }
    val colour = step.lineColor() ?: MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        step.boarding?.let {
            TransitStopMarker(
                stop = it,
                colour = colour,
                onStopClick = onStopClick,
                onLine = step,
            )
        }

        val expandLabel = if (stopsShown) {
            stringResource(Res.string.transit_preview_cd_collapse_stops)
        } else {
            stringResource(Res.string.transit_preview_cd_expand_stops)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),

        ) {
            TransitTimelineRail(color = colour, width = 6.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = expandLabel }
                        .clickable(enabled = step.intermediateStops.isNotEmpty()) {
                            stopsShown = !stopsShown
                        },
                ) {
                    Text(
                        text = listOfNotNull(
                            step.line.name ?: step.line.shortName,
                            step.line.headsign?.let {
                                stringResource(
                                    Res.string.transit_preview_towards,
                                    it,
                                )
                            },
                        ).joinToString(" "),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )

                    val stopsLabel = if (step.intermediateStops.isEmpty()) {
                        stringResource(Res.string.transit_preview_direct)
                    } else {
                        stringResource(
                            Res.string.transit_preview_stops,
                            step.intermediateStops.size,
                        )
                    }

//                Text(
//                    text = listOfNotNull(step.agency?.name, ).joinToString(" · "),
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                )

                    Text(
                        text = stopsLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AnimatedVisibility(visible = stopsShown) {
                    Column {
                        step.intermediateStops.forEach { stop ->
                            TransitIntermediateStopRow(
                                stop = stop,
                                onStopClick = onStopClick,
                                onLine = step,
                            )
                        }

                        if (step.agency != null) {
                            val uriHandler = LocalUriHandler.current

                            Text(
                                text = buildAnnotatedString {
                                    val website = step.agency.website
                                    append(stringResource(Res.string.transit_preview_agency_label))
                                    append(" ")
                                    if (website != null) {
                                        withLink(
                                            LinkAnnotation.Url(
                                                url = website,
                                                // TODO Add style
                                                linkInteractionListener = {
                                                    try {
                                                        uriHandler.openUri(website)
                                                    } catch (illegalEx: IllegalArgumentException) {
                                                        Logger.e(
                                                            illegalEx,
                                                        ) { "Error open transit agency link $website" }
                                                    }
                                                },
                                            ),
                                        ) {
                                            append(step.agency.name)
                                        }
                                    } else {
                                        append(step.agency.name)
                                    }

                                    val lineUrl = step.line.url

                                    if (lineUrl != null) {
                                        append(" - ")

                                        withLink(
                                            LinkAnnotation.Url(
                                                url = lineUrl,
                                                // TODO Add style
                                                linkInteractionListener = {
                                                    try {
                                                        uriHandler.openUri(lineUrl)
                                                    } catch (illegalEx: IllegalArgumentException) {
                                                        Logger.e(illegalEx) { "Error open transit line link $lineUrl" }
                                                    }
                                                },
                                            ),
                                        ) {
                                            append(stringResource(Res.string.transit_preview_line_website_label))
                                        }
                                    }
                                },
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        step.alighting?.let {
            TransitStopMarker(
                stop = it,
                colour = colour,
                onStopClick = onStopClick,
                onLine = step,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}
