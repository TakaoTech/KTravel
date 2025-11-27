package com.takaotech.ktravel.domain.repository

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
     * Aggiunge uno step a un giorno specifico
     */
    suspend fun addStepToDay(dayId: String, step: TravelDay.Step)

    /**
     * Rimuove uno step da un giorno
     */
    suspend fun removeStepFromDay(dayId: String, stepId: String)

    /**
     * Aggiorna uno step esistente
     */
    suspend fun updateStep(dayId: String, stepId: String, updatedStep: TravelDay.Step)
}
