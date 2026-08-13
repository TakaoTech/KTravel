package com.takaotech.navigator.api.common

import com.takaotech.navigator.api.NavigatorJson
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * [RouteTime] is the only sealed hierarchy on the wire, so its discriminator is the only place where
 * a rename would silently change the contract: the Kotlin code would still compile on both sides and
 * only the JSON between them would stop matching.
 */
class RouteTimeTest {

    private val instant = Instant.parse("2026-08-12T07:30:00Z")

    @Test
    fun `Given a departure now When encoding Then the discriminator is now and no instant is written`() {
        val encoded = NavigatorJson.encodeToJsonElement(RouteTime.serializer(), RouteTime.Now).jsonObject

        assertEquals("now", encoded["type"]?.jsonPrimitive?.content)
        assertEquals(setOf("type"), encoded.keys)
    }

    @Test
    fun `Given a departure at a given instant When encoding Then the discriminator is departAt`() {
        val encoded = NavigatorJson
            .encodeToJsonElement(RouteTime.serializer(), RouteTime.DepartAt(instant))
            .jsonObject

        assertEquals("departAt", encoded["type"]?.jsonPrimitive?.content)
        assertEquals("2026-08-12T07:30:00Z", encoded["instant"]?.jsonPrimitive?.content)
    }

    @Test
    fun `Given an arrival by a given instant When encoding Then the discriminator is arriveBy`() {
        val encoded = NavigatorJson
            .encodeToJsonElement(RouteTime.serializer(), RouteTime.ArriveBy(instant))
            .jsonObject

        assertEquals("arriveBy", encoded["type"]?.jsonPrimitive?.content)
        assertEquals("2026-08-12T07:30:00Z", encoded["instant"]?.jsonPrimitive?.content)
    }

    @Test
    fun `Given each route time When encoding and decoding Then the value is unchanged`() {
        val cases = listOf(RouteTime.Now, RouteTime.DepartAt(instant), RouteTime.ArriveBy(instant))

        cases.forEach { time ->
            val encoded = NavigatorJson.encodeToString(RouteTime.serializer(), time)

            assertEquals(time, NavigatorJson.decodeFromString(RouteTime.serializer(), encoded))
        }
    }

    @Test
    fun `Given a hand written departAt payload When decoding Then it reads as the expected variant`() {
        val json = """{ "type": "departAt", "instant": "2026-08-12T07:30:00Z" }"""

        val decoded = NavigatorJson.decodeFromString(RouteTime.serializer(), json)

        assertEquals(RouteTime.DepartAt(instant), decoded)
    }
}
