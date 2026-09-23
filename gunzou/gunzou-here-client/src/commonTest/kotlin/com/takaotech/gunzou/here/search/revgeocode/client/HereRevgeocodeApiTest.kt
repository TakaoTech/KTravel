package com.takaotech.gunzou.here.search.revgeocode.client

import com.takaotech.gunzou.here.HereClient
import com.takaotech.gunzou.here.common.HereApiResult
import com.takaotech.gunzou.here.common.HereClientConfig
import com.takaotech.gunzou.here.common.model.Coordinate
import com.takaotech.gunzou.here.search.revgeocode.dto.request.RevgeocodeRequest
import com.takaotech.gunzou.here.search.revgeocode.dto.response.RevgeocodeResponse
import com.takaotech.gunzou.here.search.revgeocode.model.RevgeocodeFeature
import com.takaotech.gunzou.here.search.revgeocode.model.RevgeocodeShowOption
import com.takaotech.gunzou.here.search.revgeocode.model.RevgeocodeType
import com.vanniktech.locale.Locale
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class HereRevgeocodeApiTest {

    @Test
    fun `Given a full request When revgeocode is called Then every parameter reaches the query string`() = runTest {
        var captured: HttpRequestData? = null
        val engine = MockEngine { request ->
            captured = request
            respond(
                content = """{"items":[{"title":"Berlin","id":"x","address":{"city":"Berlin"}}]}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val result = HereClient.withEngine(engine, HereClientConfig(apiKey = "key")).use { here ->
            here.revgeocode.revgeocode(
                RevgeocodeRequest(
                    at = Coordinate(52.5308, 13.3856),
                    types = listOf(RevgeocodeType.ADDRESS, RevgeocodeType.PLACE),
                    features = listOf(RevgeocodeFeature.UNNAMED_STREETS),
                    lang = listOf(Locale.from("it-IT")),
                    limit = 3,
                    show = listOf(RevgeocodeShowOption.COUNTRY_INFO, RevgeocodeShowOption.TIME_ZONE),
                    requestId = "request-1",
                ),
            )
        }

        val success = assertIs<HereApiResult.Success<RevgeocodeResponse>>(result)
        assertEquals("Berlin", success.data.items.single().address.city)
        val request = checkNotNull(captured)
        assertEquals("revgeocode.search.hereapi.com", request.url.host)
        assertEquals("/v1/revgeocode", request.url.encodedPath)
        val query = request.url.parameters
        assertEquals("52.5308,13.3856", query["at"])
        assertNull(query["in"])
        assertEquals("address,place", query["types"])
        assertEquals("unnamedStreets", query["with"])
        assertEquals("3", query["limit"])
        assertEquals("countryInfo,tz", query["show"])
        assertEquals("request-1", request.headers["X-Request-ID"])
    }
}
