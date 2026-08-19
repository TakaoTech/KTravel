package com.takaotech.navigator.api.response

import com.takaotech.navigator.api.NavigatorJson
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.common.ZonedTime
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

/**
 * [TransitJourneyResponse] is what a timetable answers in.
 *
 * The point of these tests is the discriminator: a leg travels as `walk` or as `ride`, and that word
 * is the contract. Rename it and every client silently stops decoding journeys, which is exactly the
 * failure a round trip on its own cannot see — it would encode and decode the new word happily.
 */
class TransitJourneyResponseSerializationTest {

    private val boardsAt = Instant.parse("2026-08-12T07:30:00Z")
    private val alightsAt = Instant.parse("2026-08-12T07:50:00Z")

    private val response = TransitJourneyResponse(
        provider = ProviderId.HERE,
        profile = ProviderProfile.TRANSIT,
        journeys = listOf(
            TransitJourneyDto(
                summary = RouteSummaryDto(durationSeconds = 1_500, distanceMeters = 6_400),
                legs = listOf(
                    TransitJourneyLeg.Walk(
                        summary = RouteSummaryDto(durationSeconds = 300, distanceMeters = 380),
                        geometry = RouteGeometry(PolylineEncoding.HERE_FLEXIBLE, "BFoz5xJ67i1B1B7P"),
                        departure = RouteWaypointDto(
                            place = GeoPoint(lat = 44.4900, lng = 11.3400),
                            time = ZonedTime(boardsAt, 7_200),
                        ),
                        arrival = RouteWaypointDto(
                            place = GeoPoint(lat = 44.4949, lng = 11.3426),
                            time = ZonedTime(boardsAt, 7_200),
                            name = "Bologna Centrale",
                        ),
                        actions = listOf(
                            RouteActionDto(action = "depart", durationSeconds = 0, instruction = "Head north"),
                        ),
                    ),
                    TransitJourneyLeg.Ride(
                        summary = RouteSummaryDto(durationSeconds = 1_200, distanceMeters = 6_020),
                        line = TransitLineDto(
                            mode = TransitMode.REGIONAL_TRAIN,
                            name = "R 2841",
                            shortName = "R",
                            longName = "Bologna - Porretta Terme",
                            category = "Regionale",
                            headsign = "Porretta Terme",
                            color = "#008C45",
                            textColor = "#FFFFFF",
                            url = "https://example.test/lines/r-2841",
                            wheelchairAccessible = WheelchairAccess.LIMITED,
                        ),
                        agency = TransitAgencyDto(name = "Trenitalia", id = "tper", website = "https://example.test"),
                        boarding = TransitStopDto(
                            place = GeoPoint(lat = 44.4949, lng = 11.3426),
                            name = "Bologna Centrale",
                            departure = ZonedTime(boardsAt, 7_200),
                            wheelchairAccessible = WheelchairAccess.YES,
                        ),
                        alighting = TransitStopDto(
                            place = GeoPoint(lat = 44.1601, lng = 10.9739),
                            name = "Porretta Terme",
                            arrival = ZonedTime(alightsAt, 7_200),
                        ),
                        intermediateStops = listOf(
                            TransitStopDto(
                                place = GeoPoint(lat = 44.48, lng = 11.32),
                                name = "Borgo Panigale",
                                arrival = ZonedTime(boardsAt, 7_200),
                                departure = ZonedTime(boardsAt, 7_200),
                                dwellSeconds = 60,
                                offset = 23,
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )

    @Test
    fun `Given a journey that walks and rides When encoding and decoding Then the response is unchanged`() {
        val encoded = NavigatorJson.encodeToString(TransitJourneyResponse.serializer(), response)

        assertEquals(response, NavigatorJson.decodeFromString(TransitJourneyResponse.serializer(), encoded))
    }

    @Test
    fun `Given the two kinds of leg When encoding Then each carries the discriminator clients branch on`() {
        val legs = NavigatorJson.encodeToJsonElement(TransitJourneyResponse.serializer(), response)
            .jsonObject["journeys"]!!.jsonArray.single()
            .jsonObject["legs"]!!.jsonArray

        assertEquals(listOf("walk", "ride"), legs.map { it.jsonObject["type"]?.jsonPrimitive?.content })
    }

    @Test
    fun `Given a decoded journey When branching on its legs Then the two kinds are separate types`() {
        val encoded = NavigatorJson.encodeToString(TransitJourneyResponse.serializer(), response)

        val legs = NavigatorJson.decodeFromString(TransitJourneyResponse.serializer(), encoded)
            .journeys.single().legs

        val walk = assertIs<TransitJourneyLeg.Walk>(legs.first())
        val ride = assertIs<TransitJourneyLeg.Ride>(legs.last())
        assertEquals("Head north", walk.actions.single().instruction)
        assertEquals(TransitMode.REGIONAL_TRAIN, ride.line.mode)
        assertEquals("Trenitalia", ride.agency?.name)
        assertEquals(23, ride.intermediateStops.single().offset)
    }

    @Test
    fun `Given a journey When reading its ends Then every leg answers without the caller branching`() {
        val legs = response.journeys.single().legs

        assertEquals(ZonedTime(boardsAt, 7_200), legs.first().departureTime)
        assertEquals(ZonedTime(alightsAt, 7_200), legs.last().arrivalTime)
    }

    @Test
    fun `Given a feed that says nothing about access When decoding Then it is unknown and never a refusal`() {
        val json = """
            {
              "provider": "here",
              "profile": "transit",
              "journeys": [
                {
                  "summary": { "durationSeconds": 600, "distanceMeters": 4000 },
                  "legs": [
                    {
                      "type": "ride",
                      "summary": { "durationSeconds": 600, "distanceMeters": 4000 },
                      "line": { "mode": "SUBWAY", "name": "M2" },
                      "platform": "a field this build has never heard of"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val ride = assertIs<TransitJourneyLeg.Ride>(
            NavigatorJson.decodeFromString(TransitJourneyResponse.serializer(), json)
                .journeys.single().legs.single(),
        )

        assertEquals(WheelchairAccess.UNKNOWN, ride.line.wheelchairAccessible)
        assertEquals(TransitMode.SUBWAY, ride.line.mode)
    }
}
