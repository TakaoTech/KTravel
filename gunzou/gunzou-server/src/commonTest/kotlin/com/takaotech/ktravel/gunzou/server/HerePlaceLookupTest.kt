package com.takaotech.ktravel.gunzou.server

import com.takaotech.gunzou.api.NavigatorApi
import com.takaotech.gunzou.api.NavigatorJson
import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.search.GeoBounds
import com.takaotech.gunzou.api.search.PlaceCategoryGroup
import com.takaotech.gunzou.api.search.SearchResultType
import com.takaotech.gunzou.api.search.place.ContactKind
import com.takaotech.gunzou.api.search.place.OpeningPeriod
import com.takaotech.gunzou.api.search.place.PermanentClosure
import com.takaotech.gunzou.api.search.place.PlaceContact
import com.takaotech.gunzou.api.search.place.PlaceDetails
import com.takaotech.gunzou.api.search.place.PlaceTimeZone
import com.takaotech.gunzou.api.search.place.Weekday
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLPathPart
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `GET /v1/here/search/id/{id}` end to end, with a recorded HERE Lookup answer behind it.
 *
 * What is pinned is the translation both ways: that the path and the query string become the Lookup
 * call HERE expects, and that its answer becomes provider neutral details — contacts by kind, hours
 * as recurring periods, closed days left out.
 */
class HerePlaceLookupTest {

    private val placeId = "here:pds:place:380sr2yk-0f0d8a4b5e0b4b1f9f"

    // ---- what reaches HERE -------------------------------------------------------------------

    @Test
    fun `Given a place id When looking it up Then the lookup host is called with the id language and time zone`() =
        testApplication {
            val here = HereMockServer(HerePayloads.LOOKUP)
            application { module(here.asKoinModule()) }

            val response = client.lookup(placeId)

            assertEquals(HttpStatusCode.OK, response.status)
            val url = here.requestUrl
            assertEquals("lookup.search.hereapi.com", url.host)
            assertTrue(url.encodedPath.endsWith("/lookup"), "Called ${url.encodedPath}")
            assertEquals(placeId, url.query("id"))
            assertEquals("it-IT", url.query("lang"))
            assertEquals("tz", url.query("show"))
        }

    // ---- what comes back ---------------------------------------------------------------------

    @Test
    fun `Given a restaurant When translated Then its identity address and category come through`() = testApplication {
        val here = HereMockServer(HerePayloads.LOOKUP)
        application { module(here.asKoinModule()) }

        val details = client.lookup(placeId).decodePlaceDetails()

        assertEquals(placeId, details.id)
        assertEquals("Trattoria Da Enzo", details.title)
        assertEquals(SearchResultType.PLACE, details.resultType)
        assertEquals(GeoPoint(lat = 41.88845, lng = 12.47697), details.position)
        assertEquals("IT", details.address.countryCode)
        assertEquals("Trastevere", details.address.district)
        assertEquals(PlaceCategoryGroup.EAT_AND_DRINK, details.category)
        assertNull(details.bounds)
    }

    @Test
    fun `Given contacts of every kind When translated Then they keep their kind and label and the fax is dropped`() =
        testApplication {
            val here = HereMockServer(HerePayloads.LOOKUP)
            application { module(here.asKoinModule()) }

            val details = client.lookup(placeId).decodePlaceDetails()

            assertEquals(
                listOf(
                    PlaceContact(kind = ContactKind.PHONE, value = "+39 06 581 2260"),
                    PlaceContact(kind = ContactKind.MOBILE, value = "+39 333 000 0000", label = "Prenotazioni"),
                    PlaceContact(kind = ContactKind.WEBSITE, value = "https://www.daenzoal29.com"),
                    PlaceContact(kind = ContactKind.EMAIL, value = "info@daenzoal29.com"),
                ),
                details.contacts,
            )
        }

    @Test
    fun `Given structured opening hours When translated Then closed days and unreadable rules are left out`() =
        testApplication {
            val here = HereMockServer(HerePayloads.LOOKUP)
            application { module(here.asKoinModule()) }

            val details = client.lookup(placeId).decodePlaceDetails()

            val mondayToSaturday = Weekday.ALL.dropLast(1)
            assertEquals(
                listOf(
                    OpeningPeriod(days = mondayToSaturday, opensAt = "12:30", durationMinutes = 150),
                    OpeningPeriod(days = mondayToSaturday, opensAt = "19:30", durationMinutes = 210),
                    // A daily rule without BYDAY applies to every day.
                    OpeningPeriod(days = Weekday.ALL, opensAt = "08:00", durationMinutes = 60),
                ),
                details.openingHours,
            )
        }

    @Test
    fun `Given a time zone and a suspected closure When translated Then both come through`() = testApplication {
        val here = HereMockServer(HerePayloads.LOOKUP)
        application { module(here.asKoinModule()) }

        val details = client.lookup(placeId).decodePlaceDetails()

        assertEquals(PlaceTimeZone(name = "Europe/Rome", utcOffset = "+02:00"), details.timeZone)
        assertEquals(PermanentClosure.POSSIBLE, details.permanentClosure)
    }

    @Test
    fun `Given a street When translated Then it has an extent and no place details`() = testApplication {
        val here = HereMockServer(HerePayloads.LOOKUP_STREET)
        application { module(here.asKoinModule()) }

        val details = client.lookup("here:af:street:1").decodePlaceDetails()

        assertEquals(SearchResultType.STREET, details.resultType)
        assertEquals(GeoBounds(west = 12.47666, south = 41.89726, east = 12.48218, north = 41.9107), details.bounds)
        assertEquals(emptyList(), details.contacts)
        assertEquals(emptyList(), details.openingHours)
        assertNull(details.timeZone)
        assertNull(details.permanentClosure)
    }

    // ---- failures ----------------------------------------------------------------------------

    @Test
    fun `Given an id HERE does not know When looking it up Then it is a 404 with PLACE_NOT_FOUND`() = testApplication {
        val here = HereMockServer(HerePayloads.LOOKUP_NOT_FOUND, HttpStatusCode.NotFound)
        application { module(here.asKoinModule()) }

        val response = client.lookup("here:pds:place:unknown")

        assertEquals(HttpStatusCode.NotFound, response.status)
        val error = response.decodeError()
        assertEquals(ErrorCode.PLACE_NOT_FOUND, error.code)
        assertEquals(404, error.providerStatus)
    }

    @Test
    fun `Given a place HERE sent without a position When looking it up Then it is not found`() = testApplication {
        val here = HereMockServer(HerePayloads.LOOKUP_WITHOUT_POSITION)
        application { module(here.asKoinModule()) }

        val response = client.lookup("here:pds:place:nowhere")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(ErrorCode.PLACE_NOT_FOUND, response.decodeError().code)
    }

    // ---- what never reaches HERE -------------------------------------------------------------

    @Test
    fun `Given no language When looking up Then it is refused and HERE is never called`() = testApplication {
        val here = HereMockServer(HerePayloads.LOOKUP)
        application { module(here.asKoinModule()) }

        val response = client.lookup(placeId, language = null)

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = response.decodeError()
        assertEquals(ErrorCode.INVALID_REQUEST, error.code)
        assertContains(error.message, NavigatorApi.SEARCH_LANGUAGE_PARAMETER)
        assertEquals(emptyList(), here.requests)
    }

    @Test
    fun `Given a language that is not a tag When looking up Then it is refused and HERE is never called`() =
        testApplication {
            val here = HereMockServer(HerePayloads.LOOKUP)
            application { module(here.asKoinModule()) }

            val response = client.lookup(placeId, language = "english")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertContains(response.decodeError().message, "'english'")
            assertEquals(emptyList(), here.requests)
        }

    @Test
    fun `Given no provider key When looking up Then HERE is never called`() = testApplication {
        val here = HereMockServer(HerePayloads.LOOKUP)
        application { module(here.asKoinModule()) }

        val response = client.lookup(placeId, providerKey = null)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorCode.MISSING_CREDENTIALS, response.decodeError().code)
        assertEquals(emptyList(), here.requests)
    }

    private suspend fun HttpClient.lookup(
        id: String,
        language: String? = "it-IT",
        providerKey: String? = TEST_PROVIDER_KEY,
    ): HttpResponse = get(
        NavigatorApi.HERE_SEARCH_PLACE_TEMPLATE.replace(
            "{${NavigatorApi.HERE_SEARCH_PLACE_ID_PARAMETER}}",
            id.encodeURLPathPart(),
        ),
    ) {
        language?.let { parameter(NavigatorApi.SEARCH_LANGUAGE_PARAMETER, it) }
        providerKey?.let { header(NavigatorApi.PROVIDER_KEY_HEADER, it) }
    }

    private suspend fun HttpResponse.decodePlaceDetails(): PlaceDetails =
        NavigatorJson.decodeFromString(PlaceDetails.serializer(), bodyAsText())
}
