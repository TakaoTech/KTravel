package com.takaotech.ktravel.presentation.planner.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.RoutingProvider
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import com.takaotech.ktravel.presentation.planner.TravelDay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

@KoinViewModel
class PlanningTransportViewModel(
    @InjectedParam private val dayId: String,
    @InjectedParam private val startPlaceId: String,
    @InjectedParam private val endPlaceId: String,
    private val repository: TravelPlanRepository,
) : ViewModel(), KoinComponent {

    private val mUiState = MutableStateFlow(PlanningTransportUiState())
    val uiState = mUiState.asStateFlow()

    private var providersMap = get<RoutingProvider>(named(mUiState.value.selectedProvider))

    init {
        viewModelScope.launch {
            repository.getTravelDayFlow(dayId).map {
                it.steps.first { it.id == startPlaceId } to it.steps.first { it.id == endPlaceId }
            }.collect { (startStep, endStep) ->
                mUiState.update {
                    it.copy(
                        startPlace = startStep as TravelDay.Step.Place,
                        endPlace = endStep as TravelDay.Step.Place
                    )
                }
            }
        }
    }


    fun selectProvider(providerType: RoutingProviderType) {
        val newProvider = mUiState.updateAndGet {
            it.copy(
                selectedProvider = providerType
            )
        }.selectedProvider
        providersMap = get<RoutingProvider>(named(newProvider))
    }
}