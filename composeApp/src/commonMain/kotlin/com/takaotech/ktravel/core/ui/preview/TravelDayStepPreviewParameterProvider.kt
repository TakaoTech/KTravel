package com.takaotech.ktravel.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.RouteDeparture
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.RoutingSection
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.VisitScheduleUi
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.minutes

class TravelDayStepPreviewParameterProvider(val items: Int) : PreviewParameterProvider<StepUi> {

    private val loremIpsum = LoremIpsum(3).values.first()

    override val values: Sequence<StepUi>
        get() = (1..items).map {
            if (it % 2 == 0) generateTransportStep(it) else generatePlaceStep(it)
        }.asSequence()

    private fun generatePlaceStep(index: Int): StepUi.Place = StepUi.Place(
        name = "$loremIpsum $index",
        lat = 45.0 + index * 0.1,
        lng = 9.0 + index * 0.1,
        schedule = VisitScheduleUi(
            startTime = LocalTime(8 + index, 0),
            endTime = LocalTime(8 + index, 30),
        ),
    )

    private fun generateTransportStep(index: Int): StepUi.Transport {
        val type = TransportType.entries[index % TransportType.entries.size]
        val summary = RouteSummary(
            durationSeconds = (30 * index).minutes,
            distance = (1000.0 * index) * Length.meters,
        )
        return StepUi.Transport(
            type = type,
            answer = TransportAnswer.Routing(
                RoutingRoute(
                    summary = summary,
                    sections = listOf(
                        RoutingSection(
                            summary = summary,
                            mode = type.name,
                            departure = RouteDeparture(
                                location = RouteLocation(
                                    lat = 45.0 + index * 0.1,
                                    lng = 9.0 + index * 0.1,
                                ),
                            ),
                            arrival = RouteDeparture(
                                location = RouteLocation(
                                    lat = 45.0 + (index + 1) * 0.1,
                                    lng = 9.0 + (index + 1) * 0.1,
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }
}
