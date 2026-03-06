package com.takaotech.ktravel.domain.model

/**
 * Mapper per convertire tra [PlaceDomain] e [StepDomain.Place].
 */
object StepPlaceMapper {
    /**
     * Converte un [PlaceDomain] in [StepDomain.Place].
     * Mantiene lo stesso id per poter tracciare l'elemento dopo la conversione.
     */
    fun placeToStep(place: PlaceDomain): StepDomain.Place =
        StepDomain.Place(
            id = place.id,
            location = place.name,
            lat = place.lat,
            lng = place.lng,
        )

    /**
     * Converte un [StepDomain.Place] in [PlaceDomain].
     * Se lat/lng non sono disponibili nello step, vengono impostati valori di default (0.0).
     */
    fun stepToPlace(step: StepDomain.Place, lat: Double = step.lat, lng: Double = step.lng): PlaceDomain =
        PlaceDomain(
            id = step.id,
            name = step.location,
            lat = lat,
            lng = lng,
            schedule = null
        )
}
