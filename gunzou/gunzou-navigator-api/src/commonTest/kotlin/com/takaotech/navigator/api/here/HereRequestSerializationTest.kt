package com.takaotech.navigator.api.here

import com.takaotech.navigator.api.NavigatorJson
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.common.Units
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * The two request models are the asymmetric half of the contract: each provider profile gets its own
 * body, fully typed, and nothing about them is shared beyond the common value types.
 */
class HereRequestSerializationTest {

    @Test
    fun `Given a fully populated car request When encoding and decoding Then every field survives`() {
        val request = HereCarRouteRequest(
            origin = GeoPoint(lat = 44.4949, lng = 11.3426),
            destination = GeoPoint(lat = 43.7696, lng = 11.2558),
            via = listOf(GeoPoint(lat = 44.1391, lng = 11.1583)),
            transportMode = HereTransportMode.TRUCK,
            routingMode = HereRoutingMode.SHORT,
            alternatives = 3,
            time = RouteTime.DepartAt(Instant.parse("2026-08-12T07:30:00Z")),
            units = Units.IMPERIAL,
            language = "it-IT",
            avoid = HereAvoidOptions(features = listOf(HereAvoidFeature.TOLL_ROAD, HereAvoidFeature.FERRY)),
            returnAttributes = HereReturnAttribute.NAVIGATION_WITH_TOLLS,
        )

        val encoded = NavigatorJson.encodeToString(HereCarRouteRequest.serializer(), request)

        assertEquals(request, NavigatorJson.decodeFromString(HereCarRouteRequest.serializer(), encoded))
    }

    @Test
    fun `Given a car request with only mandatory fields When decoding Then the defaults are the documented ones`() {
        val request = HereCarRouteRequest(
            origin = GeoPoint(lat = 44.4949, lng = 11.3426),
            destination = GeoPoint(lat = 43.7696, lng = 11.2558),
        )

        val encoded = NavigatorJson.encodeToString(HereCarRouteRequest.serializer(), request)
        val decoded = NavigatorJson.decodeFromString(HereCarRouteRequest.serializer(), encoded)

        assertEquals(HereTransportMode.CAR, decoded.transportMode)
        assertEquals(HereRoutingMode.FAST, decoded.routingMode)
        assertEquals(1, decoded.alternatives)
        assertEquals(RouteTime.Now, decoded.time)
        assertEquals(Units.METRIC, decoded.units)
        assertEquals(emptyList(), decoded.via)
        assertEquals(HereReturnAttribute.NAVIGATION, decoded.returnAttributes)
    }

    @Test
    fun `Given a fully populated transit request When encoding and decoding Then every field survives`() {
        val request = HereTransitRouteRequest(
            origin = GeoPoint(lat = 44.4949, lng = 11.3426),
            destination = GeoPoint(lat = 44.5049, lng = 11.3526),
            alternatives = 2,
            time = RouteTime.ArriveBy(Instant.parse("2026-08-12T09:00:00Z")),
            modes = HereTransitModeFilter(
                include = listOf(TransitMode.SUBWAY, TransitMode.BUS),
                exclude = listOf(TransitMode.FLIGHT),
            ),
            changes = 2,
            pedestrianSpeedMetersPerSecond = 1.2,
            pedestrianMaxDistanceMeters = 1500,
            language = "it-IT",
            units = Units.METRIC,
        )

        val encoded = NavigatorJson.encodeToString(HereTransitRouteRequest.serializer(), request)

        assertEquals(request, NavigatorJson.decodeFromString(HereTransitRouteRequest.serializer(), encoded))
    }

    @Test
    fun `Given a transit request with only mandatory fields When decoding Then the optional filters stay absent`() {
        val request = HereTransitRouteRequest(
            origin = GeoPoint(lat = 44.4949, lng = 11.3426),
            destination = GeoPoint(lat = 44.5049, lng = 11.3526),
        )

        val encoded = NavigatorJson.encodeToString(HereTransitRouteRequest.serializer(), request)

        assertEquals(request, NavigatorJson.decodeFromString(HereTransitRouteRequest.serializer(), encoded))
    }

    @Test
    fun `Given a car request When encoding Then the enums travel as their declared names`() {
        val request = HereCarRouteRequest(
            origin = GeoPoint(lat = 1.0, lng = 2.0),
            destination = GeoPoint(lat = 3.0, lng = 4.0),
            transportMode = HereTransportMode.PRIVATE_BUS,
            avoid = HereAvoidOptions(features = listOf(HereAvoidFeature.CONTROLLED_ACCESS_HIGHWAY)),
        )

        val encoded = NavigatorJson.encodeToJsonElement(HereCarRouteRequest.serializer(), request).jsonObject

        assertEquals("PRIVATE_BUS", encoded["transportMode"]?.jsonPrimitive?.content)
        assertEquals(
            """{"features":["CONTROLLED_ACCESS_HIGHWAY"]}""",
            encoded["avoid"].toString(),
        )
    }
}
