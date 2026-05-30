package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.data.entity.TravelPlanEntity

interface TravelPlanStorageDataSource {
    suspend fun saveTravelPlan(entity: TravelPlanEntity)
    fun getTravelPlan(id: String): TravelPlanEntity
    suspend fun getAllTravelPlans(): List<TravelPlanEntity>
    fun deleteTravelPlan(id: String)
}
