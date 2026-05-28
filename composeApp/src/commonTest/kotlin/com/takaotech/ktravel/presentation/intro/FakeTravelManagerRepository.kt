package com.takaotech.ktravel.presentation.intro

import com.takaotech.ktravel.domain.model.TravelPlanSummary
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

class FakeTravelManagerRepository : TravelManagerRepository {
    private val _savedPlans = MutableStateFlow<List<TravelPlanSummary>>(emptyList())
    override suspend fun getAllTravelPlans() = _savedPlans.value.toList()

    private var lastCreatedId: String? = null
    private var lastCallName: String? = null
    private var lastCallStart: LocalDate? = null
    private var lastCallEnd: LocalDate? = null

    override suspend fun createTravelPlan(
        name: String,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): String {
        lastCallName = name
        lastCallStart = periodStart
        lastCallEnd = periodEnd
        val id = "travel-${Clock.System.now()}"
        lastCreatedId = id

//        _savedPlans.update { current ->
//            current + TravelPlanSummary(id, name, periodEnd.minus(periodStart).days.toInt())
//        }

        return id
    }

    fun getLastCallName(): String? = lastCallName
    fun getLastCallStart(): LocalDate? = lastCallStart
    fun getLastCallEnd(): LocalDate? = lastCallEnd
    fun getLastCreatedId(): String? = lastCreatedId
}