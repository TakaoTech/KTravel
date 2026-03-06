package com.takaotech.ktravel.presentation.planning.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.RoutingProviderFactory
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import com.takaotech.ktravel.presentation.planning.TravelDay
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class PlanningTransportViewModel(
    @InjectedParam private val dayId: String,
    @InjectedParam private val startPlaceId: String,
    @InjectedParam private val endPlaceId: String,
    private val repository: TravelPlanRepository,
    private val providerFactory: RoutingProviderFactory,
) : ViewModel() {

    private val mUiState = MutableStateFlow(PlanningTransportUiState())
    val uiState = mUiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<PlanningTransportNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var currentProvider = providerFactory.getProvider(mUiState.value.selectedProvider)

    init {
        viewModelScope.launch {
            repository.getTravelDayFlow(dayId).map {
                it.steps.first { it.id == startPlaceId } to it.steps.first { it.id == endPlaceId }
            }.collect { (startStep, endStep) ->
                val startUi = (startStep as? StepDomain.Place)?.let {
                    TravelDay.Step.Place(id = it.id, location = it.location, lat = it.lat, lng = it.lng)
                }
                val endUi = (endStep as? StepDomain.Place)?.let {
                    TravelDay.Step.Place(id = it.id, location = it.location, lat = it.lat, lng = it.lng)
                }
                mUiState.update {
                    it.copy(
                        startPlace = startUi,
                        endPlace = endUi
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
        currentProvider = providerFactory.getProvider(newProvider)
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
                    currentProvider.getRoutes(
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
            _navigationEvent.emit(PlanningTransportNavigationEvent.NavigateToRoutePreview)
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
                "TRAIN" -> TransportType.TRAIN
                "BUS" -> TransportType.BUS
                "FLIGHT" -> TransportType.FLIGHT
                else -> TransportType.CAR
            }
            val transportStep = StepDomain.Transport(
                type = transportType,
                route = selectedRoute
            )

            repository.addTransportStep(dayId, startPlaceId, transportStep)
        }
    }
}