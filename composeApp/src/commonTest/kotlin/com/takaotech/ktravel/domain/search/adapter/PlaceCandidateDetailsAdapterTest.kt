package com.takaotech.ktravel.domain.search.adapter

import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import com.takaotech.ktravel.domain.search.model.PlaceDetails
import com.takaotech.ktravel.domain.search.model.PlaceReference
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class PlaceCandidateDetailsAdapterTest :
    BehaviorSpec({

        given("a search result found by HERE") {
            val candidate = PlaceCandidate(
                id = "SEARCH:here:pds:place:1",
                title = "Torrazzo di Cremona",
                coordinate = GeoCoordinate(lat = 45.1334, lng = 10.0246),
                source = PlaceCandidateSource.SEARCH,
                locality = "Cremona",
                addressLabel = "Piazza del Comune, 26100 Cremona CR, Italia",
                category = PlaceCategory.SIGHTS_AND_MUSEUMS,
                distanceMeters = 420,
                reference = PlaceReference(providerId = "here", placeId = "here:pds:place:1"),
            )

            `when`("it is adapted") {
                val details = PlaceCandidateDetailsAdapter.adapt(candidate)

                then("it carries what the candidate knows, the reference included, and nothing more") {
                    details shouldBe PlaceDetails(
                        title = "Torrazzo di Cremona",
                        coordinate = GeoCoordinate(lat = 45.1334, lng = 10.0246),
                        addressLabel = "Piazza del Comune, 26100 Cremona CR, Italia",
                        locality = "Cremona",
                        category = PlaceCategory.SIGHTS_AND_MUSEUMS,
                        reference = PlaceReference(providerId = "here", placeId = "here:pds:place:1"),
                    )
                }
            }
        }

        given("coordinates the traveller typed") {
            val candidate = PlaceCandidate(
                id = "COORDINATES:45.26,9.34",
                title = "45.26, 9.34",
                coordinate = GeoCoordinate(lat = 45.26, lng = 9.34),
                source = PlaceCandidateSource.COORDINATES,
            )

            `when`("they are adapted") {
                val details = PlaceCandidateDetailsAdapter.adapt(candidate)

                then("no provider can be asked about them") {
                    details.reference shouldBe null
                    details.contacts shouldBe emptyList()
                    details.openingHours shouldBe emptyList()
                }
            }
        }
    })
