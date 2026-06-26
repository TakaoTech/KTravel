package com.takaotech.ktravel.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.Route
import com.takaotech.ktravel.domain.routing.model.RouteDeparture
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSection
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.presentation.planning.StepUi
import kotlin.time.Duration.Companion.minutes

class TravelDayStepPreviewParameterProvider(val items: Int) : PreviewParameterProvider<StepUi> {

    override val values: Sequence<StepUi>
        get() = (1..items).map {
            if (it % 2 == 0) generateTransportStep(it) else generatePlaceStep(it)
        }.asSequence()

    private fun generatePlaceStep(index: Int): StepUi.Place {
        return StepUi.Place(
            name = "Place $index",
            lat = 45.0 + index * 0.1,
            lng = 9.0 + index * 0.1
        )
    }

    private fun generateTransportStep(index: Int): StepUi.Transport {
        val type = TransportType.entries[index % TransportType.entries.size]
        val route = Route(
            sections = listOf(
                RouteSection(
                    summary = RouteSummary(
                        durationSeconds = (30 * index).minutes,
                        distanceMeters = 1000 * index
                    ),
                    departure = RouteDeparture(
                        location = RouteLocation(
                            lat = 45.0 + index * 0.1,
                            lng = 9.0 + index * 0.1
                        )
                    ),
                    arrival = RouteDeparture(
                        location = RouteLocation(
                            lat = 45.0 + (index + 1) * 0.1,
                            lng = 9.0 + (index + 1) * 0.1
                        )
                    )
                )
            )
        )
        return StepUi.Transport(
            type = type,
            route = route
        )
    }
}
