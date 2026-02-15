package com.takaotech.ktravel.presentation.planner

/**
 * Mapper per convertire tra [Place] e [TravelDay.Step.Place].
 */
object StepPlaceMapper {
    /**
     * Converte un [Place] in [TravelDay.Step.Place].
     * Mantiene lo stesso id per poter tracciare l'elemento dopo la conversione.
     */
    fun placeToStep(place: Place): TravelDay.Step.Place =
        TravelDay.Step.Place(
            id = place.id,
            location = place.name
        )

    /**
     * Converte un [TravelDay.Step.Place] in [Place].
     * Poiché [TravelDay.Step.Place] non contiene coordinate, vengono impostati valori di default
     * per lat/lng (0.0). Se disponibili, fornire una versione specifica di mapping che accetti
     * lat/lng reali.
     */
    fun stepToPlace(step: TravelDay.Step.Place, lat: Double = 0.0, lng: Double = 0.0): Place =
        Place(
            id = step.id,
            name = step.location,
            lat = lat,
            lng = lng,
            schedule = null
        )
}
