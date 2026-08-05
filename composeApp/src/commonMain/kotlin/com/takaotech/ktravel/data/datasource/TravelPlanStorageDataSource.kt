package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.data.entity.TravelPlanEntity

interface TravelPlanStorageDataSource {
    suspend fun saveTravelPlan(entity: TravelPlanEntity)

    /**
     * Salva [entity] **attendendo** il completamento della scrittura, a differenza di
     * [saveTravelPlan] che è fire-and-forget. Serve quando il chiamante deve poter rileggere subito
     * il documento, come dopo l'import di un archivio.
     */
    suspend fun insertTravelPlan(entity: TravelPlanEntity)

    fun getTravelPlan(id: String): TravelPlanEntity

    /**
     * Nome del piano con questo [id], o null se non esiste. A differenza di [getTravelPlan] non
     * lancia sui documenti assenti, quindi è usabile per rilevare i conflitti di id.
     */
    suspend fun getTravelPlanNameOrNull(id: String): String?

    suspend fun getAllTravelPlans(): List<TravelPlanEntity>
    suspend fun deleteTravelPlan(id: String)
}
