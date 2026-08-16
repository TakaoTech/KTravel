package com.takaotech.ktravel.presentation.planning.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.RoutingCatalog
import com.takaotech.ktravel.domain.routing.RoutingFailure
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import com.takaotech.ktravel.presentation.planning.transport.options.routeOptionsScreen
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AssistedInject
class PlanningTransportViewModel(
    @Assisted private val travelId: String,
    @Assisted private val dayId: String,
    @Assisted private val startPlaceId: String,
    @Assisted private val endPlaceId: String,
    private val planningGraphStore: PlanningGraphStore,
) : ViewModel() {

    @AssistedFactory
    @ContributesIntoMap(AppScope::class)
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
    private val routingService get() = planningGraph.routingService
    private val navigatorTargets get() = planningGraph.navigatorTargetResolver

    /**
     * The request the options block is assembling.
     *
     * Read and never written here beyond clearing it: which vehicle, which options and the rules
     * between them belong to the presenter of the profile's family, and this view model would only
     * be a second place to keep them consistent.
     */
    private val routeOptionsDraft get() = planningGraph.routeOptionsDraft

    private val mUiState = MutableStateFlow(PlanningTransportUiState())
    val uiState = mUiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<PlanningTransportNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            planningGraph.travelPlanRepository
                .getTravelDayFlow(dayId).map {
                    it.steps.first { step -> step.id == startPlaceId } to
                        it.steps.first { step -> step.id == endPlaceId }
                }.collect { (startStep, endStep) ->
                    val startUi = (startStep as? StepDomain.Place)?.let {
                        with(TravelPlanUiMapper) { it.toUiStepPlace() }
                    }
                    val endUi = (endStep as? StepDomain.Place)?.let {
                        with(TravelPlanUiMapper) { it.toUiStepPlace() }
                    }
                    mUiState.update { it.copy(startPlace = startUi, endPlace = endUi) }
                }
        }

        // Calculate follows the options block: it lights up once that block has published a request
        // for the profile currently chosen, and goes out again the moment the profile changes.
        viewModelScope.launch {
            routeOptionsDraft.selection.collect { selection ->
                mUiState.update { state ->
                    state.copy(isRequestReady = selection != null && selection.profileId == state.selectedProfileId)
                }
            }
        }

        // The plan's preference decides where the screen opens, and a remote it cannot reach is not
        // offered at all: switching to a navigator with no address configured would produce a
        // failure the traveller cannot act on from here.
        val preferred = navigatorTargets.defaultKind()
        val remoteConfigured = navigatorTargets.isRemoteConfigured()
        val kind = if (preferred == NavigatorKind.REMOTE && !remoteConfigured) NavigatorKind.EMBEDDED else preferred

        mUiState.update { it.copy(navigatorKind = kind, isRemoteConfigured = remoteConfigured) }
        loadCatalog(kind)
    }

    // ---- which navigator ----------------------------------------------------------------------

    /** Switches navigator for this calculation only; the plan's preference is not rewritten. */
    fun selectNavigator(kind: NavigatorKind) {
        if (kind == mUiState.value.navigatorKind) return
        mUiState.update { it.copy(navigatorKind = kind, failure = null) }
        loadCatalog(kind)
    }

    /** Asks again, after a navigator that did not answer. */
    fun retryCatalog() = loadCatalog(mUiState.value.navigatorKind)

    private var catalogJob: Job? = null

    private fun loadCatalog(kind: NavigatorKind) {
        catalogJob?.cancel()
        mUiState.update { it.copy(isCatalogLoading = true) }

        catalogJob = viewModelScope.launch {
            val catalog = routingService.catalog(kind)
            mUiState.update { it.copy(isCatalogLoading = false, catalog = catalog) }
            keepProfileValid(catalog)
        }
    }

    /**
     * Keeps the selection meaningful across a change of navigator.
     *
     * A profile picked on one navigator may not exist on the other, and leaving it selected would
     * leave Calculate pointing at something this server does not serve. Falling back to the first
     * usable one is what the traveller would do anyway.
     */
    private fun keepProfileValid(catalog: RoutingCatalog) {
        val selectedId = mUiState.value.selectedProfileId
        if (catalog.options.any { it.profile.id == selectedId && it.isSelectable }) return

        val fallback = catalog.selectable.firstOrNull()
        if (fallback == null) {
            routeOptionsDraft.clear()
            mUiState.update { it.copy(selectedProfileId = null, routeOptionsScreen = null) }
            return
        }

        selectProfile(fallback.profile.id)
    }

    // ---- what to ask it for -------------------------------------------------------------------

    /**
     * Selects a profile and hands the options over to the block that belongs to it.
     *
     * The previous request is dropped rather than adapted: modes are declared per profile and their
     * vocabularies collide by name without meaning the same thing, so a mode kept from the previous
     * profile would be a value from another API's list that happens to spell the same. The new
     * block seeds its own defaults as soon as it composes.
     */
    fun selectProfile(profileId: RoutingProfileId) {
        // Outside the update: it retries its lambda under contention, and a side effect in there
        // would run more than once for one choice.
        routeOptionsDraft.clear()

        mUiState.update { state ->
            val profile = state.catalog?.options?.firstOrNull { it.profile.id == profileId }?.profile

            state.copy(
                selectedProfileId = profileId,
                routeOptionsScreen = profile?.routeOptionsScreen(travelId),
                failure = null,
            )
        }
    }

    // ---- the answer ---------------------------------------------------------------------------

    private var calculateTransportJob: Job? = null

    fun calculateTransport() {
        if (calculateTransportJob?.isActive == true) return

        val state = mUiState.value
        val selection = routeOptionsDraft.selection.value?.takeIf { it.profileId == state.selectedProfileId } ?: return
        val start = state.startPlace ?: return
        val end = state.endPlace ?: return

        calculateTransportJob = viewModelScope.launch(Dispatchers.Default) {
            mUiState.update { it.copy(isLoading = true, failure = null, routes = null) }

            // Every failure is caught. The previous implementation caught none, so a rejected key
            // escaped into the view model scope and left the screen loading forever — which stops
            // being rare the moment a remote navigator, a token and a rate limit are involved.
            val routes = try {
                routingService.routes(
                    kind = state.navigatorKind,
                    origin = "${start.lat},${start.lng}",
                    destination = "${end.lat},${end.lng}",
                    selection = selection,
                )
            } catch (failure: RoutingFailure) {
                mUiState.update { it.copy(isLoading = false, failure = failure.toReason()) }
                return@launch
            }

            mUiState.update { it.copy(isLoading = false, routes = routes, selectedRouteIndex = 0) }
            _navigationEvent.emit(PlanningTransportNavigationEvent.NavigateToRoutePreview)
        }
    }

    fun dismissFailure() = mUiState.update { it.copy(failure = null) }

    fun selectRoute(index: Int) = mUiState.update { it.copy(selectedRouteIndex = index) }

    fun saveSelectedRoute() {
        viewModelScope.launch(Dispatchers.Default) {
            val state = mUiState.value
            val selectedRoute = state.routes?.routes?.getOrNull(state.selectedRouteIndex) ?: return@launch
            planningGraph.saveTransportStepUseCase(dayId, startPlaceId, selectedRoute)
        }
    }
}

private fun RoutingFailure.toReason(): TransportFailureReason = when (this) {
    is RoutingFailure.NavigatorUnreachable -> TransportFailureReason.NAVIGATOR_UNREACHABLE
    is RoutingFailure.NotAuthenticated -> TransportFailureReason.NOT_AUTHENTICATED
    is RoutingFailure.ProviderCredentials -> TransportFailureReason.PROVIDER_CREDENTIALS
    is RoutingFailure.RateLimited -> TransportFailureReason.RATE_LIMITED
    is RoutingFailure.ProviderUnavailable -> TransportFailureReason.PROVIDER_UNAVAILABLE
    is RoutingFailure.NoRouteFound -> TransportFailureReason.NO_ROUTE_FOUND
    is RoutingFailure.InvalidRequest -> TransportFailureReason.INVALID_REQUEST
    is RoutingFailure.Unexpected -> TransportFailureReason.UNEXPECTED
}
