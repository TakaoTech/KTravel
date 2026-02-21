package com.takaotech.ktravel.presentation.planner.transport

import androidx.compose.runtime.Stable
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import com.takaotech.ktravel.presentation.planner.TravelDay
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

@Stable
data class PlanningTransportUiState(
    val availableProviders: PersistentList<RoutingProviderType> = RoutingProviderType.entries.toPersistentList(),
    val selectedProvider: RoutingProviderType = RoutingProviderType.LOCAL,

    val startPlace: TravelDay.Step.Place? = null,
    val endPlace: TravelDay.Step.Place? = null,
)