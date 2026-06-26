package com.takaotech.ktravel.presentation.planning.detail

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.core.annotation.Parcelize
import com.takaotech.ktravel.presentation.planning.PlaceUi
import com.takaotech.ktravel.presentation.planning.StepUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList

/**
 * Schermata Circuit del dettaglio di un giorno di pianificazione.
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

/** Target di navigazione emessi dal presenter; tradotti dal NavHost ospite in destinazioni Compose. */
@Parcelize
data class AddPlaceScreen(val dayId: String) : Screen

@Parcelize
data class AddTransportScreen(
    val dayId: String,
    val startPlaceId: String,
    val endPlaceId: String
) : Screen

data class PlanningDetailUiState(
    val steps: ImmutableList<StepUi>,
    val places: PersistentList<PlaceUi>,
    val eventSink: (PlanningDetailEvent) -> Unit
) : CircuitUiState

sealed interface PlanningDetailEvent : CircuitUiEvent {
    data object NavigateBack : PlanningDetailEvent
    data object AddPlace : PlanningDetailEvent

    /** Sposta un Place del backlog nella lista steps (movePlaceToStep). */
    data class MovePlaceToSteps(val placeId: String) : PlanningDetailEvent

    /** Riporta un Place del giorno nel backlog generale (movePlaceToGeneral). */
    data class MovePlaceToGeneral(val placeId: String) : PlanningDetailEvent

    /** Elimina definitivamente un Place del giorno (deletePlace). */
    data class DeletePlace(val placeId: String) : PlanningDetailEvent

    /** Rimuove uno step (la decisione Place->backlog / Transport->delete è del dominio). */
    data class StepDelete(val step: StepUi) : PlanningDetailEvent

    data class StepMoveUp(val stepId: String) : PlanningDetailEvent
    data class StepMoveDown(val stepId: String) : PlanningDetailEvent

    data class AddTransport(val startId: String, val endId: String) : PlanningDetailEvent
}
