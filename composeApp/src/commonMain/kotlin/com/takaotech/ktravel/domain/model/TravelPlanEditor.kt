package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.domain.model.TravelPlanEditor.deleteStep
import com.takaotech.ktravel.domain.model.TravelPlanEditor.moveStepToPlace
import com.takaotech.ktravel.domain.model.TravelPlanEditor.updatePlaceStartTime
import kotlinx.datetime.LocalTime


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

    /**
     * Aggiorna le note (Markdown) di uno [StepDomain.Place]. Operazione totale: se il giorno o lo
     * step non esistono, o lo step non è un Place, restituisce il piano invariato.
     */
    fun TravelPlanDomain.updatePlaceNote(
        dayId: String,
        stepId: String,
        note: String
    ): TravelPlanDomain = updateDay(dayId) { day ->
        val stepIndex = day.steps.indexOfFirst { it.id == stepId }
        val step = day.steps.getOrNull(stepIndex) as? StepDomain.Place ?: return@updateDay day
        day.copy(steps = day.steps.toMutableList().also { it[stepIndex] = step.copy(note = note) })
    }

    /**
     * Imposta l'orario di inizio di uno [StepDomain.Place], creando lo [VisitScheduleDomain] se
     * assente. Operazione totale: se il giorno o lo step non esistono, o lo step non è un Place,
     * restituisce il piano invariato.
     */
    fun TravelPlanDomain.updatePlaceStartTime(
        dayId: String,
        stepId: String,
        time: LocalTime
    ): TravelPlanDomain = updatePlaceSchedule(dayId, stepId) { it.copy(startTime = time) }

    /**
     * Imposta l'orario di fine di uno [StepDomain.Place], creando lo [VisitScheduleDomain] se
     * assente. Operazione totale (vedi [updatePlaceStartTime]).
     */
    fun TravelPlanDomain.updatePlaceEndTime(
        dayId: String,
        stepId: String,
        time: LocalTime
    ): TravelPlanDomain = updatePlaceSchedule(dayId, stepId) { it.copy(endTime = time) }

    private fun TravelPlanDomain.updatePlaceSchedule(
        dayId: String,
        stepId: String,
        transform: (VisitScheduleDomain) -> VisitScheduleDomain
    ): TravelPlanDomain = updateDay(dayId) { day ->
        val stepIndex = day.steps.indexOfFirst { it.id == stepId }
        val step = day.steps.getOrNull(stepIndex) as? StepDomain.Place ?: return@updateDay day
        val schedule = transform(step.schedule ?: VisitScheduleDomain())
        day.copy(
            steps = day.steps.toMutableList()
                .also { it[stepIndex] = step.copy(schedule = schedule) }
        )
    }

    /**
     * Index of the final destination place within a day's steps: the last [StepDomain.Place] when
     * the day has at least two places, otherwise -1. Mirrors the timeline "destination" marker in
     * the UI: a lone place is not a destination and may keep its schedule.
     */
    fun List<StepDomain>.finalDestinationIndex(): Int {
        val placeCount = count { it is StepDomain.Place }
        return if (placeCount >= 2) indexOfLast { it is StepDomain.Place } else -1
    }

    /**
     * Enforces the rule that the final destination place carries no visit schedule: clears the
     * schedule of the destination place of every day when present. The destination is the arrival
     * point of the journey, so an arrival/departure time on it is meaningless and is dropped.
     */
    fun TravelPlanDomain.clearFinalDestinationSchedules(): TravelPlanDomain =
        copy(days = days.map { it.clearFinalDestinationSchedule() })

    private fun TravelDayDomain.clearFinalDestinationSchedule(): TravelDayDomain {
        val index = steps.finalDestinationIndex()
        val destination = steps.getOrNull(index) as? StepDomain.Place ?: return this
        if (destination.schedule == null) return this
        return copy(
            steps = steps.toMutableList().also { it[index] = destination.copy(schedule = null) }
        )
    }

    /**
     * Aggiunge un file all'inventario di uno [StepDomain.Place]. Operazione totale: se il giorno o lo
     * step non esistono, o lo step non è un Place, restituisce il piano invariato.
     */
    fun TravelPlanDomain.addPlaceAttachment(
        dayId: String,
        stepId: String,
        attachment: AttachmentDomain
    ): TravelPlanDomain =
        updatePlaceStep(dayId, stepId) { it.copy(attachments = it.attachments + attachment) }

    /**
     * Rimuove un file dall'inventario di uno [StepDomain.Place] per id. Operazione totale: se non
     * trova giorno/step/allegato, restituisce il piano invariato.
     */
    fun TravelPlanDomain.removePlaceAttachment(
        dayId: String,
        stepId: String,
        attachmentId: String
    ): TravelPlanDomain = updatePlaceStep(dayId, stepId) {
        it.copy(attachments = it.attachments.filter { a -> a.id != attachmentId })
    }

    /** Applica [transform] allo [StepDomain.Place] indicato, se esiste; altrimenti no-op. */
    private fun TravelPlanDomain.updatePlaceStep(
        dayId: String,
        stepId: String,
        transform: (StepDomain.Place) -> StepDomain.Place
    ): TravelPlanDomain = updateDay(dayId) { day ->
        val stepIndex = day.steps.indexOfFirst { it.id == stepId }
        val step = day.steps.getOrNull(stepIndex) as? StepDomain.Place ?: return@updateDay day
        day.copy(steps = day.steps.toMutableList().also { it[stepIndex] = transform(step) })
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
