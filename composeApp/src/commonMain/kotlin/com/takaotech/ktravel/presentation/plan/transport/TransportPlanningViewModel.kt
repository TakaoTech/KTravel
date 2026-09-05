package com.takaotech.ktravel.presentation.plan.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TravelPlanEditor.transportAfter
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RouteTimeChoice
import com.takaotech.ktravel.domain.routing.RoutingCatalog
import com.takaotech.ktravel.domain.routing.RoutingFailure
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.presentation.plan.TravelPlanUiMapper
import com.takaotech.ktravel.presentation.plan.transport.options.routeOptionsScreen
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

@AssistedInject
class TransportPlanningViewModel(
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
        ): TransportPlanningViewModel
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

    /** The answer this screen computes, read by the route preview from the plan's graph. */
    private val routeAnswerDraft get() = planningGraph.routeAnswerDraft

    private val mUiState = MutableStateFlow(TransportPlanningUiState())
    val uiState = mUiState.asStateFlow()

    /**
     * The request the transport already on this pair of places was computed with, waiting for a
     * catalog to say whether its profile can still be used.
     *
     * A field and not part of the state: it is consumed once, by the first catalog that arrives,
     * and nothing draws it. Read before the catalog is asked for rather than beside it, so the
     * order of the two answers is not a race — and declared before [init] rather than beside its
     * only other use, because a property initializer runs *after* the init block and would put the
     * null back.
     */
    private var pendingRequest: RouteSelection? = null

    init {
        viewModelScope.launch {
            planningGraph.travelPlanRepository
                .getTravelDayFlow(dayId).collect { day ->
                    val startStep = day.steps.first { step -> step.id == startPlaceId }
                    val endStep = day.steps.first { step -> step.id == endPlaceId }

                    val startUi = (startStep as? StepDomain.Place)?.let {
                        with(TravelPlanUiMapper) { it.toUiStepPlace() }
                    }
                    val endUi = (endStep as? StepDomain.Place)?.let {
                        with(TravelPlanUiMapper) { it.toUiStepPlace() }
                    }

                    mUiState.update {
                        it.copy(
                            startPlace = startUi,
                            endPlace = endUi,
                            dayDate = day.date,
                            departureSuggestion = departureSuggestion(startUi),
                            arrivalSuggestion = arrivalSuggestion(endUi),
                        )
                    }
                }
        }

        viewModelScope.launch {
            routeOptionsDraft.selection.collect { selection ->
                mUiState.update { state ->
                    state.copy(isRequestReady = selection != null && selection.profileId == state.selectedProfileId)
                }
            }
        }

        val preferred = navigatorTargets.defaultKind()
        val remoteConfigured = navigatorTargets.isRemoteConfigured()
        val kind = if (preferred == NavigatorKind.REMOTE && !remoteConfigured) NavigatorKind.EMBEDDED else preferred

        mUiState.update { it.copy(navigatorKind = kind, isRemoteConfigured = remoteConfigured) }

        viewModelScope.launch {
            pendingRequest = planningGraph.travelPlanRepository
                .getTravelDayFlow(dayId).first().steps.transportAfter(startPlaceId)?.request

            loadCatalog(kind)
        }
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
        restore(catalog)?.let { request ->
            selectProfile(request.profileId)
            routeOptionsDraft.update(request)
            return
        }

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

    /**
     * What was asked for last time, when this catalog can still serve it.
     *
     * Consumed on the first answer whether or not it is used, so that switching navigator or
     * retrying an unreachable one never restores over choices the traveller has since made. A
     * profile this navigator does not serve simply falls through to the usual fallback.
     *
     * The draft it is written into is the plan's, not this pair of places': it may still hold what
     * was configured for another transport, which is exactly why the restore overwrites it instead
     * of deferring to it.
     */
    private fun restore(catalog: RoutingCatalog): RouteSelection? {
        val request = pendingRequest ?: return null
        pendingRequest = null

        return request.takeIf {
            catalog.options.any { option -> option.profile.id == it.profileId && option.isSelectable }
        }
    }

    // ---- when to travel -------------------------------------------------------------------------

    /**
     * Switches between leaving now, leaving at an hour and arriving by one.
     *
     * The hour arrives *with* the switch rather than after it: the mode the traveller picked is
     * seeded from the adjacent stop, and from the clock when that stop has no schedule. The screen
     * therefore never holds a mode that is waiting for an hour, and Calculate has nothing extra to
     * guard against.
     */
    fun setTimeMode(mode: RouteTimeMode) {
        val fallback = Clock.System.nowRoundedUp(TimeZone.currentSystemDefault())

        mUiState.update { state ->
            val choice = when (mode) {
                RouteTimeMode.NOW -> RouteTimeChoice.Now
                RouteTimeMode.DEPART_AT -> RouteTimeChoice.DepartAt(state.departureSuggestion ?: fallback)
                RouteTimeMode.ARRIVE_BY -> RouteTimeChoice.ArriveBy(state.arrivalSuggestion ?: fallback)
            }

            state.copy(timeChoice = choice)
        }
    }

    /** Replaces the hour, leaving the traveller on the mode they are already on. */
    fun setTime(time: LocalTime) = mUiState.update { state ->
        val choice = when (state.timeChoice) {
            RouteTimeChoice.Now -> return@update state
            is RouteTimeChoice.DepartAt -> RouteTimeChoice.DepartAt(time)
            is RouteTimeChoice.ArriveBy -> RouteTimeChoice.ArriveBy(time)
        }

        state.copy(timeChoice = choice)
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
        routeOptionsDraft.clear()

        mUiState.update { state ->
            val profile = state.catalog?.options?.firstOrNull { it.profile.id == profileId }?.profile

            state.copy(
                selectedProfileId = profileId,
                routeOptionsScreen = profile?.routeOptionsScreen(travelId),
                timeChoice = state.timeChoice.takeUnless {
                    it is RouteTimeChoice.ArriveBy && profile?.supportsArriveBy != true
                } ?: RouteTimeChoice.Now,
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
        val dayDate = state.dayDate ?: return

        calculateTransportJob = viewModelScope.launch(Dispatchers.Default) {
            mUiState.update { it.copy(isLoading = true, failure = null) }
            routeAnswerDraft.clear()

            val result = try {
                routingService.routes(
                    kind = state.navigatorKind,
                    origin = "${start.lat},${start.lng}",
                    destination = "${end.lat},${end.lng}",
                    selection = selection,
                    time = state.timeChoice,
                    dayDate = dayDate,
                )
            } catch (failure: RoutingFailure) {
                mUiState.update { it.copy(isLoading = false, failure = failure.toReason()) }
                return@launch
            }

            routeAnswerDraft.publish(result = result, request = selection)
            mUiState.update { it.copy(isLoading = false, previewRequested = true) }
        }
    }

    fun dismissFailure() = mUiState.update { it.copy(failure = null) }

    /**
     * Acknowledges that the host has opened the preview, so returning to this screen does not
     * reopen it.
     */
    fun onPreviewOpened() = mUiState.update { it.copy(previewRequested = false) }
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
