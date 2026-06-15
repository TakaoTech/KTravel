package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSource
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.mapper.TravelPlanMapper.toSummary
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.model.TravelPlanSummary
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class TravelManagerRepositoryImpl(
    private val dataSource: TravelPlanStorageDataSource
) : TravelManagerRepository {

    override suspend fun getAllTravelPlans(): List<TravelPlanSummary> {
        return dataSource.getAllTravelPlans().map { it.toSummary() }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createTravelPlan(name: String, periodStart: LocalDate, periodEnd: LocalDate): String {
        val id = Uuid.random().toString()

        val entity = TravelPlanEntity(
            id = id,
            name = name,
            periodStart = periodStart,
            periodEnd = periodEnd,
            days = emptyList(),
            places = emptyList()
        )
        dataSource.saveTravelPlan(entity)

        return id
    }
}
