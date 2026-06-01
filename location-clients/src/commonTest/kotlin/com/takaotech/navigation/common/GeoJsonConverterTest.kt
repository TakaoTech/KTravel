package com.takaotech.navigation.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeoJsonConverterTest {

    @Test
    fun `Given known encoded polyline When converting to GeoJSON Then output is a valid Feature with LineString geometry`() {
        val encoded = "BFoz5xJ67i1B1B7PzIhaxL7Y"

        val geoJson = GeoJsonConverter.polylineToGeoJson(encoded)

        val root = Json.parseToJsonElement(geoJson).jsonObject
        assertEquals("Feature", root["type"]?.jsonPrimitive?.content)
        val geometry = root["geometry"]!!.jsonObject
        assertEquals("LineString", geometry["type"]?.jsonPrimitive?.content)
    }

    @Test
    fun `Given known encoded polyline When converting to GeoJSON Then coordinates are in longitude latitude order`() {
        val encoded = "BFoz5xJ67i1B1B7PzIhaxL7Y"
        val expectedLngLat = listOf(
            listOf(8.69821, 50.10228),
            listOf(8.69567, 50.10201),
            listOf(8.69150, 50.10063),
            listOf(8.68752, 50.09878)
        )

        val geoJson = GeoJsonConverter.polylineToGeoJson(encoded)

        val coords = Json.parseToJsonElement(geoJson)
            .jsonObject["geometry"]!!
            .jsonObject["coordinates"]!!
            .jsonArray

        assertEquals(expectedLngLat.size, coords.size)
        coords.forEachIndexed { i, elem ->
            val pair = elem.jsonArray
            assertEquals(expectedLngLat[i][0], pair[0].jsonPrimitive.content.toDouble(), 0.00001)
            assertEquals(expectedLngLat[i][1], pair[1].jsonPrimitive.content.toDouble(), 0.00001)
        }
    }

    @Test
    fun `Given empty string When converting to GeoJSON Then propagates IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            GeoJsonConverter.polylineToGeoJson("")
        }
    }

    @Test
    fun `Given two valid polylines When merging Then coordinate count equals sum of both`() {
        val first = "BFoz5xJ67i1B1B7PzIhaxL7Y"   // 4 points
        val second = "BF05xgKuy2xCx9B7vUl0OhnR54EqSzpEl-HxjD3pBiGnyGi2CvwFsgD3nD4vB6e" // 10 points

        val geoJson = GeoJsonConverter.mergePolylinesToGeoJson(listOf(first, second))

        val coords = Json.parseToJsonElement(geoJson)
            .jsonObject["geometry"]!!
            .jsonObject["coordinates"]!!
            .jsonArray
        assertEquals(14, coords.size)
    }

    @Test
    fun `Given two valid polylines When merging Then coordinates from first polyline precede those from second`() {
        val first = "BFoz5xJ67i1B1B7PzIhaxL7Y"
        val second = "BF05xgKuy2xCx9B7vUl0OhnR54EqSzpEl-HxjD3pBiGnyGi2CvwFsgD3nD4vB6e"
        val firstExpectedFirstLng = 8.69821
        val secondExpectedFirstLng = 13.38663

        val geoJson = GeoJsonConverter.mergePolylinesToGeoJson(listOf(first, second))

        val coords = Json.parseToJsonElement(geoJson)
            .jsonObject["geometry"]!!
            .jsonObject["coordinates"]!!
            .jsonArray
        assertEquals(
            firstExpectedFirstLng,
            coords[0].jsonArray[0].jsonPrimitive.content.toDouble(),
            0.00001
        )
        assertEquals(
            secondExpectedFirstLng,
            coords[4].jsonArray[0].jsonPrimitive.content.toDouble(),
            0.00001
        )
    }

    @Test
    fun `Given empty list When merging polylines Then throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            GeoJsonConverter.mergePolylinesToGeoJson(emptyList())
        }
    }
}
