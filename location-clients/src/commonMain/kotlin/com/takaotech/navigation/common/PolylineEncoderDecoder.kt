package com.takaotech.navigation.common

import com.takaotech.navigation.common.PolylineEncoderDecoder.getThirdDimension
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign

/**
 * The polyline encoding is a lossy compressed representation of a list of coordinate pairs or coordinate triples.
 * It achieves that by:
 *
 * 1. Reducing the decimal digits of each value.
 * 2. Encoding only the offset from the previous point.
 * 3. Using variable length for each coordinate delta.
 * 4. Using 64 URL-safe characters to display the result.
 *
 * The advantage of this encoding are the following:
 * - Output string is composed by only URL-safe characters
 * - Floating point precision is configurable
 * - It allows to encode a 3rd dimension with a given precision, which may be a level, altitude, elevation or some other custom value
 *
 * Kotlin conversion from https://github.com/heremaps/flexible-polyline/blob/master/java/src/com/here/flexpolyline/PolylineEncoderDecoderTest.java
 */
object PolylineEncoderDecoder {

    /**
     * Header version
     * A change in the version may affect the logic to encode and decode the rest of the header and data
     */
    const val FORMAT_VERSION: Byte = 1

    // Base64 URL-safe characters
    val ENCODING_TABLE: CharArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray()

    val DECODING_TABLE: IntArray = intArrayOf(
        62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
        22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    )

    /**
     * Encode the list of coordinate triples.
     *
     * The third dimension value will be eligible for encoding only when ThirdDimension is other than ABSENT.
     * This is lossy compression based on precision accuracy.
     *
     * @param coordinates List of coordinate triples that to be encoded.
     * @param precision Floating point precision of the coordinate to be encoded.
     * @param thirdDimension [ThirdDimension] which may be a level, altitude, elevation or some other custom value
     * @param thirdDimPrecision Floating point precision for thirdDimension value
     * @return URL-safe encoded String for the given coordinates.
     */
    fun encode(
        coordinates: List<LatLngZ>,
        precision: Int,
        thirdDimension: ThirdDimension,
        thirdDimPrecision: Int
    ): String {
        require(coordinates.isNotEmpty()) { "Invalid coordinates!" }
        val enc = Encoder(precision, thirdDimension, thirdDimPrecision)
        for (coord in coordinates) {
            enc.add(coord)
        }
        return enc.getEncoded()
    }

    /**
     * Decode the encoded input String to List of coordinate triples.
     *
     * @param encoded URL-safe encoded String
     * @return List of coordinate triples that are decoded from input
     *
     * @see getThirdDimension
     * @see LatLngZ
     */
    fun decode(encoded: String): List<LatLngZ> {
        require(encoded.trim().isNotEmpty()) { "Invalid argument!" }
        val result = mutableListOf<LatLngZ>()
        val dec = Decoder(encoded)

        var coord: LatLngZ?
        while (dec.decodeOne().also { coord = it } != null) {
            result.add(coord!!)
        }
        return result
    }

    /**
     * ThirdDimension type from the encoded input String
     * @param encoded URL-safe encoded coordinate triples String
     * @return type of [ThirdDimension]
     */
    fun getThirdDimension(encoded: String): ThirdDimension {
        return Decoder(encoded).thirdDimension
    }

    fun getVersion(): Byte = FORMAT_VERSION

    // Decode a single char to the corresponding value
    private fun decodeChar(charValue: Char): Int {
        val pos = charValue.code - 45
        if (pos < 0 || pos > 77) {
            return -1
        }
        return DECODING_TABLE[pos]
    }

    /**
     * Single instance for configuration, validation and encoding for an input request.
     */
    private class Encoder(
        precision: Int,
        private val thirdDimension: ThirdDimension,
        thirdDimPrecision: Int
    ) {
        private val result = StringBuilder()
        private val latConverter = Converter(precision)
        private val lngConverter = Converter(precision)
        private val zConverter = Converter(thirdDimPrecision)

        init {
            encodeHeader(precision, thirdDimension.num, thirdDimPrecision)
        }

        private fun encodeHeader(precision: Int, thirdDimensionValue: Int, thirdDimPrecision: Int) {
            /*
             * Encode the `precision`, `third_dim` and `third_dim_precision` into one encoded char
             */
            require(precision in 0..15) { "precision out of range" }
            require(thirdDimPrecision in 0..15) { "thirdDimPrecision out of range" }
            require(thirdDimensionValue in 0..7) { "thirdDimensionValue out of range" }

            val res = (thirdDimPrecision shl 7) or (thirdDimensionValue shl 4) or precision
            Converter.encodeUnsignedVarint(
                FORMAT_VERSION.toLong(),
                result
            )
            Converter.encodeUnsignedVarint(
                res.toLong(),
                result
            )
        }

        private fun add(lat: Double, lng: Double) {
            latConverter.encodeValue(lat, result)
            lngConverter.encodeValue(lng, result)
        }

        private fun add(lat: Double, lng: Double, z: Double) {
            add(lat, lng)
            if (thirdDimension != ThirdDimension.ABSENT) {
                zConverter.encodeValue(z, result)
            }
        }

        fun add(tuple: LatLngZ) {
            add(tuple.lat, tuple.lng, tuple.z)
        }

        fun getEncoded(): String = result.toString()
    }

    /**
     * Single instance for decoding an input request.
     */
    private class Decoder(encoded: String) {
        private val iterator = StringIterator(encoded)
        private val latConverter: Converter
        private val lngConverter: Converter
        private val zConverter: Converter
        val thirdDimension: ThirdDimension

        init {
            val header = decodeHeader()
            val precision = header and 0x0f
            thirdDimension =
                ThirdDimension.fromNum(
                    (header shr 4) and 0x07
                )
                    ?: throw IllegalArgumentException("Invalid third dimension value")
            val thirdDimPrecision = (header shr 7) and 0x0f
            latConverter = Converter(precision)
            lngConverter = Converter(precision)
            zConverter = Converter(thirdDimPrecision)
        }

        private fun hasThirdDimension(): Boolean = thirdDimension != ThirdDimension.ABSENT

        private fun decodeHeader(): Int {
            val version =
                Converter.decodeUnsignedVarint(
                    iterator
                )
            require(version == FORMAT_VERSION.toLong()) { "Invalid format version" }

            // Decode the polyline header
            return Converter.decodeUnsignedVarint(
                iterator
            ).toInt()
        }

        fun decodeOne(): LatLngZ? {
            if (iterator.isDone()) {
                return null
            }

            val lat = latConverter.decodeValue(iterator)
            val lng = lngConverter.decodeValue(iterator)

            return if (hasThirdDimension()) {
                val z = zConverter.decodeValue(iterator)
                LatLngZ(lat, lng, z)
            } else {
                LatLngZ(lat, lng)
            }
        }
    }

    /**
     * Simple string iterator for multiplatform compatibility
     */
    private class StringIterator(private val str: String) {
        private var index = 0

        fun current(): Char? = if (index < str.length) str[index] else null

        fun next(): Char? {
            index++
            return current()
        }

        fun isDone(): Boolean = index >= str.length

        fun getIndex(): Int = index
    }

    /**
     * Stateful instance for encoding and decoding on a sequence of Coordinates part of an request.
     * Instance should be specific to type of coordinates (e.g. Lat, Lng)
     * so that specific type delta is computed for encoding.
     * Lat0 Lng0 3rd0 (Lat1-Lat0) (Lng1-Lng0) (3rdDim1-3rdDim0)
     */
    private class Converter(precision: Int) {
        private val multiplier: Long = 10.0.pow(precision).toLong()
        private var lastValue: Long = 0

        fun encodeValue(value: Double, result: StringBuilder) {
            /*
             * Round-half-up
             * round(-1.4) --> -1
             * round(-1.5) --> -2
             * round(-2.5) --> -3
             */
            val scaledValue = round(abs(value * multiplier)) * sign(value)
            var delta = scaledValue.toLong() - lastValue
            val negative = delta < 0

            lastValue = scaledValue.toLong()

            // make room on lowest bit
            delta = delta shl 1

            // invert bits if the value is negative
            if (negative) {
                delta = delta.inv()
            }
            encodeUnsignedVarint(
                delta,
                result
            )
        }

        fun decodeValue(iterator: StringIterator): Double {
            var l =
                decodeUnsignedVarint(
                    iterator
                )
            if ((l and 1L) != 0L) {
                l = l.inv()
            }
            l = l shr 1
            lastValue += l

            return lastValue.toDouble() / multiplier
        }

        companion object {
            fun encodeUnsignedVarint(value: Long, result: StringBuilder) {
                var v = value
                while (v > 0x1F) {
                    val pos = ((v and 0x1F) or 0x20).toInt()
                    result.append(ENCODING_TABLE[pos])
                    v = v shr 5
                }
                result.append(ENCODING_TABLE[v.toInt()])
            }

            fun decodeUnsignedVarint(iterator: StringIterator): Long {
                var shift = 0
                var result = 0L

                var c = iterator.current()
                while (c != null) {
                    iterator.next()
                    val value = decodeChar(c).toLong()
                    require(value >= 0) { "Unexpected value found '$c' at index ${iterator.getIndex()}" }
                    result = result or ((value and 0x1F) shl shift)
                    if ((value and 0x20) == 0L) {
                        return result
                    } else {
                        shift += 5
                    }
                    c = iterator.current()
                }
                throw IllegalArgumentException("Unexpected end of encoded string")
            }
        }
    }

    /**
     * 3rd dimension specification.
     * Example a level, altitude, elevation or some other custom value.
     * ABSENT is default when there is no third dimension en/decoding required.
     */
    enum class ThirdDimension(val num: Int) {
        ABSENT(0),
        LEVEL(1),
        ALTITUDE(2),
        ELEVATION(3),
        RESERVED1(4),
        RESERVED2(5),
        CUSTOM1(6),
        CUSTOM2(7);

        companion object {
            fun fromNum(value: Int): ThirdDimension? {
                return entries.find { it.num == value }
            }
        }
    }

    /**
     * Coordinate triple
     */
    data class LatLngZ(
        val lat: Double,
        val lng: Double,
        val z: Double = 0.0
    ) {
        override fun toString(): String = "LatLngZ [lat=$lat, lng=$lng, z=$z]"
    }
}