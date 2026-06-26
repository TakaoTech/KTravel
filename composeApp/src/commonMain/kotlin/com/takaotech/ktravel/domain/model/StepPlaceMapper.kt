package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.domain.model.StepPlaceMapper.placeToStep
import com.takaotech.ktravel.domain.model.StepPlaceMapper.stepToPlace


/**
 * Conversione fra [PlaceDomain] (backlog, senza tempo) e [StepDomain.Place] (itinerario,
 * titolare dell'orario).
 *
 * La conversione è **volutamente asimmetrica**:
 * - [placeToStep] crea uno step *non ancora schedulato* ([StepDomain.Place.schedule] = null);
 *   l'orario si assegna in itinerario.
 * - [stepToPlace] riporta lo step nel backlog **scartando l'orario** (perdita voluta). Non lancia
 *   mai eccezioni.
 *
 * Entrambe mantengono lo stesso `id` per poter tracciare l'elemento dopo lo spostamento.
 */
object StepPlaceMapper {
    /**
     * Converte un [PlaceDomain] in [StepDomain.Place] non ancora schedulato.
     */
    fun placeToStep(place: PlaceDomain): StepDomain.Place =
        StepDomain.Place(
            id = place.id,
            name = place.name,
            lat = place.lat,
            lng = place.lng,
            schedule = null
        )

    /**
     * Converte un [StepDomain.Place] in [PlaceDomain] scartando lo [StepDomain.Place.schedule].
     */
    fun stepToPlace(step: StepDomain.Place): PlaceDomain =
        PlaceDomain(
            id = step.id,
            name = step.name,
            lat = step.lat,
            lng = step.lng
        )
}
