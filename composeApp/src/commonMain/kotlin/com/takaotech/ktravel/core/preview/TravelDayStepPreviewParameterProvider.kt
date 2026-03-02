package com.takaotech.ktravel.core.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.domain.routing.model.*
import com.takaotech.ktravel.presentation.planning.TravelDay
import kotlin.time.Duration.Companion.minutes

class TravelDayStepPreviewParameterProvider(val items: Int) : PreviewParameterProvider<TravelDay.Step> {

    override val values: Sequence<TravelDay.Step>
        get() = (1..items).map {
            if (it % 2 == 0) generateTransportStep(it) else generatePlaceStep(it)
        }.asSequence()

    private fun generatePlaceStep(index: Int): TravelDay.Step.Place {
        return TravelDay.Step.Place(
            location = "Place $index",
            lat = 45.0 + index * 0.1,
            lng = 9.0 + index * 0.1
        )
    }

    private fun generateTransportStep(index: Int): TravelDay.Step.Transport {
        val type = TravelDay.Step.Transport.Type.entries[index % TravelDay.Step.Transport.Type.entries.size]
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
        return TravelDay.Step.Transport(
            type = type,
            route = route
        )
    }
}
