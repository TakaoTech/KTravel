package com.takaotech.navigator.api.response

import com.takaotech.navigator.api.NavigatorJson
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.common.ZonedTime
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/**
 * [RouteResponse] is the symmetric half of the contract, the one shape every provider has to answer
 * in. The two tests that matter here are the two profiles: a road answer and a public transport one
 * must both fit, because if they do not, the server has no reason to exist.
 */
class RouteResponseSerializationTest {

    private val departure = Instant.parse("2026-08-12T07:30:00Z")

    private val roadResponse = RouteResponse(
        provider = ProviderId.HERE,
        profile = ProviderProfile.ROUTING,
        routes = listOf(
            RouteDto(
                summary = RouteSummaryDto(
                    durationSeconds = 4_200,
                    distanceMeters = 105_000,
                    baseDurationSeconds = 3_900,
                ),
                sections = listOf(
                    RouteSectionDto(
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

    private val transitResponse = RouteResponse(
        provider = ProviderId.HERE,
        profile = ProviderProfile.TRANSIT,
        routes = listOf(
            RouteDto(
                summary = RouteSummaryDto(durationSeconds = 1_500, distanceMeters = 6_400),
                sections = listOf(
                    RouteSectionDto(
                        summary = RouteSummaryDto(durationSeconds = 300, distanceMeters = 380),
                        mode = TravelMode.PEDESTRIAN,
                        geometry = RouteGeometry(PolylineEncoding.HERE_FLEXIBLE, "BFoz5xJ67i1B1B7P"),
                    ),
                    RouteSectionDto(
                        summary = RouteSummaryDto(durationSeconds = 1_200, distanceMeters = 6_020),
                        mode = TravelMode.TRANSIT,
                        departure = RouteWaypointDto(
                            place = GeoPoint(lat = 44.4949, lng = 11.3426),
                            time = ZonedTime(departure, 7_200),
                            name = "Bologna Centrale",
                        ),
                        transit = TransitDetailsDto(
                            mode = TransitMode.REGIONAL_TRAIN,
                            name = "R 2841",
                            category = "Regionale",
                            headsign = "Porretta Terme",
                            agency = TransitAgencyDto(name = "Trenitalia", id = "tper", website = "https://tper.it"),
                            color = "#008C45",
                            textColor = "#FFFFFF",
                            intermediateStops = listOf(
                                TransitStopDto(
                                    place = GeoPoint(lat = 44.48, lng = 11.32),
                                    name = "Borgo Panigale",
                                    arrival = ZonedTime(departure, 7_200),
                                    departure = ZonedTime(departure, 7_200),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )

    @Test
    fun `Given a road route with tolls and actions When encoding and decoding Then the response is unchanged`() {
        val encoded = NavigatorJson.encodeToString(RouteResponse.serializer(), roadResponse)

        assertEquals(roadResponse, NavigatorJson.decodeFromString(RouteResponse.serializer(), encoded))
    }

    @Test
    fun `Given a transit journey When encoding and decoding Then the response is unchanged`() {
        val encoded = NavigatorJson.encodeToString(RouteResponse.serializer(), transitResponse)

        assertEquals(transitResponse, NavigatorJson.decodeFromString(RouteResponse.serializer(), encoded))
    }

    @Test
    fun `Given a road section When encoding Then it carries no transit details`() {
        val encoded = NavigatorJson.encodeToString(RouteResponse.serializer(), roadResponse)
        val decoded = NavigatorJson.decodeFromString(RouteResponse.serializer(), encoded)

        assertNull(decoded.routes.single().sections.single().transit)
    }

    @Test
    fun `Given a transit journey When decoding Then the walking leg and the vehicle leg share one section type`() {
        val encoded = NavigatorJson.encodeToString(RouteResponse.serializer(), transitResponse)

        val sections = NavigatorJson.decodeFromString(RouteResponse.serializer(), encoded)
            .routes.single().sections

        assertEquals(listOf(TravelMode.PEDESTRIAN, TravelMode.TRANSIT), sections.map { it.mode })
        assertNull(sections.first().transit)
        assertEquals(TransitMode.REGIONAL_TRAIN, sections.last().transit?.mode)
    }

    @Test
    fun `Given a provider and a profile When encoding Then they travel as plain strings`() {
        val encoded = NavigatorJson.encodeToJsonElement(RouteResponse.serializer(), roadResponse).jsonObject

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

        val decoded = NavigatorJson.decodeFromString(RouteResponse.serializer(), json)

        assertEquals(ProviderId("an-engine-added-after-this-client-shipped"), decoded.provider)
        assertEquals(ProviderProfile("auto"), decoded.profile)
    }
}
