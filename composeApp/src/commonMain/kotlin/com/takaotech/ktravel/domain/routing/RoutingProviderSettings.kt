package com.takaotech.ktravel.domain.routing

import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

sealed interface RoutingProviderSettings {
    data class Local(
        val avoidTolls: Boolean = false,
        val transportMode: LocalTransportMode = LocalTransportMode.CAR
    ) : RoutingProviderSettings

    @Stable
    data class Here(
        val transportMode: HereTransportMode = HereTransportMode.CAR,
        val routingMode: HereRoutingMode = HereRoutingMode.FAST,
        val alternatives: Int = 1,
        val departureDate: LocalDate? = null,
        val departureTime: LocalTime? = null
    ) : RoutingProviderSettings {
        enum class HereTransportMode {
            CAR,
            PEDESTRIAN,
            BICYCLE,
            SCOOTER
        }

        enum class HereRoutingMode { FAST, SHORT }
    }

    data class GMaps(
        val avoidTolls: Boolean = false,
        val avoidFerries: Boolean = false,
        val travelMode: GMapsTravelMode = GMapsTravelMode.DRIVING,
    ) : RoutingProviderSettings
}

enum class LocalTransportMode { CAR, BICYCLE, WALKING }
enum class GMapsTravelMode { DRIVING, WALKING, BICYCLING, TRANSIT }