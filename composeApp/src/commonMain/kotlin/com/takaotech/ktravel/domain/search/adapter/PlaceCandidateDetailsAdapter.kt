package com.takaotech.ktravel.domain.search.adapter

import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceDetails

/**
 * The details a [PlaceCandidate] already carries: its name, position, address and category.
 *
 * What a card shows the moment it opens, before a provider answered, and all it ever shows for a
 * place no provider can be asked about, such as typed coordinates. The reference is kept, so the
 * richer details can be asked for from these.
 */
object PlaceCandidateDetailsAdapter : PlaceDetailsAdapter<PlaceCandidate> {

    override fun adapt(source: PlaceCandidate): PlaceDetails = PlaceDetails(
        title = source.title,
        coordinate = source.coordinate,
        addressLabel = source.addressLabel,
        locality = source.locality,
        category = source.category,
        reference = source.reference,
    )
}
