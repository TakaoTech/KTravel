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
 * Rigenera tutti gli id di un piano importato, per poterlo affiancare a un viaggio già presente
 * senza collisioni.
 *
 * Il piano non contiene riferimenti incrociati per id (nessun puntatore step -> place, nessun id
 * dentro le rotte), quindi la rigenerazione è "piatta". L'unico legame da mantenere coerente è
 * quello fra il `relative_path` degli allegati e i riferimenti `ktravel://attachment/...` dentro le
 * note Markdown.
 */
internal object TravelArchiveIdRemapper {

    data class Remapped(
        val plan: TravelPlanEntity,
        /** vecchio relativePath -> nuovo relativePath, per estrarre i file e riscrivere le note. */
        val attachmentPathMapping: Map<String, String>,
    )

    /**
     * @param newTravelId id del viaggio di destinazione.
     * @param newId generatore di id, iniettabile per rendere i test deterministici.
     */
    fun remap(
        plan: TravelPlanEntity,
        newTravelId: String,
        newId: () -> String = { Uuid.random().toString() },
    ): Remapped {
        val pathMapping = mutableMapOf<String, String>()

        // Primo passaggio: rigenera gli id e raccoglie la mappa completa dei path.
        val daysWithNewIds = plan.days.map { day ->
            val newDayId = newId()
            val steps = day.steps.map { step -> step.remapIds(newTravelId, newId, pathMapping) }
            day.copy(id = newDayId, steps = steps, places = day.places.remapIds(newId))
        }

        // Secondo passaggio: riscrive le note solo ora, perché una nota può referenziare
        // l'allegato di un altro step e la mappa deve essere completa.
        val days = daysWithNewIds.map { day ->
            day.copy(steps = day.steps.map { step -> step.rewriteNote(pathMapping) })
        }

        return Remapped(
            plan = plan.copy(
                id = newTravelId,
                days = days,
                places = plan.places.remapIds(newId),
            ),
            attachmentPathMapping = pathMapping,
        )
    }

    private fun List<PlaceEntity>.remapIds(newId: () -> String): List<PlaceEntity> =
        map { place -> place.copy(id = newId()) }

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
     * Il nome fisico del file è già un uuid univoco e viene conservato: cambiano solo le cartelle
     * viaggio e step.
     */
    private fun String.movedTo(newTravelId: String, newStepId: String): String =
        "$newTravelId/$newStepId/${substringAfterLast('/')}"
}
