@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.domain.model.*
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import kotlinx.coroutines.flow.*
import org.koin.core.annotation.Single
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Single
class TravelPlanRepositoryImpl : TravelPlanRepository {

    private val _planningState = MutableStateFlow(TravelPlan())
    override val planningState: StateFlow<TravelPlan> = _planningState.asStateFlow()

    init {
        val now = kotlin.time.Clock.System.now()
        _planningState.value = _planningState.value.setPeriod(start = now, end = now)
    }

    private fun TravelPlan.setPeriod(start: Instant, end: Instant): TravelPlan {
        val newDays = (start.toLocalDate()..end.toLocalDate()).map { newDate ->
            days.firstOrNull { it.date == newDate } ?: TravelDayDomain(date = newDate)
        }
        return copy(
            periodStart = start.toEpochMilliseconds(),
            periodEnd = end.toEpochMilliseconds(),
            days = newDays
        )
    }

    override fun getTravelDayFlow(dayId: String): Flow<TravelDayDomain> {
        return planningState.map { state ->
            state.days.firstOrNull { it.id == dayId } ?: TravelDayDomain.EMPTY
        }
    }

    override suspend fun updatePeriod(startMillis: Long, endMillis: Long) {
        _planningState.value = _planningState.value.setPeriod(
            start = Instant.fromEpochMilliseconds(startMillis),
            end = Instant.fromEpochMilliseconds(endMillis)
        )
    }

    override suspend fun updateStep(dayId: String, stepId: String, updatedStep: StepDomain) {
        val currentState = _planningState.value
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        val day = currentState.days[dayIndex]
        val stepIndex = day.steps.indexOfFirst { it.id == stepId }
        if (stepIndex == -1) return

        val updatedSteps = day.steps.toMutableList().also { it[stepIndex] = updatedStep }
        val updatedDay = day.copy(steps = updatedSteps)
        val updatedDays = currentState.days.toMutableList().also { it[dayIndex] = updatedDay }
        _planningState.value = currentState.copy(days = updatedDays)
    }

    override fun updatePlanName(name: String) {
        _planningState.update { it.copy(name = name) }
    }

    override suspend fun savePlace(place: PlaceDomain, dayId: String?) {
        val currentState = _planningState.value

        if (dayId != null) {
            val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
            if (dayIndex == -1) return

            val day = currentState.days[dayIndex]
            val updatedDay = day.copy(places = day.places + place)
            val updatedDays = currentState.days.toMutableList().also { it[dayIndex] = updatedDay }
            _planningState.value = currentState.copy(days = updatedDays)
        } else {
            _planningState.value = currentState.copy(places = currentState.places + place)
        }
    }

    override suspend fun movePlaceToDay(placeId: String, dayId: String) {
        val currentState = _planningState.value
        val place = currentState.places.firstOrNull { it.id == placeId } ?: return
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        val day = currentState.days[dayIndex]
        val updatedDay = day.copy(places = day.places + place)
        val updatedDays = currentState.days.toMutableList().also { it[dayIndex] = updatedDay }
        _planningState.value = currentState.copy(
            places = currentState.places.filter { it.id != placeId },
            days = updatedDays
        )
    }

    override suspend fun movePlaceToGeneral(placeId: String, dayId: String) {
        val currentState = _planningState.value
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        val day = currentState.days[dayIndex]
        val place = day.places.firstOrNull { it.id == placeId } ?: return

        val updatedDay = day.copy(places = day.places.filter { it.id != placeId })
        val updatedDays = currentState.days.toMutableList().also { it[dayIndex] = updatedDay }
        _planningState.value = currentState.copy(
            places = currentState.places + place,
            days = updatedDays
        )
    }

    override suspend fun movePlaceToStep(placeId: String, dayId: String) {
        val currentState = _planningState.value
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        val day = currentState.days[dayIndex]
        val place = day.places.firstOrNull { it.id == placeId } ?: return
        val stepPlace = StepPlaceMapper.placeToStep(place)

        val updatedDay = day.copy(
            places = day.places.filter { it.id != placeId },
            steps = day.steps + stepPlace
        )
        val updatedDays = currentState.days.toMutableList().also { it[dayIndex] = updatedDay }
        _planningState.value = currentState.copy(days = updatedDays)
    }

    override suspend fun moveStepToPlace(stepId: String, dayId: String) {
        val currentState = _planningState.value
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        val day = currentState.days[dayIndex]
        val step = day.steps.firstOrNull { it.id == stepId } as? StepDomain.Place ?: return
        val place = StepPlaceMapper.stepToPlace(step)

        val updatedDay = day.copy(
            steps = day.steps.filter { it.id != stepId },
            places = day.places + place
        )
        val updatedDays = currentState.days.toMutableList().also { it[dayIndex] = updatedDay }
        _planningState.value = currentState.copy(days = updatedDays)
    }

    override suspend fun moveTravelStepUp(stepId: String, dayId: String) {
        val currentState = _planningState.value
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        val day = currentState.days[dayIndex]
        val stepIndex = day.steps.indexOfFirst { it.id == stepId }
        if (stepIndex <= 0) return // Già in cima o non trovato

        val steps = day.steps
        val step = steps[stepIndex]
        val prevStep = steps[stepIndex - 1]

        val mutableSteps = steps.toMutableList()
        mutableSteps[stepIndex - 1] = step
        mutableSteps[stepIndex] = prevStep

        val updatedDay = day.copy(steps = mutableSteps)
        val updatedDays = currentState.days.toMutableList().also { it[dayIndex] = updatedDay }
        _planningState.value = currentState.copy(days = updatedDays)
    }

    override suspend fun moveTravelStepDown(stepId: String, dayId: String) {
        val currentState = _planningState.value
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        val day = currentState.days[dayIndex]
        val stepIndex = day.steps.indexOfFirst { it.id == stepId }
        if (stepIndex == -1 || stepIndex >= day.steps.size - 1) return // Già in fondo o non trovato

        val steps = day.steps
        val step = steps[stepIndex]
        val nextStep = steps[stepIndex + 1]

        val mutableSteps = steps.toMutableList()
        mutableSteps[stepIndex + 1] = step
        mutableSteps[stepIndex] = nextStep

        val updatedDay = day.copy(steps = mutableSteps)
        val updatedDays = currentState.days.toMutableList().also { it[dayIndex] = updatedDay }
        _planningState.value = currentState.copy(days = updatedDays)
    }

    override suspend fun addTransportStep(dayId: String, afterStepId: String, step: StepDomain) {
        val currentState = _planningState.value
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        val day = currentState.days[dayIndex]
        val afterIndex = day.steps.indexOfFirst { it.id == afterStepId }
        if (afterIndex == -1) return

        val updatedSteps = day.steps.toMutableList().also { it.add(afterIndex + 1, step) }
        val updatedDay = day.copy(steps = updatedSteps)
        val updatedDays = currentState.days.toMutableList().also { it[dayIndex] = updatedDay }
        _planningState.value = currentState.copy(days = updatedDays)
    }

    override suspend fun deletePlace(placeId: String, dayId: String?) {
        val currentState = _planningState.value

        if (dayId != null) {
            val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
            if (dayIndex == -1) return

            val day = currentState.days[dayIndex]
            val updatedDay = day.copy(places = day.places.filter { it.id != placeId })
            val updatedDays = currentState.days.toMutableList().also { it[dayIndex] = updatedDay }
            _planningState.value = currentState.copy(days = updatedDays)
        } else {
            _planningState.value = currentState.copy(
                places = currentState.places.filter { it.id != placeId }
            )
        }
    }
}
