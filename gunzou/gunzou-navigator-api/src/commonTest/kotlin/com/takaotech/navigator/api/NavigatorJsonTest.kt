package com.takaotech.navigator.api

import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereTransportMode
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The three settings of [NavigatorJson] are what let a server and a client of different vintages
 * keep talking to each other, so each one is pinned by a test: they are easy to drop in a refactor
 * and the damage only shows up in production, against a client nobody is rebuilding.
 */
class NavigatorJsonTest {

    private val request = HereCarRouteRequest(
        origin = GeoPoint(lat = 44.4949, lng = 11.3426),
        destination = GeoPoint(lat = 43.7696, lng = 11.2558),
    )

    @Test
    fun `Given a payload with unknown keys When decoding Then the known fields are read and the rest ignored`() {
        val json = """
            {
              "origin": { "lat": 44.4949, "lng": 11.3426, "elevation": 54.0 },
              "destination": { "lat": 43.7696, "lng": 11.2558 },
              "transportMode": "CAR",
              "somethingAddedByANewerServer": { "nested": [1, 2, 3] }
            }
        """.trimIndent()

        val decoded = NavigatorJson.decodeFromString<HereCarRouteRequest>(json)

        assertEquals(GeoPoint(lat = 44.4949, lng = 11.3426), decoded.origin)
        assertEquals(HereTransportMode.CAR, decoded.transportMode)
    }

    @Test
    fun `Given an optional field left unset When encoding Then the key is omitted instead of written as null`() {
        val encoded = NavigatorJson.encodeToJsonElement(HereCarRouteRequest.serializer(), request).jsonObject

        assertFalse(encoded.containsKey("language"))
        assertFalse(encoded.containsKey("avoid"))
    }

    @Test
    fun `Given a payload without an optional field When decoding Then it reads as null`() {
        val json = """{ "origin": { "lat": 1.0, "lng": 2.0 }, "destination": { "lat": 3.0, "lng": 4.0 } }"""

        val decoded = NavigatorJson.decodeFromString<HereCarRouteRequest>(json)

        assertNull(decoded.language)
        assertNull(decoded.avoid)
    }

    @Test
    fun `Given a request that relies on its defaults When encoding Then the defaults are written out explicitly`() {
        val encoded = NavigatorJson.encodeToJsonElement(HereCarRouteRequest.serializer(), request).jsonObject

        assertEquals("CAR", encoded["transportMode"]?.jsonPrimitive?.content)
        assertEquals("FAST", encoded["routingMode"]?.jsonPrimitive?.content)
        assertEquals("1", encoded["alternatives"]?.jsonPrimitive?.content)
        assertTrue(encoded.containsKey("time"))
        assertTrue(encoded.containsKey("returnAttributes"))
    }
}
