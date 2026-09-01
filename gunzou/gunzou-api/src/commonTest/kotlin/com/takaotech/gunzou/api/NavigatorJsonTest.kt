package com.takaotech.gunzou.api

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.here.HereRoutingRequest
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

    private val request = HereRoutingRequest(
        origin = GeoPoint(lat = 44.4949, lng = 11.3426),
        destination = GeoPoint(lat = 43.7696, lng = 11.2558),
    )

    @Test
    fun `Given a payload with unknown keys When decoding Then the known fields are read and the rest ignored`() {
        val json = """
            {
              "origin": { "lat": 44.4949, "lng": 11.3426, "elevation": 54.0 },
              "destination": { "lat": 43.7696, "lng": 11.2558 },
              "language": "it-IT",
              "somethingAddedByANewerServer": { "nested": [1, 2, 3] }
            }
        """.trimIndent()

        val decoded = NavigatorJson.decodeFromString<HereRoutingRequest>(json)

        assertEquals(GeoPoint(lat = 44.4949, lng = 11.3426), decoded.origin)
        assertEquals("it-IT", decoded.language)
    }

    @Test
    fun `Given an optional field left unset When encoding Then the key is omitted instead of written as null`() {
        val encoded = NavigatorJson.encodeToJsonElement(HereRoutingRequest.serializer(), request).jsonObject

        assertFalse(encoded.containsKey("language"))
        assertFalse(encoded.containsKey("avoid"))
    }

    @Test
    fun `Given a payload without an optional field When decoding Then it reads as null`() {
        val json = """{ "origin": { "lat": 1.0, "lng": 2.0 }, "destination": { "lat": 3.0, "lng": 4.0 } }"""

        val decoded = NavigatorJson.decodeFromString<HereRoutingRequest>(json)

        assertNull(decoded.language)
        assertNull(decoded.avoid)
    }

    @Test
    fun `Given a request that relies on its defaults When encoding Then the defaults are written out explicitly`() {
        val encoded = NavigatorJson.encodeToJsonElement(HereRoutingRequest.serializer(), request).jsonObject

        assertEquals("FAST", encoded["routingMode"]?.jsonPrimitive?.content)
        assertEquals("1", encoded["alternatives"]?.jsonPrimitive?.content)
        assertTrue(encoded.containsKey("time"))
        assertTrue(encoded.containsKey("returnAttributes"))
    }
}
