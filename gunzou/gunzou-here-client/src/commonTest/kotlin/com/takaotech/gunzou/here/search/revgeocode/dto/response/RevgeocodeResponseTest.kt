package com.takaotech.gunzou.here.search.revgeocode.dto.response

import com.takaotech.gunzou.here.common.hereApiJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RevgeocodeResponseTest {

    @Test
    fun `Given a house number result When the response is decoded Then its sections are read`() {
        val response = hereApiJson.decodeFromString<RevgeocodeResponse>(SAMPLE_RESPONSE)

        val item = response.items.single()
        assertEquals("houseNumber", item.resultType)
        assertEquals("PA", item.houseNumberType)
        assertEquals("Berlin", item.address.city)
        assertEquals("6", item.address.houseNumber)
        assertEquals(52.53086, item.position?.lat)
        assertEquals(12L, item.distance)
        assertEquals("DE", item.countryInfo?.alpha2)
        assertEquals("Europe/Berlin", item.timeZone?.name)
        assertEquals("Invalidenstraße", item.streetInfo?.single()?.baseName)
        assertNull(item.houseNumberFallback)
    }

    @Test
    fun `Given no result When the response is decoded Then the list is empty`() {
        val response = hereApiJson.decodeFromString<RevgeocodeResponse>("""{"items":[]}""")

        assertEquals(emptyList(), response.items)
    }

    private companion object {
        val SAMPLE_RESPONSE = """
            {
              "items": [
                {
                  "title": "Invalidenstraße 116, 10115 Berlin, Deutschland",
                  "id": "here:af:streetsection:tVuvjJYhVLJZeK7ttzUXHC:CggIBCCi-9SPARABGgMxMTY",
                  "resultType": "houseNumber",
                  "houseNumberType": "PA",
                  "address": {
                    "label": "Invalidenstraße 116, 10115 Berlin, Deutschland",
                    "countryCode": "DEU",
                    "city": "Berlin",
                    "street": "Invalidenstraße",
                    "postalCode": "10115",
                    "houseNumber": "6"
                  },
                  "position": {"lat": 52.53086, "lng": 13.38469},
                  "access": [{"lat": 52.53077, "lng": 13.38467}],
                  "distance": 12,
                  "mapView": {"west": 13.38, "south": 52.53, "east": 13.39, "north": 52.54},
                  "timeZone": {"name": "Europe/Berlin", "utcOffset": "+02:00"},
                  "streetInfo": [{"baseName": "Invalidenstraße", "language": "de"}],
                  "countryInfo": {"alpha2": "DE", "alpha3": "DEU"},
                  "mapReferences": {"segments": [{"ref": "unread"}]},
                  "navigationAttributes": {"functionalClass": [{"value": 4}]}
                }
              ]
            }
        """.trimIndent()
    }
}
