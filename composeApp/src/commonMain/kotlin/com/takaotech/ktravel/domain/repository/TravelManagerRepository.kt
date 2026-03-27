package com.takaotech.ktravel.domain.repository

import com.takaotech.ktravel.domain.model.TravelPlanSummary
import kotlinx.datetime.LocalDate

interface TravelManagerRepository {
    /**
     * Restituisce la lista di tutti i piani di viaggio salvati nel database
     */
    suspend fun getAllTravelPlans(): List<TravelPlanSummary>

    /**
     * Crea un nuovo piano di viaggio con i dati forniti
     */
    suspend fun createTravelPlan(name: String, periodStart: LocalDate, periodEnd: LocalDate): String
}
