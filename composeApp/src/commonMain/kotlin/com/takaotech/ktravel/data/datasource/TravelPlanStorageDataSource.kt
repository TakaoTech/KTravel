package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.data.entity.TravelPlanEntity

interface TravelPlanStorageDataSource {
    fun saveTravelPlan(entity: TravelPlanEntity)
    fun getTravelPlan(id: String): TravelPlanEntity
    fun getAllTravelPlans(): List<TravelPlanEntity>
    fun deleteTravelPlan(id: String)
}
