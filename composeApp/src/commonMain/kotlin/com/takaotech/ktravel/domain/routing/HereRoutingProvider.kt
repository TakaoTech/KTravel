package com.takaotech.ktravel.domain.routing

import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.navigation.routing.client.HereRoutingClient
import com.takaotech.navigation.routing.dto.request.DepartureTime
import com.takaotech.navigation.routing.dto.request.RoutesRequest
import com.takaotech.navigation.routing.dto.request.Waypoint
import com.takaotech.navigation.routing.model.RoutingMode
import com.takaotech.navigation.routing.model.TransportMode
import kotlinx.coroutines.supervisorScope
import kotlinx.datetime.LocalDateTime

class HereRoutingProvider(
    private val settingsRepository: SettingsRepository
) : RoutingProvider {

    private val routingClient: HereRoutingClient = HereRoutingClient(apiKey = settingsRepository.hereApiKey.value)

    override suspend fun getRoutes(
        origin: String,
        destination: String,
        settings: RoutingProviderSettings
    ): Unit = supervisorScope {
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

        routingClient.getRoutes(
            RoutesRequest(
                origin = Waypoint.fromString(origin),
                destination = Waypoint.fromString(destination),
                transportMode = transportMode,
                routingMode = routingMode,
                alternatives = settings.alternatives,
                departureTime = departureTime
            )
        )
    }
}
