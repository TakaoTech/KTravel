package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.domain.model.StepPlaceMapper.placeToStep
import com.takaotech.ktravel.domain.model.StepPlaceMapper.stepToPlace

/**
 * Conversione fra [PlaceDomain] (backlog, senza tempo) e [StepDomain.Place] (itinerario,
 * titolare dell'orario).
 *
 * La conversione è **asimmetrica in un punto solo**:
 * - [placeToStep] crea uno step *non ancora schedulato* ([StepDomain.Place.schedule] = null);
 *   l'orario si assegna in itinerario.
 * - [stepToPlace] riporta lo step nel backlog **scartando l'orario** (perdita voluta). Non lancia
 *   mai eccezioni.
 *
 * Tutto il resto attraversa: nota e allegati sono materiale del viaggiatore e non hanno niente a
 * che vedere con la posizione dell'elemento, quindi seguono il luogo in entrambi i versi. Anche
 * l'`id` si mantiene, sia per tracciare l'elemento dopo lo spostamento sia perché il
 * `relativePath` di un allegato lo contiene (`<travelId>/<id>/<uuid>.<ext>`): rigenerarlo
 * staccherebbe i file dai loro metadati.
 */
object StepPlaceMapper {
    /**
     * Converte un [PlaceDomain] in [StepDomain.Place] non ancora schedulato.
     */
    fun placeToStep(place: PlaceDomain): StepDomain.Place = StepDomain.Place(
        id = place.id,
        name = place.name,
        lat = place.lat,
        lng = place.lng,
        schedule = null,
        note = place.note,
        attachments = place.attachments,
    )

    /**
     * Converte un [StepDomain.Place] in [PlaceDomain] scartando lo [StepDomain.Place.schedule].
     */
    fun stepToPlace(step: StepDomain.Place): PlaceDomain = PlaceDomain(
        id = step.id,
        name = step.name,
        lat = step.lat,
        lng = step.lng,
        note = step.note,
        attachments = step.attachments,
    )
}
