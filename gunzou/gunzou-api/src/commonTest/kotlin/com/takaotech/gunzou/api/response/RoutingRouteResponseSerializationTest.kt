package com.takaotech.gunzou.api.response

import com.takaotech.gunzou.api.NavigatorJson
import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.common.ProviderId
import com.takaotech.gunzou.api.common.ProviderProfile
import com.takaotech.gunzou.api.common.TravelMode
import com.takaotech.gunzou.api.common.ZonedTime
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * [RoutingRouteResponse] is what every road engine answers in, whatever the vehicle.
 *
 * The fixture is deliberately the richest a road answer gets — manoeuvres, geometry, tolls with the
 * systems they refer to — because the encoding has to survive the whole of it, not the part a
 * screen happens to draw today.
 */
class RoutingRouteResponseSerializationTest {

    private val departure = Instant.parse("2026-08-12T07:30:00Z")

    private val response = RoutingRouteResponse(
        provider = ProviderId.HERE,
        profile = ProviderProfile.ROUTING,
        routes = listOf(
            RoutingRouteDto(
                summary = RouteSummaryDto(
                    durationSeconds = 4_200,
                    distanceMeters = 105_000,
                    baseDurationSeconds = 3_900,
                ),
                sections = listOf(
                    RoutingSectionDto(
                        summary = RouteSummaryDto(durationSeconds = 4_200, distanceMeters = 105_000),
                        mode = TravelMode.CAR,
                        actions = listOf(
                            RouteActionDto(
                                action = "depart",
                                durationSeconds = 0,
                                distanceMeters = 0,
                                instruction = "Head north on Via Rizzoli",
                                offset = 0,
                            ),
                            RouteActionDto(
                                action = "turn",
                                durationSeconds = 120,
                                distanceMeters = 1_400,
                                instruction = "Turn right onto the A1",
                                offset = 42,
                                direction = "right",
                                severity = "quite",
                            ),
                        ),
                        departure = RouteWaypointDto(
                            place = GeoPoint(lat = 44.4949, lng = 11.3426),
                            time = ZonedTime(instant = departure, offsetSeconds = 7_200),
                        ),
                        arrival = RouteWaypointDto(
                            place = GeoPoint(lat = 43.7696, lng = 11.2558),
                            time = ZonedTime(instant = departure, offsetSeconds = 7_200),
                        ),
                        geometry = RouteGeometry(
                            encoding = PolylineEncoding.HERE_FLEXIBLE,
                            value = "BFoz5xJ67i1B1B7PzIhaxL7Y",
                        ),
                        tollSystems = listOf(TollSystemDto(id = "autostrade", name = "Autostrade per l'Italia")),
                        tolls = listOf(
                            TollCostDto(
                                tollSystemRefs = listOf(0),
                                countryCode = "ITA",
                                fares = listOf(
                                    TollFareDto(
                                        price = TollPriceDto(currency = "EUR", minimum = 8.7, maximum = 8.7),
                                        name = "Class A",
                                        paymentMethods = listOf("cash", "transponder"),
                                    ),
                                ),
                                collectionLocations = listOf(GeoPoint(lat = 44.3, lng = 11.2)),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        notices = listOf(NoticeDto(code = "violatedAvoidTollRoad", severity = NoticeSeverity.CRITICAL)),
    )

    @Test
    fun `Given a road route with tolls and actions When encoding and decoding Then the response is unchanged`() {
        val encoded = NavigatorJson.encodeToString(RoutingRouteResponse.serializer(), response)

        assertEquals(response, NavigatorJson.decodeFromString(RoutingRouteResponse.serializer(), encoded))
    }

    @Test
    fun `Given a provider and a profile When encoding Then they travel as plain strings`() {
        val encoded = NavigatorJson.encodeToJsonElement(RoutingRouteResponse.serializer(), response).jsonObject

        assertEquals("here", encoded["provider"]?.jsonPrimitive?.content)
        assertEquals("routing", encoded["profile"]?.jsonPrimitive?.content)
    }

    @Test
    fun `Given a provider this build does not know When decoding Then it is read instead of rejected`() {
        val json = """
            {
              "provider": "an-engine-added-after-this-client-shipped",
              "profile": "auto",
              "routes": []
            }
        """.trimIndent()

        val decoded = NavigatorJson.decodeFromString(RoutingRouteResponse.serializer(), json)

        assertEquals(ProviderId("an-engine-added-after-this-client-shipped"), decoded.provider)
        assertEquals(ProviderProfile("auto"), decoded.profile)
    }

    @Test
    fun `Given an answer from a newer server When decoding Then the fields this build knows still arrive`() {
        val json = """
            {
              "provider": "here",
              "profile": "routing",
              "routes": [
                {
                  "summary": { "durationSeconds": 60, "distanceMeters": 900, "elevationGain": 12 },
                  "sections": [
                    {
                      "summary": { "durationSeconds": 60, "distanceMeters": 900 },
                      "mode": "CAR",
                      "surfaceQuality": "smooth"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val decoded = NavigatorJson.decodeFromString(RoutingRouteResponse.serializer(), json)

        assertEquals(900, decoded.routes.single().sections.single().summary.distanceMeters)
        assertEquals(TravelMode.CAR, decoded.routes.single().sections.single().mode)
    }
}
