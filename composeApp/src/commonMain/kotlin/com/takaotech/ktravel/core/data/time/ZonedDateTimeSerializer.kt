package com.takaotech.ktravel.core.data.time

import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * Reads and writes a [ZonedDateTime] as one string: the local date and time, the offset in force at
 * that moment, and the zone in brackets, as in `2024-06-15T10:30:45+02:00[Europe/Rome]`.
 *
 * The offset is written even though the zone already implies it, so the string can be read by
 * anything that understands an ISO 8601 timestamp; the zone in brackets is what this reader needs
 * and what a bare offset would lose.
 */
object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor =
        PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ZonedDateTime = parseZonedDateTime(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(formatZonedDateTime(value))
    }

    /**
     * The moment and the zone [iso] names, in the form this object writes.
     *
     * @throws IllegalArgumentException when [iso] names no zone in brackets, since a moment without
     * one is not what this serializer stores.
     */
    fun parseZonedDateTime(iso: String): ZonedDateTime {
        val zoneId = iso.substringAfter("[", "").substringBefore("]")
        require(zoneId.isNotEmpty()) { "Missing zone ID" }

        val dateTimePart = iso.substringBefore("[")

        val instant = Instant.parse(dateTimePart)
        val timeZone = TimeZone.of(zoneId)

        return ZonedDateTime(instant, timeZone)
    }

    /** The stored form of [zdt], which [parseZonedDateTime] reads back into the same value. */
    fun formatZonedDateTime(zdt: ZonedDateTime): String {
        val localDateTime = zdt.instant.toLocalDateTime(zdt.timeZone)

        val offset = zdt.timeZone.offsetAt(zdt.instant)
        val offsetString = offset.toString() // e.g. +01:00

        return buildString {
            append(localDateTime) // yyyy-MM-ddTHH:mm:ss
            append(offsetString) // +01:00
            append("[")
            append(zdt.timeZone.id) // Europe/Rome
            append("]")
        }
    }
}
