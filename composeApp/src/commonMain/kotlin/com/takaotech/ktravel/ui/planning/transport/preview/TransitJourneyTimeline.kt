package com.takaotech.ktravel.ui.planning.transport.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.TransitAgency
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransitStop
import com.takaotech.ktravel.domain.routing.model.TransitTime
import com.takaotech.ktravel.domain.routing.model.WheelchairAccess
import com.takaotech.ktravel.ui.common.formatClock
import com.takaotech.ktravel.ui.common.toColorOrNull
import com.takaotech.ktravel.ui.theme.KTravelTheme
import com.takaotech.navigator.api.geometry.PolylineEncoderDecoder
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.transit_preview_agency_label
import ktravel.composeapp.generated.resources.transit_preview_cd_collapse_stops
import ktravel.composeapp.generated.resources.transit_preview_cd_expand_stops
import ktravel.composeapp.generated.resources.transit_preview_changes
import ktravel.composeapp.generated.resources.transit_preview_direct
import ktravel.composeapp.generated.resources.transit_preview_line_website_label
import ktravel.composeapp.generated.resources.transit_preview_no_change
import ktravel.composeapp.generated.resources.transit_preview_stops
import ktravel.composeapp.generated.resources.transit_preview_towards
import ktravel.composeapp.generated.resources.transit_preview_walk
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.minutes

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

/**
 * One step on the timeline.
 *
 * The `when` is exhaustive over the sealed step, which is the point of it being sealed: a kind of step
 * added to the model does not compile until it has been given a row.
 */
@Composable
internal fun TransitStepRow(
    step: TransitStep,
    onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (step) {
        is TransitStep.Walk -> TransitWalkRow(
            step = step,
            modifier = modifier,
        )

        is TransitStep.Ride -> TransitRideRow(
            step = step,
            onStopClick = onStopClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun TransitWalkRow(step: TransitStep.Walk, modifier: Modifier = Modifier) {
    // TODO Add support

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransitTimelineRail(color = MaterialTheme.colorScheme.outlineVariant, width = 3.dp)
        Text(
            text = stringResource(
                Res.string.transit_preview_walk,
                step.summary.durationSeconds.toString(),
            ),
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = " · ${step.summary.distance.formatDistance()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TransitRideRow(
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

/** A stop the traveller acts at: the time on the board, and the name on the sign. */
@Composable
private fun TransitStopMarker(
    stop: TransitStop,
    colour: Color,
    onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    onLine: TransitStep.Ride,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickableStop(stop, onLine, onStopClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .size(6.dp, 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.size(10.dp).background(colour, CircleShape))
        }
        Text(
            text = listOfNotNull((stop.departure ?: stop.arrival)?.clock(), stop.name).joinToString(
                "  ",
            ),
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun TransitIntermediateStopRow(
    stop: TransitStop,
    onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    onLine: TransitStep.Ride,
    modifier: Modifier = Modifier,
) {
    Text(
        text = listOfNotNull(
            (stop.departure ?: stop.arrival)?.clock(),
            stop.name,
        ).joinToString("  "),
        modifier = modifier
            .fillMaxWidth()
            .clickableStop(stop, onLine, onStopClick)
            .padding(vertical = 4.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Makes a stop move the camera, when there is enough to place it with.
 *
 * The offset on the ride's own geometry is preferred over the stop's coordinates because it is what
 * the navigator carried it for, and it lands the marker on the drawn line rather than beside it.
 */
private fun Modifier.clickableStop(
    stop: TransitStop,
    onLine: TransitStep.Ride,
    onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
): Modifier = clickable {
    val polyline = onLine.polyline
    val offset = stop.offset

    val point = if (polyline != null && offset != null) {
        runCatching { PolylineEncoderDecoder.getCoordinateAtOffset(polyline, offset) }.getOrNull()
    } else {
        null
    } ?: PolylineEncoderDecoder.LatLngZ(stop.location.lat, stop.location.lng)

    onStopClick(point)
}

/** The vertical stroke that ties the rows into one itinerary. */
@Composable
private fun TransitTimelineRail(color: Color, width: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(width)
            .size(width, 28.dp)
            .background(color, RoundedCornerShape(width / 2)),
    )
}

/** The wall clock at the stop, which is the number printed on the departure board. */
internal fun TransitTime.clock(): String = atStop().formatClock()

internal fun TransitStep.lineColor(): Color? = (this as? TransitStep.Ride)?.line?.color?.toColorOrNull()

// FIXME Check background color luminance for accessibility
internal fun TransitStep.textColor(): Color? = (this as? TransitStep.Ride)?.line?.textColor?.toColorOrNull()

/**
 * The journey at a glance: when it leaves, when it lands, how long that is, and how many changes.
 *
 * The duration shown is arrival minus departure and not the sum of the steps. The two differ by every
 * minute spent waiting on a platform, and a traveller deciding whether they have time for this is
 * asking about the first number.
 */
@Composable
private fun TransitJourneySummaryCard(journey: TransitJourney, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = listOfNotNull(journey.departure?.clock(), journey.arrival?.clock())
                        .joinToString(" → "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                journey.totalDuration?.let {
                    Text(text = it.toString(), style = MaterialTheme.typography.titleMedium)
                }
            }

            Text(
                text = if (journey.changes == 0) {
                    stringResource(Res.string.transit_preview_no_change)
                } else {
                    stringResource(Res.string.transit_preview_changes, journey.changes)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                journey.steps.forEach { step -> TransitStepBadge(step) }
            }
        }
    }
}

/** One step of the journey reduced to what fits on a badge: the line, or how long the walk is. */
@Composable
private fun TransitStepBadge(step: TransitStep, modifier: Modifier = Modifier) {
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

/**
 * Two ways across Milan, the first with a change on the way.
 *
 * `@PreviewScreenSizes` is what makes this worth having: the scaffold puts the timeline beside the
 * map on a wide window and inside a bottom sheet on a narrow one, and only both widths together say
 * whether a row that carries a time, a line, an operator and a stop count still reads.
 *
 * The steps carry no geometry. A polyline in a fixture would have to be a real HERE flexible
 * encoding to decode into anything, and what is being looked at here is the timeline — so the map
 * draws its base style with nothing on it, which is also what an answer without geometry looks like.
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

/** Central European summer time, which is the offset the departure boards in the fixture read in. */
private val PREVIEW_OFFSET = UtcOffset(hours = 2)

/** A moment on the fixture's timetable, fixed rather than relative to now so the preview never moves. */
private fun previewTime(hour: Int, minute: Int): TransitTime = TransitTime(
    instant = LocalDateTime(
        year = 2026,
        monthNumber = 5,
        dayOfMonth = 14,
        hour = hour,
        minute = minute,
    )
        .toInstant(PREVIEW_OFFSET),
    offset = PREVIEW_OFFSET,
)

private fun previewStop(
    name: String,
    lat: Double,
    lng: Double,
    arrival: TransitTime? = null,
    departure: TransitTime? = null,
): TransitStop = TransitStop(
    location = RouteLocation(lat = lat, lng = lng),
    name = name,
    arrival = arrival,
    departure = departure,
)

private val PREVIEW_AGENCY = TransitAgency(name = "ATM", id = "atm", website = "")

/** Assago to Brera, changing from the underground onto a tram. */
private val PREVIEW_WITH_CHANGE = TransitJourney(
    summary = RouteSummary(durationSeconds = 43.minutes, distance = 12_190 * Length.meters),
    steps = listOf(
        TransitStep.Walk(
            summary = RouteSummary(durationSeconds = 4.minutes, distance = 320 * Length.meters),
            departure = previewTime(9, 27),
            arrival = previewTime(9, 31),
            from = RouteLocation(lat = 45.40887, lng = 9.12565),
            to = RouteLocation(lat = 45.41043, lng = 9.12718),
        ),
        TransitStep.Ride(
            summary = RouteSummary(durationSeconds = 21.minutes, distance = 9_200 * Length.meters),
            line = TransitLine(
                mode = "SUBWAY",
                name = "M2",
                shortName = "M2",
                category = "Metropolitana",
                headsign = "Cologno Nord",
                color = "#00A94F",
                textColor = "#FFFFFF",
                wheelchairAccessible = WheelchairAccess.YES,
                url = "",
            ),
            agency = PREVIEW_AGENCY,
            boarding = previewStop(
                "Assago Milanofiori Forum",
                45.41043,
                9.12718,
                departure = previewTime(9, 31),
            ),
            alighting = previewStop("Cadorna FN", 45.46870, 9.17570, arrival = previewTime(9, 52)),
            intermediateStops = listOf(
                previewStop("Famagosta", 45.43128, 9.15676, departure = previewTime(9, 38)),
                previewStop("Porta Genova FS", 45.45191, 9.17300, departure = previewTime(9, 44)),
                previewStop("Sant'Ambrogio", 45.46246, 9.17500, departure = previewTime(9, 49)),
            ),
        ),
        TransitStep.Walk(
            summary = RouteSummary(durationSeconds = 3.minutes, distance = 210 * Length.meters),
            departure = previewTime(9, 52),
            arrival = previewTime(9, 55),
        ),
        TransitStep.Ride(
            summary = RouteSummary(durationSeconds = 11.minutes, distance = 2_200 * Length.meters),
            line = TransitLine(
                mode = "LIGHT_RAIL",
                name = "1",
                shortName = "1",
                category = "Tram",
                headsign = "Greco",
                color = "#F5A623",
                textColor = "#000000",
                wheelchairAccessible = WheelchairAccess.LIMITED,
            ),
            agency = PREVIEW_AGENCY,
            boarding = previewStop("Cadorna", 45.46830, 9.17660, departure = previewTime(9, 57)),
            alighting = previewStop("Manzoni", 45.46980, 9.19170, arrival = previewTime(10, 8)),
            intermediateStops = listOf(
                previewStop("Cordusio", 45.46590, 9.18420, departure = previewTime(10, 2)),
            ),
        ),
        TransitStep.Walk(
            summary = RouteSummary(durationSeconds = 4.minutes, distance = 260 * Length.meters),
            departure = previewTime(10, 8),
            arrival = previewTime(10, 12),
        ),
    ),
)

/** The same trip on one coach: longer, but nothing to catch in the middle. */
private val PREVIEW_DIRECT = TransitJourney(
    summary = RouteSummary(durationSeconds = 52.minutes, distance = 13_400 * Length.meters),
    steps = listOf(
        TransitStep.Walk(
            summary = RouteSummary(durationSeconds = 6.minutes, distance = 480 * Length.meters),
            departure = previewTime(9, 24),
            arrival = previewTime(9, 30),
        ),
        TransitStep.Ride(
            summary = RouteSummary(durationSeconds = 41.minutes, distance = 12_520 * Length.meters),
            line = TransitLine(
                mode = "BUS",
                name = "321",
                shortName = "321",
                category = "Autobus",
                headsign = "Milano Famagosta",
            ),
            agency = PREVIEW_AGENCY,
            boarding = previewStop(
                "Assago Forum",
                45.40887,
                9.12565,
                departure = previewTime(9, 30),
            ),
            alighting = previewStop(
                "Via Manzoni",
                45.46980,
                9.19170,
                arrival = previewTime(10, 11),
            ),
        ),
        TransitStep.Walk(
            summary = RouteSummary(durationSeconds = 5.minutes, distance = 400 * Length.meters),
            departure = previewTime(10, 11),
            arrival = previewTime(10, 16),
        ),
    ),
)

//region Preview
