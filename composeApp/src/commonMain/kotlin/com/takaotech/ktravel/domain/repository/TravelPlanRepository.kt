package com.takaotech.ktravel.domain.repository

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import com.takaotech.ktravel.domain.model.TravelSettingsDomain
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalTime

@OpenForMokkery
interface TravelPlanRepository {
    /**
     * Full state of the travel plan
     */
    val planningState: StateFlow<TravelPlanDomain>

    /**
     * Gets a Flow for a specific day
     */
    fun getTravelDayFlow(dayId: String): Flow<TravelDayDomain>

    //region Update

    /**
     * Updates the name of the travel plan
     */
    suspend fun updatePlanName(name: String)

    /**
     * Updates the period of the trip
     */
    suspend fun updatePeriod(startMillis: Long, endMillis: Long)

    /**
     * Replaces the preferences of this plan.
     *
     * This repository owns the plan document, so it is also the one that writes the settings; the
     * API meant for callers is [com.takaotech.ktravel.domain.repository.SettingsRepository], which
     * goes through here.
     */
    suspend fun updateSettings(settings: TravelSettingsDomain)

    /**
     * Updates an existing step
     */
    suspend fun updateStep(dayId: String, stepId: String, updatedStep: StepDomain)

    /**
     * Updates the (Markdown) notes of a Step.Place
     */
    suspend fun updatePlaceNote(dayId: String, stepId: String, note: String)

    /**
     * Sets the start time of a Step.Place (creates the schedule when missing)
     */
    suspend fun updatePlaceStartTime(dayId: String, stepId: String, time: LocalTime)

    /**
     * Sets the end time of a Step.Place (creates the schedule when missing)
     */
    suspend fun updatePlaceEndTime(dayId: String, stepId: String, time: LocalTime)

    /**
     * Adds a file to the inventory of a Step.Place: copies [source] to disk and records the
     * metadata. It writes the file first, the metadata after (consistency).
     */
    suspend fun addAttachment(dayId: String, stepId: String, source: PlatformFile)

    /**
     * Removes a file from the inventory of a Step.Place: deletes the metadata and then the file on disk.
     */
    suspend fun removeAttachment(dayId: String, stepId: String, attachmentId: String)

    //endregion Update

    //region Move

    /**
     * Moves a Place from the general list to a TravelDay
     */
    suspend fun movePlaceToDay(placeId: String, dayId: String)

    /**
     * Moves a Place from a TravelDay to the general list
     */
    suspend fun movePlaceToGeneral(placeId: String, dayId: String)

    /**
     * Moves a Place (held in the day's places list) into the steps list of the same day,
     * converting it into a Step.Place
     */
    suspend fun movePlaceToStep(placeId: String, dayId: String)

    /**
     * Moves a Step.Place (held in the day's steps list) into the places list of the same day,
     * converting it into a Place
     */
    suspend fun moveStepToPlace(stepId: String, dayId: String)

    /**
     * Moves a step up in the list
     */
    suspend fun moveTravelStepUp(stepId: String, dayId: String)

    /**
     * Moves a step down in the list
     */
    suspend fun moveTravelStepDown(stepId: String, dayId: String)

    //endregion Move

    /**
     * Saves a new Place
     * @param place the Place to save
     * @param dayId when set, adds the Place straight to the TravelDay with this id
     */
    suspend fun savePlace(place: PlaceDomain, dayId: String? = null)

    /**
     * Inserts a step in the position right after another step
     */
    suspend fun addTransportStep(dayId: String, afterStepId: String, step: StepDomain)

    /**
     * Deletes a Place from the travel plan
     */
    suspend fun deletePlace(placeId: String, dayId: String? = null)

    /**
     * Deletes a step from the day's steps list
     */
    suspend fun deleteStep(stepId: String, dayId: String)

    /**
     * Removes a step, deciding by type: a Step.Place goes back to the backlog (places), a
     * Step.Transport is deleted. The decision is a domain rule, not a UI one.
     */
    suspend fun removeStep(stepId: String, dayId: String)
}
