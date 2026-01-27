package com.takaotech.ktravel.domain.repository

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.presentation.planner.Place
import com.takaotech.ktravel.presentation.planner.PlanningUiState
import com.takaotech.ktravel.presentation.planner.TravelDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TravelPlanRepository {
    /**
     * Stato completo del piano di viaggio
     */
    val planningState: StateFlow<PlanningUiState>

    /**
     * Ottiene un Flow per un giorno specifico
     */
    fun getTravelDayFlow(dayId: String): Flow<TravelDay?>

    /**
     * Aggiorna il periodo del viaggio
     */
    suspend fun updatePeriod(startMillis: Long, endMillis: Long)
    /**
     * Rimuove uno step da un giorno
     */
    suspend fun removeStepFromDay(dayId: String, stepId: String)

    /**
     * Aggiorna uno step esistente
     */
    suspend fun updateStep(dayId: String, stepId: String, updatedStep: TravelDay.Step)

    /**
     * Aggiorna il nome del piano di viaggio
     */
    fun updatePlanName(name: TextFieldValue)

    /**
     * Salva un nuovo Place
     */
    suspend fun savePlace(place: Place)

    /**
     * Sposta un Place dalla lista generale a un TravelDay
     */
    suspend fun movePlaceToDay(placeId: String, dayId: String)
}
