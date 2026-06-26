package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.domain.model.TravelPlanEditor.deleteStep
import com.takaotech.ktravel.domain.model.TravelPlanEditor.moveStepToPlace


/**
 * Operazioni di mutazione **pure** su [TravelPlanDomain].
 *
 * Concentra il pattern "trova giorno -> trasforma -> ricostruisci" così che le regole siano
 * testabili in isolamento (input -> output) senza StateFlow né datasource. Il repository si limita
 * ad applicarle e a persistere lo stato. Tutte le operazioni sono totali: se un id non esiste,
 * restituiscono il piano invariato senza lanciare eccezioni.
 */
object TravelPlanEditor {

    private fun TravelPlanDomain.updateDay(
        dayId: String,
        transform: (TravelDayDomain) -> TravelDayDomain
    ): TravelPlanDomain {
        val index = days.indexOfFirst { it.id == dayId }
        if (index == -1) return this
        val updatedDays = days.toMutableList().also { it[index] = transform(it[index]) }
        return copy(days = updatedDays)
    }

    fun TravelPlanDomain.savePlace(place: PlaceDomain, dayId: String?): TravelPlanDomain =
        if (dayId == null) {
            copy(places = places + place)
        } else {
            updateDay(dayId) { it.copy(places = it.places + place) }
        }

    fun TravelPlanDomain.updateStep(
        dayId: String,
        stepId: String,
        updatedStep: StepDomain
    ): TravelPlanDomain = updateDay(dayId) { day ->
        val stepIndex = day.steps.indexOfFirst { it.id == stepId }
        if (stepIndex == -1) day
        else day.copy(steps = day.steps.toMutableList().also { it[stepIndex] = updatedStep })
    }

    fun TravelPlanDomain.movePlaceToDay(placeId: String, dayId: String): TravelPlanDomain {
        val place = places.firstOrNull { it.id == placeId } ?: return this
        if (days.none { it.id == dayId }) return this
        return copy(places = places.filter { it.id != placeId })
            .updateDay(dayId) { it.copy(places = it.places + place) }
    }

    fun TravelPlanDomain.movePlaceToGeneral(placeId: String, dayId: String): TravelPlanDomain {
        val dayIndex = days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return this
        val place = days[dayIndex].places.firstOrNull { it.id == placeId } ?: return this
        return copy(places = places + place)
            .updateDay(dayId) { it.copy(places = it.places.filter { p -> p.id != placeId }) }
    }

    /** Sposta un Place del giorno nella lista steps convertendolo in [StepDomain.Place]. */
    fun TravelPlanDomain.movePlaceToStep(placeId: String, dayId: String): TravelPlanDomain =
        updateDay(dayId) { day ->
            val place = day.places.firstOrNull { it.id == placeId } ?: return@updateDay day
            day.copy(
                places = day.places.filter { it.id != placeId },
                steps = day.steps + StepPlaceMapper.placeToStep(place)
            )
        }

    /** Riporta uno [StepDomain.Place] nella lista places del giorno (V1: scarta lo schedule). */
    fun TravelPlanDomain.moveStepToPlace(stepId: String, dayId: String): TravelPlanDomain =
        updateDay(dayId) { day ->
            val step = day.steps.firstOrNull { it.id == stepId } as? StepDomain.Place
                ?: return@updateDay day
            day.copy(
                steps = day.steps.filter { it.id != stepId },
                places = day.places + StepPlaceMapper.stepToPlace(step)
            )
        }

    fun TravelPlanDomain.moveStepUp(stepId: String, dayId: String): TravelPlanDomain =
        updateDay(dayId) { day ->
            val index = day.steps.indexOfFirst { it.id == stepId }
            if (index <= 0) day
            else day.copy(steps = day.steps.toMutableList().also {
                it[index - 1] = day.steps[index]
                it[index] = day.steps[index - 1]
            })
        }

    fun TravelPlanDomain.moveStepDown(stepId: String, dayId: String): TravelPlanDomain =
        updateDay(dayId) { day ->
            val index = day.steps.indexOfFirst { it.id == stepId }
            if (index == -1 || index >= day.steps.size - 1) day
            else day.copy(steps = day.steps.toMutableList().also {
                it[index + 1] = day.steps[index]
                it[index] = day.steps[index + 1]
            })
        }

    fun TravelPlanDomain.addTransportStep(
        dayId: String,
        afterStepId: String,
        step: StepDomain
    ): TravelPlanDomain = updateDay(dayId) { day ->
        val afterIndex = day.steps.indexOfFirst { it.id == afterStepId }
        if (afterIndex == -1) day
        else day.copy(steps = day.steps.toMutableList().also { it.add(afterIndex + 1, step) })
    }

    fun TravelPlanDomain.deleteStep(stepId: String, dayId: String): TravelPlanDomain =
        updateDay(dayId) { it.copy(steps = it.steps.filter { s -> s.id != stepId }) }

    fun TravelPlanDomain.deletePlace(placeId: String, dayId: String?): TravelPlanDomain =
        if (dayId == null) {
            copy(places = places.filter { it.id != placeId })
        } else {
            updateDay(dayId) { it.copy(places = it.places.filter { p -> p.id != placeId }) }
        }

    /**
     * Rimuove uno step dall'itinerario decidendo in base al tipo (regola di dominio, prima nella UI):
     * un [StepDomain.Place] torna nel backlog (via [moveStepToPlace]); un [StepDomain.Transport]
     * viene eliminato (via [deleteStep]).
     */
    fun TravelPlanDomain.removeStep(stepId: String, dayId: String): TravelPlanDomain {
        val dayIndex = days.indexOfFirst { it.id == dayId }
        if (dayIndex == -1) return this
        val step = days[dayIndex].steps.firstOrNull { it.id == stepId } ?: return this
        return when (step) {
            is StepDomain.Place -> moveStepToPlace(stepId, dayId)
            is StepDomain.Transport -> deleteStep(stepId, dayId)
        }
    }
}
