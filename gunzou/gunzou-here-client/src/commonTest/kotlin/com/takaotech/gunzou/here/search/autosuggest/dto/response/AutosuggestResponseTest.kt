package com.takaotech.gunzou.here.search.autosuggest.dto.response

import com.takaotech.gunzou.here.common.hereApiJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AutosuggestResponseTest {

    @Test
    fun `Given entity and query suggestions When the response is decoded Then each item gets its own type`() {
        val response = hereApiJson.decodeFromString<AutosuggestResponse>(SAMPLE_RESPONSE)

        val place = assertIs<AutosuggestResultItem.Entity>(response.items[0])
        assertEquals("place", place.resultType)
        assertEquals("Berlin", place.address.city)
        assertEquals(52.5308, place.position?.lat)
        assertEquals(true, place.driveThrough)
        assertEquals("Restau", place.highlights?.title?.single()?.toIntRange()?.let(place.title::substring))

        val query = assertIs<AutosuggestResultItem.Query>(response.items[1])
        assertEquals("categoryQuery", query.resultType)
        assertTrue(query.href.orEmpty().startsWith("https://autosuggest.search.hereapi.com/v1/discover"))
        assertNull(query.noResultOnFollowUp)
        assertNull(query.highlights?.address)

        val term = response.queryTerms.single()
        assertEquals("restaurant", term.term)
        assertEquals("restau", "restau berlin".substring(term.toIntRange()))
    }

    @Test
    fun `Given a query suggestion with an unknown result type When the response is decoded Then it is still a query`() {
        val response = hereApiJson.decodeFromString<AutosuggestResponse>(
            """{"items":[{"title":"Something","id":"x","resultType":"futureQuery"}],"queryTerms":[]}""",
        )

        assertIs<AutosuggestResultItem.Query>(response.items.single())
    }

    private companion object {
        val SAMPLE_RESPONSE = """
            {
              "items": [
                {
                  "title": "Restaurant Tim Raue",
                  "id": "here:pds:place:276u33db-8b7b1cf4d8d74e1e9e6a5e3f1a0a2b1c",
                  "resultType": "place",
                  "address": {"label": "Restaurant Tim Raue, Rudi-Dutschke-Straße 26, 10969 Berlin", "city": "Berlin"},
                  "position": {"lat": 52.5308, "lng": 13.3856},
                  "access": [{"lat": 52.53086, "lng": 13.38559}],
                  "driveThrough": true,
                  "distance": 1296,
                  "categories": [{"id": "100-1000-0000", "name": "Restaurant", "primary": true}],
                  "chains": [{"id": "1566"}],
                  "highlights": {"title": [{"start": 0, "end": 6}], "address": {"label": [{"start": 0, "end": 6}]}}
                },
                {
                  "title": "Restaurants near Berlin",
                  "id": "here:cm:ontology:restaurant",
                  "resultType": "categoryQuery",
                  "href": "https://autosuggest.search.hereapi.com/v1/discover?at=52.5308,13.3856&q=restaurant",
                  "highlights": {"title": [{"start": 0, "end": 6}]}
                }
              ],
              "queryTerms": [{"term": "restaurant", "replaces": "restau", "start": 0, "end": 6}]
            }
        """.trimIndent()
    }
}
