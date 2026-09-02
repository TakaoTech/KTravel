@file:OptIn(ExperimentalUuidApi::class)

package com.takaotech.ktravel.data.archive

import com.takaotech.ktravel.data.entity.AttachmentEntity
import com.takaotech.ktravel.data.entity.PlaceEntity
import com.takaotech.ktravel.data.entity.StepEntity
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.domain.model.AttachmentReference
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Regenerates every id of an imported plan, so it can sit next to an already existing trip without
 * collisions.
 *
 * Used only by the `DUPLICATE` conflict strategy: replacing a trip keeps the ids as they are.
 *
 * The plan holds no cross references by id (no step -> place pointer, no id inside the routes), so
 * the regeneration is flat. The one link that has to stay consistent is the one between the
 * `relative_path` of the attachments and the `ktravel://attachment/...` references inside the
 * Markdown notes.
 */
internal object TravelArchiveIdRemapper {

    /**
     * @property plan the plan with every id regenerated and every note already rewritten.
     * @property attachmentPathMapping old relativePath -> new relativePath, used to read each file
     * out of the archive under its old name and write it under the new one.
     */
    data class Remapped(val plan: TravelPlanEntity, val attachmentPathMapping: Map<String, String>)

    /**
     * Rebuilds [plan] under fresh ids, in two passes over the tree.
     *
     * @param newTravelId id of the destination trip.
     * @param newId id generator, injectable to keep the tests deterministic.
     */
    fun remap(
        plan: TravelPlanEntity,
        newTravelId: String,
        newId: () -> String = { Uuid.random().toString() },
    ): Remapped {
        val pathMapping = mutableMapOf<String, String>()

        // First pass: regenerate the ids and collect the complete path mapping.
        val daysWithNewIds = plan.days.map { day ->
            val newDayId = newId()
            val steps = day.steps.map { step -> step.remapIds(newTravelId, newId, pathMapping) }
            day.copy(id = newDayId, steps = steps, places = day.places.remapIds(newTravelId, newId, pathMapping))
        }
        val backlogWithNewIds = plan.places.remapIds(newTravelId, newId, pathMapping)

        // Second pass: the notes are rewritten only now, because a note may reference the
        // attachment of another step or of a backlog place, and the mapping has to be complete.
        val days = daysWithNewIds.map { day ->
            day.copy(
                steps = day.steps.map { step -> step.rewriteNote(pathMapping) },
                places = day.places.rewriteNotes(pathMapping),
            )
        }

        return Remapped(
            plan = plan.copy(
                id = newTravelId,
                days = days,
                places = backlogWithNewIds.rewriteNotes(pathMapping),
            ),
            attachmentPathMapping = pathMapping,
        )
    }

    /**
     * A place carries a note and attachments just like a step, so it is remapped like a step: a new
     * id, and the files moved under the new trip/place pair.
     */
    private fun List<PlaceEntity>.remapIds(
        newTravelId: String,
        newId: () -> String,
        pathMapping: MutableMap<String, String>,
    ): List<PlaceEntity> = map { place ->
        val newPlaceId = newId()
        place.copy(
            id = newPlaceId,
            attachments = place.attachments.map { attachment ->
                val newPath = attachment.relativePath.movedTo(newTravelId, newPlaceId)
                pathMapping[attachment.relativePath] = newPath
                attachment.copy(id = newId(), relativePath = newPath)
            },
        )
    }

    private fun List<PlaceEntity>.rewriteNotes(pathMapping: Map<String, String>): List<PlaceEntity> =
        map { place -> place.copy(note = AttachmentReference.rewriteReferences(place.note, pathMapping)) }

    private fun StepEntity.remapIds(
        newTravelId: String,
        newId: () -> String,
        pathMapping: MutableMap<String, String>,
    ): StepEntity {
        val newStepId = newId()
        fun List<AttachmentEntity>.moved(): List<AttachmentEntity> = map { attachment ->
            val newPath = attachment.relativePath.movedTo(newTravelId, newStepId)
            pathMapping[attachment.relativePath] = newPath
            attachment.copy(id = newId(), relativePath = newPath)
        }

        return when (this) {
            is StepEntity.Transport -> copy(id = newStepId, attachments = attachments.moved())
            is StepEntity.Place -> copy(id = newStepId, attachments = attachments.moved())
        }
    }

    private fun StepEntity.rewriteNote(pathMapping: Map<String, String>): StepEntity = when (this) {
        is StepEntity.Transport -> copy(note = AttachmentReference.rewriteReferences(note, pathMapping))
        is StepEntity.Place -> copy(note = AttachmentReference.rewriteReferences(note, pathMapping))
    }

    /**
     * The physical file name is already a unique uuid and is kept as is: only the trip and step
     * folders change.
     */
    private fun String.movedTo(newTravelId: String, newStepId: String): String =
        "$newTravelId/$newStepId/${substringAfterLast('/')}"
}
