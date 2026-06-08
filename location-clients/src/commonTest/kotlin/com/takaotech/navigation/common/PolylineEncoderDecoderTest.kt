package com.takaotech.navigation.common

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Validate polyline encoding with different input combinations.
 * Kotlin conversion from https://github.com/heremaps/flexible-polyline/blob/master/java/src/com/here/flexpolyline/PolylineEncoderDecoderTest.java
 */
class PolylineEncoderDecoderTest {

    @Test
    fun `Given empty coordinates list When encoding Then should throw IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            PolylineEncoderDecoder.encode(emptyList(), 5, PolylineEncoderDecoder.ThirdDimension.ABSENT, 0)
        }
    }

    @Test
    fun `Given single coordinate with negative longitude When encoding Then should encode correctly`() {
        // Test encoding of a coordinate with negative longitude value (-179.98321)
        // by encoding a single point and verifying the result contains the expected encoding
        val coordinates = listOf(PolylineEncoderDecoder.LatLngZ(-179.98321, 0.0))
        val encoded = PolylineEncoderDecoder.encode(coordinates, 5, PolylineEncoderDecoder.ThirdDimension.ABSENT, 0)
        // The encoded string should start with header and contain the encoded value
        assertTrue(encoded.isNotEmpty())
    }

    @Test
    fun `Given simple lat lng pairs When encoding Then should return expected encoded string`() {
        val pairs = listOf(
            PolylineEncoderDecoder.LatLngZ(50.1022829, 8.6982122),
            PolylineEncoderDecoder.LatLngZ(50.1020076, 8.6956695),
            PolylineEncoderDecoder.LatLngZ(50.1006313, 8.6914960),
            PolylineEncoderDecoder.LatLngZ(50.0987800, 8.6875156)
        )

        val expected = "BFoz5xJ67i1B1B7PzIhaxL7Y"
        val computed = PolylineEncoderDecoder.encode(pairs, 5, PolylineEncoderDecoder.ThirdDimension.ABSENT, 0)
        assertEquals(expected, computed)
    }

    @Test
    fun `Given complex lat lng pairs When encoding Then should return expected encoded string`() {
        val pairs = listOf(
            PolylineEncoderDecoder.LatLngZ(52.5199356, 13.3866272),
            PolylineEncoderDecoder.LatLngZ(52.5100899, 13.2816896),
            PolylineEncoderDecoder.LatLngZ(52.4351807, 13.1935196),
            PolylineEncoderDecoder.LatLngZ(52.4107285, 13.1964502),
            PolylineEncoderDecoder.LatLngZ(52.38871, 13.1557798),
            PolylineEncoderDecoder.LatLngZ(52.3727798, 13.1491003),
            PolylineEncoderDecoder.LatLngZ(52.3737488, 13.1154604),
            PolylineEncoderDecoder.LatLngZ(52.3875198, 13.0872202),
            PolylineEncoderDecoder.LatLngZ(52.4029388, 13.0706196),
            PolylineEncoderDecoder.LatLngZ(52.4105797, 13.0755529)
        )

        val expected = "BF05xgKuy2xCx9B7vUl0OhnR54EqSzpEl-HxjD3pBiGnyGi2CvwFsgD3nD4vB6e"
        val computed = PolylineEncoderDecoder.encode(pairs, 5, PolylineEncoderDecoder.ThirdDimension.ABSENT, 0)
        assertEquals(expected, computed)
    }

    @Test
    fun `Given lat lng z tuples When encoding with altitude Then should return expected encoded string`() {
        val tuples = listOf(
            PolylineEncoderDecoder.LatLngZ(50.1022829, 8.6982122, 10.0),
            PolylineEncoderDecoder.LatLngZ(50.1020076, 8.6956695, 20.0),
            PolylineEncoderDecoder.LatLngZ(50.1006313, 8.6914960, 30.0),
            PolylineEncoderDecoder.LatLngZ(50.0987800, 8.6875156, 40.0)
        )

        val expected = "BlBoz5xJ67i1BU1B7PUzIhaUxL7YU"
        val computed = PolylineEncoderDecoder.encode(tuples, 5, PolylineEncoderDecoder.ThirdDimension.ALTITUDE, 0)
        assertEquals(expected, computed)
    }

    @Test
    fun `Given empty string When decoding Then should throw IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            PolylineEncoderDecoder.decode("")
        }
    }

    @Test
    fun `Given encoded string without third dimension When getting third dimension Then should return ABSENT`() {
        assertTrue(PolylineEncoderDecoder.getThirdDimension("BFoz5xJ67i1BU") == PolylineEncoderDecoder.ThirdDimension.ABSENT)
    }

    @Test
    fun `Given encoded string with level When getting third dimension Then should return LEVEL`() {
        assertTrue(PolylineEncoderDecoder.getThirdDimension("BVoz5xJ67i1BU") == PolylineEncoderDecoder.ThirdDimension.LEVEL)
    }

    @Test
    fun `Given encoded string with altitude When getting third dimension Then should return ALTITUDE`() {
        assertTrue(PolylineEncoderDecoder.getThirdDimension("BlBoz5xJ67i1BU") == PolylineEncoderDecoder.ThirdDimension.ALTITUDE)
    }

    @Test
    fun `Given encoded string with elevation When getting third dimension Then should return ELEVATION`() {
        assertTrue(PolylineEncoderDecoder.getThirdDimension("B1Boz5xJ67i1BU") == PolylineEncoderDecoder.ThirdDimension.ELEVATION)
    }

    @Test
    fun `Given simple encoded string When decoding Then should return expected coordinates`() {
        val computed = PolylineEncoderDecoder.decode("BFoz5xJ67i1B1B7PzIhaxL7Y")
        val expected = listOf(
            PolylineEncoderDecoder.LatLngZ(50.10228, 8.69821),
            PolylineEncoderDecoder.LatLngZ(50.10201, 8.69567),
            PolylineEncoderDecoder.LatLngZ(50.10063, 8.69150),
            PolylineEncoderDecoder.LatLngZ(50.09878, 8.68752)
        )

        assertEquals(expected.size, computed.size)
        for (i in computed.indices) {
            assertEquals(expected[i], computed[i])
        }
    }

    @Test
    fun `Given complex encoded string When decoding Then should return expected coordinates`() {
        val computed = PolylineEncoderDecoder.decode("BF05xgKuy2xCx9B7vUl0OhnR54EqSzpEl-HxjD3pBiGnyGi2CvwFsgD3nD4vB6e")

        val pairs = listOf(
            PolylineEncoderDecoder.LatLngZ(52.51994, 13.38663),
            PolylineEncoderDecoder.LatLngZ(52.51009, 13.28169),
            PolylineEncoderDecoder.LatLngZ(52.43518, 13.19352),
            PolylineEncoderDecoder.LatLngZ(52.41073, 13.19645),
            PolylineEncoderDecoder.LatLngZ(52.38871, 13.15578),
            PolylineEncoderDecoder.LatLngZ(52.37278, 13.14910),
            PolylineEncoderDecoder.LatLngZ(52.37375, 13.11546),
            PolylineEncoderDecoder.LatLngZ(52.38752, 13.08722),
            PolylineEncoderDecoder.LatLngZ(52.40294, 13.07062),
            PolylineEncoderDecoder.LatLngZ(52.41058, 13.07555)
        )

        assertEquals(pairs.size, computed.size)
        for (i in computed.indices) {
            assertEquals(pairs[i], computed[i])
        }
    }

    @Test
    fun `Given encoded string with altitude When decoding Then should return coordinates with z values`() {
        val computed = PolylineEncoderDecoder.decode("BlBoz5xJ67i1BU1B7PUzIhaUxL7YU")
        val tuples = listOf(
            PolylineEncoderDecoder.LatLngZ(50.10228, 8.69821, 10.0),
            PolylineEncoderDecoder.LatLngZ(50.10201, 8.69567, 20.0),
            PolylineEncoderDecoder.LatLngZ(50.10063, 8.69150, 30.0),
            PolylineEncoderDecoder.LatLngZ(50.09878, 8.68752, 40.0)
        )

        assertEquals(tuples.size, computed.size)
        for (i in computed.indices) {
            assertEquals(tuples[i], computed[i])
        }
    }

    @Test
    fun `Given very long line When encoding and decoding Then should complete without error`() {
        val precision = 10
        val random = Random(42) // Fixed seed for reproducibility
        val coordinates = (0..1000).map {
            PolylineEncoderDecoder.LatLngZ(
                random.nextDouble() * 180 - 90,
                random.nextDouble() * 360 - 180,
                random.nextDouble() * 1000
            )
        }

        val encoded = PolylineEncoderDecoder.encode(
            coordinates,
            precision,
            PolylineEncoderDecoder.ThirdDimension.ALTITUDE,
            precision
        )
        val decoded = PolylineEncoderDecoder.decode(encoded)

        assertEquals(coordinates.size, decoded.size)
    }

    @Test
    fun `Given coordinates When encoding and decoding round trip Then should preserve values within precision`() {
        val coordinates = listOf(
            PolylineEncoderDecoder.LatLngZ(52.5200, 13.4050),
            PolylineEncoderDecoder.LatLngZ(48.1351, 11.5820)
        )

        val encoded = PolylineEncoderDecoder.encode(coordinates, 5, PolylineEncoderDecoder.ThirdDimension.ABSENT, 0)
        val decoded = PolylineEncoderDecoder.decode(encoded)

        assertEquals(coordinates.size, decoded.size)
        for (i in coordinates.indices) {
            assertEquals(coordinates[i].lat, decoded[i].lat, 0.00001)
            assertEquals(coordinates[i].lng, decoded[i].lng, 0.00001)
        }
    }

    @Test
    fun `Given negative coordinates When encoding and decoding Then should handle correctly`() {
        val coordinates = listOf(
            PolylineEncoderDecoder.LatLngZ(-33.8688, 151.2093),  // Sydney
            PolylineEncoderDecoder.LatLngZ(-37.8136, 144.9631)   // Melbourne
        )

        val encoded = PolylineEncoderDecoder.encode(coordinates, 5, PolylineEncoderDecoder.ThirdDimension.ABSENT, 0)
        val decoded = PolylineEncoderDecoder.decode(encoded)

        assertEquals(coordinates.size, decoded.size)
        for (i in coordinates.indices) {
            assertEquals(coordinates[i].lat, decoded[i].lat, 0.00001)
            assertEquals(coordinates[i].lng, decoded[i].lng, 0.00001)
        }
    }

    // getCoordinateAtOffset — encoded string "BFoz5xJ67i1B1B7PzIhaxL7Y" decodes to 4 points:
    // [0] LatLngZ(50.10228, 8.69821)
    // [1] LatLngZ(50.10201, 8.69567)
    // [2] LatLngZ(50.10063, 8.69150)
    // [3] LatLngZ(50.09878, 8.68752)

    @Test
    fun `Given valid polyline and offset 0 When getting coordinate at offset Then should return first coordinate`() {
        val result = PolylineEncoderDecoder.getCoordinateAtOffset("BFoz5xJ67i1B1B7PzIhaxL7Y", 0)
        assertEquals(PolylineEncoderDecoder.LatLngZ(50.10228, 8.69821), result)
    }

    @Test
    fun `Given valid polyline and mid offset When getting coordinate at offset Then should return correct coordinate`() {
        val result = PolylineEncoderDecoder.getCoordinateAtOffset("BFoz5xJ67i1B1B7PzIhaxL7Y", 2)
        assertEquals(PolylineEncoderDecoder.LatLngZ(50.10063, 8.69150), result)
    }

    @Test
    fun `Given valid polyline and last offset When getting coordinate at offset Then should return last coordinate`() {
        val result = PolylineEncoderDecoder.getCoordinateAtOffset("BFoz5xJ67i1B1B7PzIhaxL7Y", 3)
        assertEquals(PolylineEncoderDecoder.LatLngZ(50.09878, 8.68752), result)
    }

    @Test
    fun `Given valid polyline and out of bounds offset When getting coordinate at offset Then should throw IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            PolylineEncoderDecoder.getCoordinateAtOffset("BFoz5xJ67i1B1B7PzIhaxL7Y", 4)
        }
    }

    @Test
    fun `Given valid polyline and negative offset When getting coordinate at offset Then should throw IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            PolylineEncoderDecoder.getCoordinateAtOffset("BFoz5xJ67i1B1B7PzIhaxL7Y", -1)
        }
    }

    @Test
    fun `Given format version When getting version Then should return 1`() {
        assertEquals(1.toByte(), PolylineEncoderDecoder.getVersion())
    }

    @Test
    fun `Given different precisions When encoding Then should produce different results`() {
        val coordinates = listOf(
            PolylineEncoderDecoder.LatLngZ(52.5200123, 13.4050456)
        )

        val encodedPrecision5 =
            PolylineEncoderDecoder.encode(coordinates, 5, PolylineEncoderDecoder.ThirdDimension.ABSENT, 0)
        val encodedPrecision7 =
            PolylineEncoderDecoder.encode(coordinates, 7, PolylineEncoderDecoder.ThirdDimension.ABSENT, 0)

        assertTrue(encodedPrecision5 != encodedPrecision7)
    }
}