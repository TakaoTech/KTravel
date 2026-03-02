package com.takaotech.ktravel.domain.repository

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.presentation.planning.Place
import com.takaotech.ktravel.presentation.planning.PlanningUiState
import com.takaotech.ktravel.presentation.planning.StepPlaceMapper
import com.takaotech.ktravel.presentation.planning.TravelDay
import kotlinx.coroutines.flow.*
import org.koin.core.annotation.Single
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Single
class TravelPlanRepositoryImpl : TravelPlanRepository {

    private val _planningState = MutableStateFlow(PlanningUiState())
    override val planningState: StateFlow<PlanningUiState> = _planningState.asStateFlow()

    init {
        // Inizializza con il periodo di default
        with(_planningState.value) {
            val start = planHeader.period.start.toEpochMilliseconds()
            val end = planHeader.period.end.toEpochMilliseconds()
            _planningState.value = setPeriod(
                start = Instant.fromEpochMilliseconds(start),
                end = Instant.fromEpochMilliseconds(end)
            )
        }
    }

    override fun getTravelDayFlow(dayId: String): Flow<TravelDay> {
        return planningState.map { state ->
            state.days.firstOrNull { it.id == dayId } ?: TravelDay.EMPTY
        }
    }

    override suspend fun updatePeriod(startMillis: Long, endMillis: Long) {
        _planningState.value = _planningState.value.setPeriod(
            start = Instant.fromEpochMilliseconds(startMillis),
            end = Instant.fromEpochMilliseconds(endMillis)
        )
    }

    override suspend fun updateStep(dayId: String, stepId: String, updatedStep: TravelDay.Step) {
        val currentState = _planningState.value
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }

        if (dayIndex == -1) return

        val day = currentState.days[dayIndex]
        val stepIndex = day.steps.indexOfFirst { it.id == stepId }

        if (stepIndex == -1) return

        val updatedDay = day.copy(
            steps = day.steps.set(stepIndex, updatedStep)
        )

        _planningState.value = currentState.copy(
            days = currentState.days.set(dayIndex, updatedDay)
        )
    }

    override fun updatePlanName(name: TextFieldValue) {
        val currentState = _planningState.value
        _planningState.value = currentState.copy(
            planHeader = currentState.planHeader.copy(name = name)
        )
    }

    override suspend fun savePlace(place: Place, dayId: String?) {
        val currentState = _planningState.value

        if (dayId != null) {
            // Trova l'indice del giorno
            val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
            if (dayIndex == -1) {
                // TODO Log this?
                return
            }

            // Aggiungi il Place al TravelDay
            val day = currentState.days[dayIndex]
            val updatedDay = day.copy(
                places = day.places.add(place)
            )

            _planningState.value = currentState.copy(
                days = currentState.days.set(dayIndex, updatedDay)
            )
        } else {
            // Aggiungi alla lista generale
            _planningState.value = currentState.copy(
                places = currentState.places.add(place)
            )
        }
    }

    override suspend fun movePlaceToDay(placeId: String, dayId: String) {
        val currentState = _planningState.value

        // Trova il Place nella lista generale
        val place = currentState.places.firstOrNull { it.id == placeId } ?: return

        // Trova l'indice del giorno
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        // Rimuovi il Place dalla lista generale
        val updatedPlaces = currentState.places.remove(place)

        // Aggiungi il Place al TravelDay
        val day = currentState.days[dayIndex]
        val updatedDay = day.copy(
            places = day.places.add(place)
        )

        // Aggiorna lo stato
        _planningState.value = currentState.copy(
            places = updatedPlaces,
            days = currentState.days.set(dayIndex, updatedDay)
        )
    }

    override suspend fun movePlaceToGeneral(placeId: String, dayId: String) {
        val currentState = _planningState.value

        // Trova l'indice del giorno
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        // Trova il Place nel giorno specificato
        val day = currentState.days[dayIndex]
        val place = day.places.firstOrNull { it.id == placeId } ?: return

        // Rimuovi il Place dal giorno
        val updatedDay = day.copy(
            places = day.places.remove(place)
        )

        // Aggiungi il Place alla lista generale
        val updatedPlaces = currentState.places.add(place)

        // Aggiorna lo stato
        _planningState.value = currentState.copy(
            places = updatedPlaces,
            days = currentState.days.set(dayIndex, updatedDay)
        )
    }

    override suspend fun movePlaceToStep(placeId: String, dayId: String) {
        val currentState = _planningState.value

        // Trova l'indice del giorno
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        // Trova il Place nel giorno specificato
        val day = currentState.days[dayIndex]
        val place = day.places.firstOrNull { it.id == placeId } ?: return

        // Converte il Place in Step.Place
        val stepPlace = StepPlaceMapper.placeToStep(place)

        // Rimuove il Place dalla lista places e aggiunge lo Step nella lista steps
        val updatedDay = day.copy(
            places = day.places.remove(place),
            steps = day.steps.add(stepPlace)
        )

        // Aggiorna lo stato
        _planningState.value = currentState.copy(
            days = currentState.days.set(dayIndex, updatedDay)
        )
    }

    override suspend fun moveStepToPlace(stepId: String, dayId: String) {
        val currentState = _planningState.value

        // Trova l'indice del giorno
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        // Trova lo Step.Place nel giorno specificato
        val day = currentState.days[dayIndex]
        val step = day.steps.firstOrNull { it.id == stepId } as? TravelDay.Step.Place ?: return

        // Converte lo Step.Place in Place
        val place = StepPlaceMapper.stepToPlace(step)

        // Rimuove lo Step dalla lista steps e aggiunge il Place nella lista places
        val updatedDay = day.copy(
            steps = day.steps.remove(step),
            places = day.places.add(place)
        )

        // Aggiorna lo stato
        _planningState.value = currentState.copy(
            days = currentState.days.set(dayIndex, updatedDay)
        )
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

        val updatedSteps = steps
            .set(stepIndex - 1, step)
            .set(stepIndex, prevStep)

        val updatedDay = day.copy(steps = updatedSteps)
        _planningState.value = currentState.copy(
            days = currentState.days.set(dayIndex, updatedDay)
        )
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

        val updatedSteps = steps
            .set(stepIndex + 1, step)
            .set(stepIndex, nextStep)

        val updatedDay = day.copy(steps = updatedSteps)
        _planningState.value = currentState.copy(
            days = currentState.days.set(dayIndex, updatedDay)
        )
    }

    override suspend fun addTransportStep(dayId: String, afterStepId: String, step: TravelDay.Step) {
        val currentState = _planningState.value
        val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return

        val day = currentState.days[dayIndex]
        val afterIndex = day.steps.indexOfFirst { it.id == afterStepId }
        if (afterIndex == -1) return

        val updatedSteps = day.steps.add(afterIndex + 1, step)
        val updatedDay = day.copy(steps = updatedSteps)
        _planningState.value = currentState.copy(
            days = currentState.days.set(dayIndex, updatedDay)
        )
    }

    override suspend fun deletePlace(placeId: String, dayId: String?) {
        val currentState = _planningState.value

        if (dayId != null) {
            // Trova l'indice del giorno
            val dayIndex = currentState.days.indexOfFirst { it.id == dayId }
            if (dayIndex == -1) return

            // Trova il Place nel giorno specificato
            val day = currentState.days[dayIndex]
            val place = day.places.firstOrNull { it.id == placeId } ?: return

            // Rimuovi il Place dal giorno
            val updatedDay = day.copy(
                places = day.places.remove(place)
            )

            // Aggiorna lo stato
            _planningState.value = currentState.copy(
                days = currentState.days.set(dayIndex, updatedDay)
            )
        } else {
            // Rimuovi dalla lista generale
            val place = currentState.places.firstOrNull { it.id == placeId } ?: return

            _planningState.value = currentState.copy(
                places = currentState.places.remove(place)
            )
        }
    }
}
