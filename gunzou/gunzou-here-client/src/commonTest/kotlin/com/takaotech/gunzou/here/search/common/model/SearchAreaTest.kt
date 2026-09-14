package com.takaotech.gunzou.here.search.common.model

import com.takaotech.gunzou.here.common.model.Coordinate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SearchAreaTest {

    @Test
    fun `Given several countries When converted Then they are listed after countryCode`() {
        assertEquals("countryCode:ITA,FRA", SearchArea.Countries(listOf("ITA", "FRA")).toQueryString())
    }

    @Test
    fun `Given a circle When converted Then the radius follows the centre`() {
        assertEquals("circle:52.53,13.38;r=10000", SearchArea.Circle(Coordinate(52.53, 13.38), 10_000).toQueryString())
    }

    @Test
    fun `Given a bounding box When converted Then the sides are in west south east north order`() {
        val box = SearchArea.BoundingBox(west = 13.08836, south = 52.33812, east = 13.761, north = 52.6755)

        assertEquals("bbox:13.08836,52.33812,13.761,52.6755", box.toQueryString())
    }

    @Test
    fun `Given invalid areas When they are built Then they are rejected`() {
        assertFailsWith<IllegalArgumentException> { SearchArea.Countries(emptyList()) }
        assertFailsWith<IllegalArgumentException> { SearchArea.Countries(listOf("ita")) }
        assertFailsWith<IllegalArgumentException> { SearchArea.Countries(listOf("IT")) }
        assertFailsWith<IllegalArgumentException> { SearchArea.Circle(Coordinate(0.0, 0.0), 0) }
        assertFailsWith<IllegalArgumentException> { SearchArea.BoundingBox(0.0, 10.0, 1.0, 5.0) }
    }
}
