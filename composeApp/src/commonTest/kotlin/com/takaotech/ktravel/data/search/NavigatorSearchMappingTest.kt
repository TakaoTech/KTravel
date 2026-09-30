package com.takaotech.ktravel.data.search

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.common.SearchProviderId
import com.takaotech.gunzou.api.search.SearchResultType
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteSuggestion
import com.takaotech.ktravel.domain.search.model.PlaceReference
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class NavigatorSearchMappingTest :
    BehaviorSpec({

        given("a place HERE suggested to the autocomplete") {
            val suggestion = AutocompleteSuggestion.Place(
                id = "here:pds:place:380sr2yk-0f0d8a4b5e0b4b1f9f",
                title = "Trattoria Da Enzo",
                resultType = SearchResultType.PLACE,
                position = GeoPoint(lat = 41.88845, lng = 12.47697),
            )

            `when`("it becomes a candidate") {
                val candidate = suggestion.toCandidate(SearchProviderId.Here)

                then("it records HERE and HERE's own id, which is what its details are asked by") {
                    candidate.reference shouldBe PlaceReference(providerId = "here", placeId = suggestion.id)
                    candidate.id shouldBe "SEARCH:${suggestion.id}"
                }
            }
        }
    })
