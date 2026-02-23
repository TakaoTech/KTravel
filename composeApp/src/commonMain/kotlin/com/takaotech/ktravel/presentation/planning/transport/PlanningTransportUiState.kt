package com.takaotech.ktravel.presentation.planning.transport

import androidx.compose.runtime.Stable
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import com.takaotech.ktravel.presentation.planning.TravelDay
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

@Stable
data class PlanningTransportUiState(
    val availableProviders: PersistentList<RoutingProviderType> = RoutingProviderType.entries.toPersistentList(),
    val selectedProvider: RoutingProviderType = RoutingProviderType.LOCAL,
    val providerSettings: RoutingProviderSettings = RoutingProviderSettings.Local(),

    val startPlace: TravelDay.Step.Place? = null,
    val endPlace: TravelDay.Step.Place? = null,
)