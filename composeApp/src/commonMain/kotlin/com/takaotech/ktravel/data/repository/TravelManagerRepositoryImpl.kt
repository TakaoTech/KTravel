package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSource
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.mapper.TravelPlanMapper.toSummary
import com.takaotech.ktravel.domain.model.TravelPlanSummary
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class TravelManagerRepositoryImpl(
    private val dataSource: TravelPlanStorageDataSource
) : TravelManagerRepository {

    override suspend fun getAllTravelPlans(): List<TravelPlanSummary> {
        return dataSource.getAllTravelPlans().map { it.toSummary() }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createTravelPlan(name: String, periodStart: LocalDate, periodEnd: LocalDate) {
        val entity = TravelPlanEntity(
            id = Uuid.random().toString(),
            name = name,
            periodStart = periodStart,
            periodEnd = periodEnd,
            days = emptyList(),
            places = emptyList()
        )
        dataSource.saveTravelPlan(entity)
    }
}
