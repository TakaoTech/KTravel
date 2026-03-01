package com.takaotech.ktravel.presentation.planning.transport

import androidx.compose.runtime.Stable
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import com.takaotech.ktravel.domain.routing.model.Routes
import com.takaotech.ktravel.presentation.planning.TravelDay
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

sealed interface PlanningTransportNavigationEvent {
    data object NavigateToRoutePreview : PlanningTransportNavigationEvent
}

@Stable
data class PlanningTransportUiState(
    val availableProviders: PersistentList<RoutingProviderType> = RoutingProviderType.entries.toPersistentList(),
    val selectedProvider: RoutingProviderType = RoutingProviderType.LOCAL,
    val providerSettings: RoutingProviderSettings = RoutingProviderSettings.Local(),

    val startPlace: TravelDay.Step.Place? = null,
    val endPlace: TravelDay.Step.Place? = null,

    val routes: Routes? = null,
    val selectedRouteIndex: Int = 0,
)