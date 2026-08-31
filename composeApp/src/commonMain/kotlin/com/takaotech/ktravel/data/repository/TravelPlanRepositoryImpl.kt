@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.data.datasource.AttachmentDataSource
import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSource
import com.takaotech.ktravel.data.mapper.TravelPlanEntityMapper.toDomain
import com.takaotech.ktravel.data.mapper.TravelPlanEntityMapper.toEntity
import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import com.takaotech.ktravel.domain.model.TravelPlanEditor.addStepAttachment
import com.takaotech.ktravel.domain.model.TravelPlanEditor.clearFinalDestinationSchedules
import com.takaotech.ktravel.domain.model.TravelPlanEditor.deletePlace
import com.takaotech.ktravel.domain.model.TravelPlanEditor.deleteStep
import com.takaotech.ktravel.domain.model.TravelPlanEditor.movePlaceToDay
import com.takaotech.ktravel.domain.model.TravelPlanEditor.movePlaceToGeneral
import com.takaotech.ktravel.domain.model.TravelPlanEditor.movePlaceToStep
import com.takaotech.ktravel.domain.model.TravelPlanEditor.moveStepDown
import com.takaotech.ktravel.domain.model.TravelPlanEditor.moveStepToPlace
import com.takaotech.ktravel.domain.model.TravelPlanEditor.moveStepUp
import com.takaotech.ktravel.domain.model.TravelPlanEditor.putTransportStep
import com.takaotech.ktravel.domain.model.TravelPlanEditor.removeStep
import com.takaotech.ktravel.domain.model.TravelPlanEditor.removeStepAttachment
import com.takaotech.ktravel.domain.model.TravelPlanEditor.savePlace
import com.takaotech.ktravel.domain.model.TravelPlanEditor.updatePlaceEndTime
import com.takaotech.ktravel.domain.model.TravelPlanEditor.updatePlaceStartTime
import com.takaotech.ktravel.domain.model.TravelPlanEditor.updateStep
import com.takaotech.ktravel.domain.model.TravelPlanEditor.updateStepNote
import com.takaotech.ktravel.domain.model.TravelSettingsDomain
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@SingleIn(PlanningGraphScope::class)
@ContributesBinding(PlanningGraphScope::class)
@Inject
class TravelPlanRepositoryImpl(
    @param:Named("travelId") private val travelId: String,
    private val dataSource: TravelPlanStorageDataSource,
    private val attachmentDataSource: AttachmentDataSource,
) : TravelPlanRepository {

    private val travelPlanId: String = travelId
    private val _planningState =
        MutableStateFlow(
            dataSource.getTravelPlan(travelPlanId).toDomain().clearFinalDestinationSchedules(),
        )
    override val planningState: StateFlow<TravelPlanDomain> = _planningState.asStateFlow()

    private suspend fun persistCurrentState() {
        val entity = _planningState.value.toEntity(id = travelPlanId)
        dataSource.saveTravelPlan(entity)
    }

    /**
     * Applies a pure mutation to the current state and persists the result. After every mutation it
     * reapplies the final-destination invariant (no time on the last place of the day).
     */
    private suspend fun mutate(transform: (TravelPlanDomain) -> TravelPlanDomain) {
        _planningState.update { transform(it).clearFinalDestinationSchedules() }
        persistCurrentState()
    }

    private fun TravelPlanDomain.setPeriod(start: Instant, end: Instant): TravelPlanDomain {
        val newDays = (start.toLocalDate()..end.toLocalDate()).map { newDate ->
            days.firstOrNull { it.date == newDate } ?: TravelDayDomain(date = newDate)
        }

        // TODO Pass Zone?
        return copy(
            periodStart = start.toLocalDate(),
            periodEnd = end.toLocalDate(),
            days = newDays,
        )
    }

    override fun getTravelDayFlow(dayId: String): Flow<TravelDayDomain> = planningState.map { state ->
        state.days.firstOrNull { it.id == dayId } ?: TravelDayDomain.EMPTY
    }

    override suspend fun updatePeriod(startMillis: Long, endMillis: Long) = mutate {
        it.setPeriod(
            start = Instant.fromEpochMilliseconds(startMillis),
            end = Instant.fromEpochMilliseconds(endMillis),
        )
    }

    override suspend fun updateStep(dayId: String, stepId: String, updatedStep: StepDomain) =
        mutate { it.updateStep(dayId, stepId, updatedStep) }

    override suspend fun updateStepNote(dayId: String, stepId: String, note: String) =
        mutate { it.updateStepNote(dayId, stepId, note) }

    override suspend fun updatePlaceStartTime(dayId: String, stepId: String, time: LocalTime) =
        mutate { it.updatePlaceStartTime(dayId, stepId, time) }

    override suspend fun updatePlaceEndTime(dayId: String, stepId: String, time: LocalTime) =
        mutate { it.updatePlaceEndTime(dayId, stepId, time) }

    override suspend fun addAttachment(dayId: String, stepId: String, source: PlatformFile) {
        // The file on disk first, the metadata after: this avoids references to missing files.
        val attachment = attachmentDataSource.saveAttachment(travelId, stepId, source).toDomain()
        mutate { it.addStepAttachment(dayId, stepId, attachment) }
    }

    override suspend fun removeAttachment(dayId: String, stepId: String, attachmentId: String) {
        val relativePath = _planningState.value.days
            .firstOrNull { it.id == dayId }?.steps
            ?.firstOrNull { it.id == stepId }
            ?.let { step ->
                when (step) {
                    is StepDomain.Place -> step.attachments
                    is StepDomain.Transport -> step.attachments
                }
            }
            ?.firstOrNull { it.id == attachmentId }?.relativePath
        mutate { it.removeStepAttachment(dayId, stepId, attachmentId) }
        relativePath?.let { attachmentDataSource.deleteAttachment(it) }
    }

    override suspend fun updatePlanName(name: String) = mutate { it.copy(name = name) }

    override suspend fun updateSettings(settings: TravelSettingsDomain) = mutate { it.copy(settings = settings) }

    override suspend fun savePlace(place: PlaceDomain, dayId: String?) = mutate { it.savePlace(place, dayId) }

    override suspend fun movePlaceToDay(placeId: String, dayId: String) = mutate { it.movePlaceToDay(placeId, dayId) }

    override suspend fun movePlaceToGeneral(placeId: String, dayId: String) =
        mutate { it.movePlaceToGeneral(placeId, dayId) }

    override suspend fun movePlaceToStep(placeId: String, dayId: String) = mutate { it.movePlaceToStep(placeId, dayId) }

    override suspend fun moveStepToPlace(stepId: String, dayId: String) = mutate { it.moveStepToPlace(stepId, dayId) }

    override suspend fun moveTravelStepUp(stepId: String, dayId: String) = mutate { it.moveStepUp(stepId, dayId) }

    override suspend fun moveTravelStepDown(stepId: String, dayId: String) = mutate { it.moveStepDown(stepId, dayId) }

    override suspend fun putTransportStep(dayId: String, afterStepId: String, step: StepDomain.Transport) =
        mutate { it.putTransportStep(dayId, afterStepId, step) }

    override suspend fun deleteStep(stepId: String, dayId: String) = mutate { it.deleteStep(stepId, dayId) }

    override suspend fun removeStep(stepId: String, dayId: String) = mutate { it.removeStep(stepId, dayId) }

    /**
     * Deleting a place for good takes its files with it: this is the one exit from the backlog that
     * is not a move, so nothing will reference those binaries again. Same order as
     * [removeAttachment] — read the paths while the place is still in the state, then delete.
     */
    override suspend fun deletePlace(placeId: String, dayId: String?) {
        val state = _planningState.value
        val places = if (dayId == null) state.places else state.days.firstOrNull { it.id == dayId }?.places
        val relativePaths = places?.firstOrNull { it.id == placeId }
            ?.attachments.orEmpty()
            .map { it.relativePath }
        mutate { it.deletePlace(placeId, dayId) }
        relativePaths.forEach { attachmentDataSource.deleteAttachment(it) }
    }
}
