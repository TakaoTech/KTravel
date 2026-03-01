package com.takaotech.ktravel.domain.routing

import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.ktravel.domain.routing.model.*
import com.takaotech.navigation.routing.client.HereRoutingClient
import com.takaotech.navigation.routing.dto.request.DepartureTime
import com.takaotech.navigation.routing.dto.request.ReturnAttribute
import com.takaotech.navigation.routing.dto.request.RoutesRequest
import com.takaotech.navigation.routing.dto.request.Waypoint
import com.takaotech.navigation.routing.dto.response.RouterRouteResponse
import com.takaotech.navigation.routing.model.RoutingMode
import com.takaotech.navigation.routing.model.TransportMode
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeComponents
import kotlin.time.Duration.Companion.seconds

class HereRoutingProvider(
    private val settingsRepository: SettingsRepository
) : RoutingProvider {

    private val routingClient: HereRoutingClient = HereRoutingClient(
        apiKey = settingsRepository.hereApiKey.value,
        enableLogging = true
    )

    override suspend fun getRoutes(
        origin: String,
        destination: String,
        settings: RoutingProviderSettings
    ): Routes = withContext(Dispatchers.Default) {
        supervisorScope {
            require(settings is RoutingProviderSettings.Here) {
                "Expected Here settings but got: ${settings::class.simpleName}"
            }

            val transportMode = when (settings.transportMode) {
                RoutingProviderSettings.Here.HereTransportMode.CAR -> TransportMode.CAR
                RoutingProviderSettings.Here.HereTransportMode.PEDESTRIAN -> TransportMode.PEDESTRIAN
                RoutingProviderSettings.Here.HereTransportMode.BICYCLE -> TransportMode.BICYCLE
                RoutingProviderSettings.Here.HereTransportMode.SCOOTER -> TransportMode.SCOOTER
            }

            val routingMode = when (settings.routingMode) {
                RoutingProviderSettings.Here.HereRoutingMode.FAST -> RoutingMode.FAST
                RoutingProviderSettings.Here.HereRoutingMode.SHORT -> RoutingMode.SHORT
            }

            val departureTime = if (settings.departureDate != null && settings.departureTime != null) {
                DepartureTime.fromLocalDateTime(
                    LocalDateTime(settings.departureDate, settings.departureTime)
                )
            } else {
                null
            }

            val result = withContext(Dispatchers.IO) {
                routingClient.getRoutes(
                    RoutesRequest(
                        origin = Waypoint.fromString(origin),
                        destination = Waypoint.fromString(destination),
                        transportMode = transportMode,
                        routingMode = routingMode,
                        alternatives = settings.alternatives,
                        departureTime = departureTime,
                        returnAttributes = listOf(
                            ReturnAttribute.POLYLINE,
                            ReturnAttribute.ACTIONS,
                            ReturnAttribute.TURN_BY_TURN_ACTIONS,
                            ReturnAttribute.INSTRUCTIONS,
                            ReturnAttribute.SUMMARY,
                            ReturnAttribute.TRAVEL_SUMMARY,
                            ReturnAttribute.TYPICAL_DURATION,
                            ReturnAttribute.TOLLS
                        )
                    )
                )
            }

            mapHereRouteResponse(result.getOrThrow())
        }
    }
}

internal fun mapHereRouteResponse(response: RouterRouteResponse): Routes {
    return Routes(
        routes = response.routes.map { route ->
            val steps = route.sections.map { section ->
                RouteSection(
                    summary = RouteSummary(
                        durationSeconds = (section.summary?.duration ?: 0).seconds,
                        distanceMeters = section.summary?.length ?: 0
                    ),

                    actions = section.actions?.map { action ->
                        RouteAction(
                            action = action.action,
                            durationSeconds = action.duration.seconds,
                            distanceMeters = action.length * Length.meters,
                            instruction = action.instruction,
//                            offset = action.offset,
                            direction = action.direction,
                            severity = action.severity
                        )
                    }.orEmpty(),
                    departure = section.departure.let { dep ->

                        RouteDeparture(
                            location = RouteLocation(
                                lat = dep.place.location.lat,
                                lng = dep.place.location.lng
                            ),
                            time = dep.time?.let { time ->
                                DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.parse(time)
                            }
                        )
                    },
                    arrival = section.arrival.let { arr ->
                        RouteDeparture(
                            location = RouteLocation(
                                lat = arr.place.location.lat,
                                lng = arr.place.location.lng
                            ),
                            time = arr.time?.let { time ->
                                DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.parse(time)
                            }
                        )
                    },
                    transport = RouteTransport(mode = section.transport.mode.name),
                    polyline = section.polyline,
                    tollSystems = section.tollSystems?.map {
                        RouteTollSystem(id = it.id, name = it.name)
                    } ?: emptyList(),
                    tolls = section.tolls?.map {
                        RouteTollCost(
                            tollSystem = it.tollSystem,
                            tollSystemRef = it.tollSystemRef,
                            tollSystems = it.tollSystems,
                            countryCode = it.countryCode
                        )
                    } ?: emptyList()
                )
            }

            Route(
                sections = steps,
            )
        }
    )
}
