package com.takaotech.gunzou.api.search

import com.takaotech.gunzou.api.NavigatorJson
import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteRequest
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteResponse
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteSuggestion
import com.takaotech.gunzou.api.search.autocomplete.QuerySuggestionKind
import com.takaotech.gunzou.api.search.autocomplete.TermSuggestion
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AutocompleteSerializationTest {

    private val response = AutocompleteResponse(
        suggestions = listOf(
            AutocompleteSuggestion.Place(
                id = "here:pds:place:1",
                title = "Colosseo",
                resultType = SearchResultType.PLACE,
                position = GeoPoint(lat = 41.8902, lng = 12.4922),
                address = SearchAddress(label = "Colosseo, Piazza del Colosseo, Roma", countryCode = "IT"),
                category = PlaceCategoryGroup.SIGHTS_AND_MUSEUMS,
                distanceMeters = 350,
                highlights = SearchHighlights(title = listOf(TextRange(start = 0, end = 4))),
            ),
            AutocompleteSuggestion.Query(
                id = "here:cm:ontology:restaurant",
                title = "Restaurants near Rome",
                kind = QuerySuggestionKind.CATEGORY,
                searchText = "restaurant",
            ),
        ),
        termSuggestions = listOf(TermSuggestion(term = "colosseo", replaces = "colo", start = 0, end = 4)),
    )

    @Test
    fun `Given a place and a query suggestion When encoding and decoding Then the answer is unchanged`() {
        val encoded = NavigatorJson.encodeToString(AutocompleteResponse.serializer(), response)

        assertEquals(response, NavigatorJson.decodeFromString(AutocompleteResponse.serializer(), encoded))
    }

    @Test
    fun `Given a place and a query suggestion When encoded Then each is discriminated on type`() {
        val encoded = NavigatorJson.encodeToJsonElement(AutocompleteResponse.serializer(), response)

        val types = encoded.jsonObject.getValue("suggestions").jsonArray.map {
            it.jsonObject.getValue("type").jsonPrimitive.content
        }
        assertEquals(listOf("place", "query"), types)
    }

    @Test
    fun `Given values this client does not know When decoding a place Then it still decodes`() {
        val json = """
            {
              "suggestions": [
                {
                  "type": "place",
                  "id": "x",
                  "title": "Somewhere",
                  "resultType": "somethingNew",
                  "position": { "lat": 1.0, "lng": 2.0 },
                  "category": "aGroupFromTheFuture"
                }
              ]
            }
        """.trimIndent()

        val place = assertIs<AutocompleteSuggestion.Place>(
            NavigatorJson.decodeFromString(AutocompleteResponse.serializer(), json).suggestions.single(),
        )

        assertEquals(SearchResultType("somethingNew"), place.resultType)
        assertEquals(PlaceCategoryGroup("aGroupFromTheFuture"), place.category)
        assertEquals(SearchAddress(), place.address)
    }

    @Test
    fun `Given a request with every area shape When encoding and decoding Then each shape survives`() {
        val areas = listOf(
            SearchArea.Countries(listOf("IT", "SM")),
            SearchArea.Circle(center = GeoPoint(lat = 41.9, lng = 12.5), radiusMeters = 5_000),
            SearchArea.BoundingBox(west = 12.4, south = 41.8, east = 12.6, north = 42.0),
        )

        areas.forEach { area ->
            val request = AutocompleteRequest(query = "roma", area = area, language = "it-IT")
            val encoded = NavigatorJson.encodeToString(AutocompleteRequest.serializer(), request)

            assertEquals(request, NavigatorJson.decodeFromString(AutocompleteRequest.serializer(), encoded))
        }
    }

    @Test
    fun `Given each area shape When its kind is read Then it matches the type it is written with`() {
        val areas = listOf(
            SearchArea.Countries(listOf("IT")),
            SearchArea.Circle(center = GeoPoint(lat = 41.9, lng = 12.5), radiusMeters = 1),
            SearchArea.BoundingBox(west = 0.0, south = 0.0, east = 1.0, north = 1.0),
        )

        areas.forEach { area ->
            val type = NavigatorJson.encodeToJsonElement(SearchArea.serializer(), area)
                .jsonObject.getValue("type").jsonPrimitive.content

            assertEquals(area.kind.value, type)
        }
    }

    @Test
    fun `Given a request without a limit When encoded Then the default limit is written out`() {
        val encoded = NavigatorJson.encodeToJsonElement(
            AutocompleteRequest.serializer(),
            AutocompleteRequest(query = "a", language = "en-GB"),
        )

        assertEquals(
            AutocompleteRequest.DEFAULT_LIMIT.toString(),
            encoded.jsonObject.getValue("limit").jsonPrimitive.content,
        )
    }
}
