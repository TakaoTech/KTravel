package com.takaotech.ktravel.gunzou.server

import com.takaotech.gunzou.api.NavigatorApi
import com.takaotech.gunzou.api.NavigatorJson
import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.common.SearchProviderId
import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.search.GeoBounds
import com.takaotech.gunzou.api.search.PlaceCategoryGroup
import com.takaotech.gunzou.api.search.SearchArea
import com.takaotech.gunzou.api.search.SearchCatalogResponse
import com.takaotech.gunzou.api.search.SearchProfile
import com.takaotech.gunzou.api.search.SearchResultType
import com.takaotech.gunzou.api.search.TextRange
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteRequest
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteResponse
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteSuggestion
import com.takaotech.gunzou.api.search.autocomplete.QuerySuggestionKind
import com.takaotech.gunzou.api.search.autocomplete.TermSuggestion
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `POST /v1/here/search/autocomplete` end to end, with a recorded HERE Autosuggest answer behind it.
 *
 * What is pinned is the translation both ways: that the provider neutral request becomes the query
 * string HERE expects — countries in alpha-3, a language it can read — and that HERE's answer becomes
 * places with a position and a broad category, and queries without HERE's URL in them.
 */
class HereAutocompleteTest {

    private val request = AutocompleteRequest(
        query = "colo",
        at = GeoPoint(lat = 41.89, lng = 12.49),
        language = "it-IT",
        limit = 5,
    )

    // ---- what reaches HERE -------------------------------------------------------------------

    @Test
    fun `Given an autocomplete request When searching Then the autosuggest host is called with the query`() =
        testApplication {
            val here = HereMockServer(HerePayloads.AUTOSUGGEST)
            application { module(here.asKoinModule()) }

            client.postJson(
                path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
                serializer = AutocompleteRequest.serializer(),
                body = request,
            )

            val url = here.requestUrl
            assertEquals("autosuggest.search.hereapi.com", url.host)
            assertTrue(url.encodedPath.endsWith("/autosuggest"), "Called ${url.encodedPath}")
            assertEquals("colo", url.query("q"))
            assertEquals("41.89,12.49", url.query("at"))
            assertEquals("5", url.query("limit"))
            assertEquals("it-IT", url.query("lang"))
        }

    @Test
    fun `Given a countries area When searching Then HERE receives the countries in alpha-3`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val inItaly = request.copy(area = SearchArea.Countries(listOf("IT", "SM")))

        client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = inItaly,
        )

        assertEquals("countryCode:ITA,SMR", here.requestUrl.query("in"))
    }

    @Test
    fun `Given a bounding box and no point When searching Then the box locates the search`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val inBox = request.copy(
            at = null,
            area = SearchArea.BoundingBox(west = 12.4, south = 41.8, east = 12.6, north = 42.0),
        )

        val response = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = inBox,
        )

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("bbox:12.4,41.8,12.6,42.0", here.requestUrl.query("in"))
        assertNull(here.requestUrl.query("at"))
    }

    @Test
    fun `Given a body without a language When searching Then it is refused and HERE is never called`() =
        testApplication {
            val here = HereMockServer(HerePayloads.AUTOSUGGEST)
            application { module(here.asKoinModule()) }

            val response = client.postRaw(
                path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
                body = """{ "query": "colo", "at": { "lat": 41.89, "lng": 12.49 } }""",
            )

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals(ErrorCode.INVALID_REQUEST, response.decodeError().code)
            assertEquals(emptyList(), here.requests)
        }

    // ---- what comes back ---------------------------------------------------------------------

    @Test
    fun `Given a place When translated Then its position category and highlights come through`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val suggestions = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = request,
        )
            .decodeAutocomplete().suggestions

        val colosseo = assertIs<AutocompleteSuggestion.Place>(suggestions.first())
        assertEquals(SearchResultType.PLACE, colosseo.resultType)
        assertEquals(GeoPoint(lat = 41.89021, lng = 12.49223), colosseo.position)
        // The primary category wins over the first one listed.
        assertEquals(PlaceCategoryGroup.SIGHTS_AND_MUSEUMS, colosseo.category)
        assertEquals(350L, colosseo.distanceMeters)
        assertEquals(listOf(TextRange(start = 0, end = 4)), colosseo.highlights?.title)
        assertEquals(listOf(TextRange(start = 0, end = 4)), colosseo.highlights?.addressLabel)
    }

    @Test
    fun `Given a place address in alpha-3 When translated Then the country is named in alpha-2`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val colosseo = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = request,
        )
            .decodeAutocomplete().suggestions.first()

        val address = assertIs<AutocompleteSuggestion.Place>(colosseo).address
        assertEquals("IT", address.countryCode)
        assertEquals("Roma", address.city)
        assertEquals("00184", address.postalCode)
    }

    @Test
    fun `Given a street and a postal code When translated Then each keeps its type and extent`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val places = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = request,
        )
            .decodeAutocomplete().suggestions.filterIsInstance<AutocompleteSuggestion.Place>()

        val street = places[1]
        assertEquals(SearchResultType.STREET, street.resultType)
        assertEquals(GeoBounds(west = 12.48871, south = 41.89311, east = 12.49114, north = 41.89443), street.bounds)
        assertNull(street.category)

        // A locality of type postal code is a postal code, however HERE spells it.
        assertEquals(SearchResultType.POSTAL_CODE, places[2].resultType)
    }

    @Test
    fun `Given an entity without a position When translated Then it is not offered`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val suggestions = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = request,
        )
            .decodeAutocomplete().suggestions

        assertEquals(3, suggestions.filterIsInstance<AutocompleteSuggestion.Place>().size)
        assertTrue(suggestions.none { it.id == "here:af:street:3" })
    }

    @Test
    fun `Given category and chain queries When translated Then they keep their kind and lose the HERE URL`() =
        testApplication {
            val here = HereMockServer(HerePayloads.AUTOSUGGEST)
            application { module(here.asKoinModule()) }

            val response = client.postJson(
                path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
                serializer = AutocompleteRequest.serializer(),
                body = request,
            )
            val body = response.bodyAsText()
            val queries = NavigatorJson.decodeFromString(AutocompleteResponse.serializer(), body)
                .suggestions.filterIsInstance<AutocompleteSuggestion.Query>()

            assertEquals(listOf(QuerySuggestionKind.CATEGORY, QuerySuggestionKind.CHAIN), queries.map { it.kind })
            assertEquals(listOf("restaurant", "Coop"), queries.map { it.searchText })
            assertTrue("hereapi.com" !in body, "The provider's URL leaked into the answer")
        }

    @Test
    fun `Given term completions When translated Then they reach the answer`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val terms = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = request,
        )
            .decodeAutocomplete().termSuggestions

        assertEquals(listOf(TermSuggestion(term = "colosseo", replaces = "colo", start = 0, end = 4)), terms)
    }

    @Test
    fun `Given nothing matches When searching Then it is an empty answer and not an error`() = testApplication {
        val here = HereMockServer(HerePayloads.NO_SUGGESTIONS)
        application { module(here.asKoinModule()) }

        val response = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = request,
        )

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(emptyList(), response.decodeAutocomplete().suggestions)
    }

    // ---- what never reaches HERE -------------------------------------------------------------

    @Test
    fun `Given no provider key When searching Then HERE is never called`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val response = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = request,
            providerKey = null,
        )

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorCode.MISSING_CREDENTIALS, response.decodeError().code)
        assertEquals(emptyList(), here.requests)
    }

    @Test
    fun `Given more results than HERE returns When searching Then its own limit is enforced`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val response = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = request.copy(limit = SearchProfile.HereAutocomplete.descriptor.maxResults + 1),
        )

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorCode.INVALID_REQUEST, response.decodeError().code)
        assertEquals(emptyList(), here.requests)
    }

    @Test
    fun `Given a countries area without a point When searching Then the search is not located and HERE is not called`() =
        testApplication {
            val here = HereMockServer(HerePayloads.AUTOSUGGEST)
            application { module(here.asKoinModule()) }

            val unlocated = request.copy(at = null, area = SearchArea.Countries(listOf("IT")))

            val response = client.postJson(
                path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
                serializer = AutocompleteRequest.serializer(),
                body = unlocated,
            )

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertContains(response.decodeError().message, "locate")
            assertEquals(emptyList(), here.requests)
        }

    @Test
    fun `Given both a point and a circle When searching Then the request is refused before HERE`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val both = request.copy(
            area = SearchArea.Circle(center = GeoPoint(lat = 41.9, lng = 12.5), radiusMeters = 1_000),
        )

        val response = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = both,
        )

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(emptyList(), here.requests)
    }

    @Test
    fun `Given invalid fields When searching Then validation names every one of them`() = testApplication {
        val here = HereMockServer(HerePayloads.AUTOSUGGEST)
        application { module(here.asKoinModule()) }

        val invalid = AutocompleteRequest(
            query = "  ",
            language = "english",
            at = GeoPoint(lat = 123.0, lng = 12.49),
            area = SearchArea.Countries(listOf("ITA", "XX")),
            limit = 0,
        )

        val response = client.postJson(
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            serializer = AutocompleteRequest.serializer(),
            body = invalid,
        )

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val message = response.decodeError().message
        listOf("query", "'english'", "at.lat", "limit", "'ITA'", "'XX'").forEach { assertContains(message, it) }
        assertEquals(emptyList(), here.requests)
    }

    // ---- the catalog -------------------------------------------------------------------------

    @Test
    fun `Given the embedded server When the search catalog is fetched Then HERE autocomplete is listed`() =
        testApplication {
            application { module(HereMockServer().asKoinModule()) }

            val body = client.get(NavigatorApi.SEARCH_PROFILES).bodyAsText()
            val catalog = NavigatorJson.decodeFromString(SearchCatalogResponse.serializer(), body)

            assertEquals(listOf(SearchProfile.HereAutocomplete.descriptor), catalog.profiles)
            assertEquals(SearchProviderId.Here, catalog.profiles.single().provider)
        }
}
