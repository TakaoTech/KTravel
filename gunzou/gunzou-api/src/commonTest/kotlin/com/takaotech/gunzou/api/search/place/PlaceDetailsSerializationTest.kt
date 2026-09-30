package com.takaotech.gunzou.api.search.place

import com.takaotech.gunzou.api.NavigatorJson
import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.search.PlaceCategoryGroup
import com.takaotech.gunzou.api.search.SearchAddress
import com.takaotech.gunzou.api.search.SearchResultType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaceDetailsSerializationTest {

    @Test
    fun `Given complete place details When encoding and decoding Then the answer is unchanged`() {
        val details = PlaceDetails(
            id = "here:pds:place:380sr2yk-0f0d8a4b5e0b4b1f9f",
            title = "Colosseo",
            resultType = SearchResultType.PLACE,
            position = GeoPoint(lat = 41.8902, lng = 12.4922),
            address = SearchAddress(label = "Piazza del Colosseo, 00184 Roma RM, Italia", countryCode = "IT"),
            category = PlaceCategoryGroup.SIGHTS_AND_MUSEUMS,
            contacts = listOf(
                PlaceContact(kind = ContactKind.PHONE, value = "+39 06 3996 7700"),
                PlaceContact(kind = ContactKind.WEBSITE, value = "https://colosseo.it", label = "Official"),
            ),
            openingHours = listOf(
                OpeningPeriod(days = listOf(Weekday.MONDAY, Weekday.TUESDAY), opensAt = "08:30", durationMinutes = 600),
            ),
            timeZone = PlaceTimeZone(name = "Europe/Rome", utcOffset = "+02:00"),
            permanentClosure = PermanentClosure.POSSIBLE,
        )

        val encoded = NavigatorJson.encodeToString(PlaceDetails.serializer(), details)

        assertEquals(details, NavigatorJson.decodeFromString(PlaceDetails.serializer(), encoded))
    }

    @Test
    fun `Given only the required fields When decoding Then the details are empty rather than missing`() {
        val json = """
            {
              "id": "here:af:street:1",
              "title": "Via del Corso",
              "resultType": "street",
              "position": { "lat": 41.9, "lng": 12.48 }
            }
        """.trimIndent()

        val details = NavigatorJson.decodeFromString(PlaceDetails.serializer(), json)

        assertEquals(SearchAddress(), details.address)
        assertEquals(emptyList(), details.contacts)
        assertEquals(emptyList(), details.openingHours)
        assertNull(details.timeZone)
        assertNull(details.permanentClosure)
    }

    @Test
    fun `Given vocabulary from a newer server When decoding Then the unknown values are kept`() {
        val json = """
            {
              "id": "here:pds:place:1",
              "title": "Bar",
              "resultType": "place",
              "position": { "lat": 41.9, "lng": 12.48 },
              "contacts": [ { "kind": "fax", "value": "+39 06 1" } ],
              "openingHours": [ { "days": [ "holiday" ], "opensAt": "18:00", "durationMinutes": 480 } ],
              "permanentClosure": "temporary"
            }
        """.trimIndent()

        val details = NavigatorJson.decodeFromString(PlaceDetails.serializer(), json)

        assertEquals(ContactKind("fax"), details.contacts.single().kind)
        assertEquals(listOf(Weekday("holiday")), details.openingHours.single().days)
        assertEquals(PermanentClosure("temporary"), details.permanentClosure)
    }
}
