package com.takaotech.ktravel.presentation.planning.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.routing.RoutingProviderFactory
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AssistedInject
class PlanningTransportViewModel(
    @Assisted private val travelId: String,
    @Assisted private val dayId: String,
    @Assisted private val startPlaceId: String,
    @Assisted private val endPlaceId: String,
    private val providerFactory: RoutingProviderFactory,
    private val planningGraphStore: PlanningGraphStore,
) : ViewModel() {

    @AssistedFactory
    @ContributesIntoMap(com.takaotech.ktravel.di.AppScope::class)
    @ManualViewModelAssistedFactoryKey
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(
            travelId: String,
            dayId: String,
            startPlaceId: String,
            endPlaceId: String,
        ): PlanningTransportViewModel
    }

    private val planningGraph get() = planningGraphStore.getOrCreate(travelId)

    private val mUiState = MutableStateFlow(PlanningTransportUiState())
    val uiState = mUiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<PlanningTransportNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var currentProvider = providerFactory.getProvider(mUiState.value.selectedProvider)

    init {
        viewModelScope.launch {
            planningGraph.travelPlanRepository
                .getTravelDayFlow(dayId).map {
                    it.steps.first { it.id == startPlaceId } to it.steps.first { it.id == endPlaceId }
                }.collect { (startStep, endStep) ->
                    val startUi = (startStep as? StepDomain.Place)?.let {
                        with(TravelPlanUiMapper) { it.toUiStepPlace() }
                    }
                    val endUi = (endStep as? StepDomain.Place)?.let {
                        with(TravelPlanUiMapper) { it.toUiStepPlace() }
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

            _navigationEvent.emit(PlanningTransportNavigationEvent.NavigateToRoutePreview)
        }
    }

    fun selectRoute(index: Int) {
        mUiState.update { it.copy(selectedRouteIndex = index) }
    }

    fun saveSelectedRoute() {
        viewModelScope.launch(Dispatchers.Default) {
            val state = mUiState.value
            val selectedRoute =
                state.routes?.routes?.getOrNull(state.selectedRouteIndex) ?: return@launch
            planningGraph.saveTransportStepUseCase(dayId, startPlaceId, selectedRoute)
        }
    }
}
