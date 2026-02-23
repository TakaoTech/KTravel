package com.takaotech.ktravel.presentation.planning.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.RoutingProvider
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import com.takaotech.ktravel.presentation.planning.TravelDay
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
        val defaultSettings = when (providerType) {
            RoutingProviderType.LOCAL -> RoutingProviderSettings.Local()
            RoutingProviderType.HERE -> RoutingProviderSettings.Here()
            RoutingProviderType.GMAPS -> RoutingProviderSettings.GMaps()
        }
        val newProvider = mUiState.updateAndGet {
            it.copy(
                selectedProvider = providerType,
                providerSettings = defaultSettings
            )
        }.selectedProvider
        providersMap = get<RoutingProvider>(named(newProvider))
    }

    fun updateProviderSettings(settings: RoutingProviderSettings) {
        mUiState.update {
            it.copy(providerSettings = settings)
        }
    }

    fun calculateTransport() {
        viewModelScope.launch {
            //TODO Change to a common LatLng
            with(mUiState.value) {
                providersMap.getRoutes(
                    origin = "${startPlace!!.lat},${startPlace.lng}",
                    destination = "${endPlace!!.lat},${endPlace.lng}",
                    settings = mUiState.value.providerSettings
                )
            }

        }
    }
}