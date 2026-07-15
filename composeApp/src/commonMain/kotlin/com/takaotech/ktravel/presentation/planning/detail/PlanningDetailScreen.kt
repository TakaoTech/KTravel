package com.takaotech.ktravel.presentation.planning.detail

import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.core.annotation.Parcelize

/**
 * Schermata Circuit del dettaglio di un giorno di pianificazione: coordina i due pannelli figli
 * [StepsPaneScreen] (itinerario) e [PlacesBacklogScreen] (backlog posti), montati come
 * `CircuitContent` annidati dal layout adattivo della pagina.
 *
 * `Screen` è `Parcelable` su Android: l'annotazione comune [Parcelize] (riconosciuta dal plugin
 * kotlin-parcelize via `additionalAnnotation`) genera l'implementazione Parcelable solo su Android;
 * su jvm/native è un marker ignorato.
 *
 * Il [travelId] determina il [com.takaotech.ktravel.di.PlanningGraph] da cui risolvere il
 * repository (vincolo V3); il [dayId] individua il giorno mostrato.
 */
@Parcelize
data class PlanningDetailScreen(val travelId: String, val dayId: String) : Screen

/** Target di navigazione emessi dai presenter; tradotti dal NavHost ospite in destinazioni Compose. */
@Parcelize
data class AddPlaceScreen(val dayId: String) : Screen

@Parcelize
data class AddTransportScreen(
    val dayId: String,
    val startPlaceId: String,
    val endPlaceId: String
) : Screen

data class PlanningDetailUiState(
    val stepsPaneScreen: StepsPaneScreen,
    val placesBacklogScreen: PlacesBacklogScreen,
    val eventSink: (PlanningDetailEvent) -> Unit
) : CircuitUiState

sealed interface PlanningDetailEvent : CircuitUiEvent {
    /**
     * [NavEvent] emerso da un `CircuitContent` figlio e non gestito dal layout della pagina
     * (pane switching): il presenter lo inoltra al proprio [com.slack.circuit.runtime.Navigator],
     * raggiungendo la traduzione verso il NavController nell'host.
     */
    data class ChildNav(val navEvent: NavEvent) : PlanningDetailEvent
}
