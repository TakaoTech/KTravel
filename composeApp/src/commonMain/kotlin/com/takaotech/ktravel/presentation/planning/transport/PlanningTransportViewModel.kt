package com.takaotech.ktravel.presentation.planning.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.ModeSelection
import com.takaotech.ktravel.domain.routing.RoutingCatalog
import com.takaotech.ktravel.domain.routing.RoutingFailure
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

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
            mUiState.update { state ->
                state.copy(isCatalogLoading = false, catalog = catalog).withProfileStillValid(catalog)
            }
        }
    }

    /**
     * Keeps the selection meaningful across a change of navigator.
     *
     * A profile picked on one navigator may not exist on the other, and leaving it selected would
     * leave Calculate pointing at something this server does not serve. Falling back to the first
     * usable one is what the traveller would do anyway.
     */
    private fun PlanningTransportUiState.withProfileStillValid(catalog: RoutingCatalog): PlanningTransportUiState {
        val stillUsable = catalog.options.firstOrNull { it.profile.id == selectedProfileId && it.isSelectable }
        if (stillUsable != null) return this

        val fallback = catalog.selectable.firstOrNull() ?: return copy(
            selectedProfileId = null,
            selectedMode = null,
            modeFilter = persistentSetOf(),
        )

        return withProfile(fallback.profile.id)
    }

    // ---- what to ask it for -------------------------------------------------------------------

    fun selectProfile(profileId: RoutingProfileId) {
        mUiState.update { it.withProfile(profileId) }
    }

    /**
     * Selects a profile and resets everything that belonged to the previous one.
     *
     * The options are not carried across: modes are declared per profile and their vocabularies
     * collide by name without meaning the same thing, so a mode kept from the previous profile would
     * be a value from another API's list that happens to spell the same.
     */
    private fun PlanningTransportUiState.withProfile(profileId: RoutingProfileId): PlanningTransportUiState {
        val profile = catalog?.options?.firstOrNull { it.profile.id == profileId }?.profile

        return copy(
            selectedProfileId = profileId,
            selectedMode = profile?.takeIf { it.modeSelection == ModeSelection.SINGLE }?.modes?.firstOrNull(),
            // Empty rather than everything: upstream reads no filter as no restriction, which is
            // what a traveller who has not chosen means.
            modeFilter = persistentSetOf(),
            alternatives = alternatives.coerceAtMost(profile?.maxAlternatives ?: 1),
            avoidTolls = avoidTolls && profile?.supportsTolls == true,
            shortestDistance = false,
            failure = null,
        )
    }

    fun selectMode(mode: RoutingMode) {
        mUiState.update {
            it.copy(
                selectedMode = mode,
                // The option does not survive a mode that upstream refuses it on.
                shortestDistance =
                it.shortestDistance && mode in (it.selectedProfile?.modesSupportingShortest ?: emptySet()),
            )
        }
    }

    fun toggleModeFilter(mode: RoutingMode) {
        mUiState.update { state ->
            val updated = if (mode in state.modeFilter) state.modeFilter - mode else state.modeFilter + mode
            state.copy(modeFilter = updated.toPersistentSet())
        }
    }

    fun setAlternatives(count: Int) {
        mUiState.update {
            it.copy(alternatives = count.coerceIn(1, it.selectedProfile?.maxAlternatives ?: 1))
        }
    }

    fun setAvoidTolls(avoid: Boolean) = mUiState.update { it.copy(avoidTolls = avoid) }

    fun setShortestDistance(shortest: Boolean) = mUiState.update { it.copy(shortestDistance = shortest) }

    fun setDeparture(date: LocalDate?, time: LocalTime?) =
        mUiState.update { it.copy(departureDate = date, departureTime = time) }

    // ---- the answer ---------------------------------------------------------------------------

    private var calculateTransportJob: Job? = null

    fun calculateTransport() {
        if (calculateTransportJob?.isActive == true) return

        val state = mUiState.value
        val selection = state.toSelection() ?: return
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
