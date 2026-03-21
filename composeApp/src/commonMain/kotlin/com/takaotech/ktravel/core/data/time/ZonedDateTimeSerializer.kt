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

object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor =
        PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        return parseZonedDateTime(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(formatZonedDateTime(value))
    }


    fun parseZonedDateTime(iso: String): ZonedDateTime {
        val zoneId = iso.substringAfter("[", "").substringBefore("]")
        require(zoneId.isNotEmpty()) { "Missing zone ID" }

        val dateTimePart = iso.substringBefore("[")

        val instant = Instant.parse(dateTimePart)
        val timeZone = TimeZone.of(zoneId)

        return ZonedDateTime(instant, timeZone)
    }

    fun formatZonedDateTime(zdt: ZonedDateTime): String {
        val localDateTime = zdt.instant.toLocalDateTime(zdt.timeZone)

        val offset = zdt.timeZone.offsetAt(zdt.instant)
        val offsetString = offset.toString() // es: +01:00

        return buildString {
            append(localDateTime)   // yyyy-MM-ddTHH:mm:ss
            append(offsetString)    // +01:00
            append("[")
            append(zdt.timeZone.id) // Europe/Rome
            append("]")
        }
    }
}