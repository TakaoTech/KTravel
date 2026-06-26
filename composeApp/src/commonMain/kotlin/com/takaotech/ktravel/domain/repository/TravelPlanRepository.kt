package com.takaotech.ktravel.domain.repository

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OpenForMokkery
interface TravelPlanRepository {
    /**
     * Stato completo del piano di viaggio
     */
    val planningState: StateFlow<TravelPlanDomain>

    /**
     * Ottiene un Flow per un giorno specifico
     */
    fun getTravelDayFlow(dayId: String): Flow<TravelDayDomain>

    //region Update

    /**
     * Aggiorna il nome del piano di viaggio
     */
    suspend fun updatePlanName(name: String)

    /**
     * Aggiorna il periodo del viaggio
     */
    suspend fun updatePeriod(startMillis: Long, endMillis: Long)

    /**
     * Aggiorna uno step esistente
     */
    suspend fun updateStep(dayId: String, stepId: String, updatedStep: StepDomain)


    //endregion Update

    //region Move
    /**
     * Sposta un Place dalla lista generale a un TravelDay
     */
    suspend fun movePlaceToDay(placeId: String, dayId: String)

    /**
     * Sposta un Place da un TravelDay alla lista generale
     */
    suspend fun movePlaceToGeneral(placeId: String, dayId: String)


    /**
     * Sposta un Place (presente nella lista places del giorno) nella lista steps
     * dello stesso giorno convertendolo in Step.Place
     */
    suspend fun movePlaceToStep(placeId: String, dayId: String)

    /**
     * Sposta uno Step.Place (presente nella lista steps del giorno) nella lista places
     * dello stesso giorno convertendolo in Place
     */
    suspend fun moveStepToPlace(stepId: String, dayId: String)

    /**
     * Sposta uno step verso l'alto nella lista
     */
    suspend fun moveTravelStepUp(stepId: String, dayId: String)

    /**
     * Sposta uno step verso il basso nella lista
     */
    suspend fun moveTravelStepDown(stepId: String, dayId: String)

    //endregion Move

    /**
     * Salva un nuovo Place
     * @param place il Place da salvare
     * @param dayId se impostato, aggiunge il Place direttamente al TravelDay con questo id
     */
    suspend fun savePlace(place: PlaceDomain, dayId: String? = null)

    /**
     * Inserisce uno step nella posizione successiva a un altro step
     */
    suspend fun addTransportStep(dayId: String, afterStepId: String, step: StepDomain)

    /**
     * Elimina un Place dal piano di viaggio
     */
    suspend fun deletePlace(placeId: String, dayId: String? = null)

    /**
     * Elimina uno step dalla lista steps del giorno
     */
    suspend fun deleteStep(stepId: String, dayId: String)

    /**
     * Rimuove uno step decidendo in base al tipo: uno Step.Place torna nel backlog (places),
     * uno Step.Transport viene eliminato. La decisione è una regola di dominio (non della UI).
     */
    suspend fun removeStep(stepId: String, dayId: String)
}
