package com.takaotech.ktravel.presentation.planning.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.RoutingProvider
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import com.takaotech.ktravel.presentation.planning.TravelDay
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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

    private val _navigationEvent = Channel<PlanningTransportNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

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

    private var calculateTransportJob: Job? = null

    fun calculateTransport() {
        if (calculateTransportJob?.isActive == true) return
        calculateTransportJob = viewModelScope.launch(Dispatchers.Default) {
            // TODO Change to a common LatLng

            mUiState.update {
                it.copy(
                    isLoading = true,
                    routes = null,
                )
            }

            val routes = with(mUiState.value) {
                withContext(Dispatchers.IO) {
                    providersMap.getRoutes(
                        origin = "${startPlace!!.lat},${startPlace.lng}",
                        destination = "${endPlace!!.lat},${endPlace.lng}",
                        settings = mUiState.value.providerSettings
                    )
                }
            }

            mUiState.update {
                it.copy(
                    isLoading = false,
                    routes = routes,
                    selectedRouteIndex = 0,
                )
            }

            // TODO change this
            _navigationEvent.send(PlanningTransportNavigationEvent.NavigateToRoutePreview)
        }
    }

    fun selectRoute(index: Int) {
        mUiState.update { it.copy(selectedRouteIndex = index) }
    }

    fun saveSelectedRoute() {
        viewModelScope.launch(Dispatchers.Default) {
            val state = mUiState.value
            val selectedRoute = state.routes?.routes?.getOrNull(state.selectedRouteIndex) ?: return@launch

            // TODO Change this transformation
            val transportType = when (selectedRoute.sections.firstOrNull()?.transport?.mode?.uppercase()) {
                "TRAIN" -> TravelDay.Step.Transport.Type.TRAIN
                "BUS" -> TravelDay.Step.Transport.Type.BUS
                "FLIGHT" -> TravelDay.Step.Transport.Type.FLIGHT
                else -> TravelDay.Step.Transport.Type.CAR
            }
            val transportStep = TravelDay.Step.Transport(
                type = transportType,
                route = selectedRoute
            )


            repository.addTransportStep(dayId, startPlaceId, transportStep)
        }
    }
}