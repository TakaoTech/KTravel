package com.takaotech.ktravel.core.data.time

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

class ZonedDateTimeSerializerTest : BehaviorSpec({

    given("a valid ISO string with zone ID") {
        `when`("parseZonedDateTime is called with Europe/Rome zone") {
            val iso = "2024-06-15T10:30:45+02:00[Europe/Rome]"
            val result = ZonedDateTimeSerializer.parseZonedDateTime(iso)

            then("should return a ZonedDateTime with the correct timezone") {
                result.timeZone shouldBe TimeZone.of("Europe/Rome")
            }

            then("should return a ZonedDateTime with the correct instant") {
                result.instant shouldBe Instant.parse("2024-06-15T08:30:45Z")
            }
        }

        `when`("parseZonedDateTime is called with UTC zone") {
            val iso = "2024-01-01T00:00:00Z[UTC]"
            val result = ZonedDateTimeSerializer.parseZonedDateTime(iso)

            then("should return a ZonedDateTime with UTC timezone") {
                result.timeZone shouldBe TimeZone.of("UTC")
            }

            then("should return a ZonedDateTime with the correct instant") {
                result.instant shouldBe Instant.parse("2024-01-01T00:00:00Z")
            }
        }

        `when`("parseZonedDateTime is called with America/New_York zone") {
            val iso = "2024-03-10T12:00:00-05:00[America/New_York]"
            val result = ZonedDateTimeSerializer.parseZonedDateTime(iso)

            then("should return a ZonedDateTime with America/New_York timezone") {
                result.timeZone shouldBe TimeZone.of("America/New_York")
            }

            then("should return a ZonedDateTime with the correct instant") {
                result.instant shouldBe Instant.parse("2024-03-10T17:00:00Z")
            }
        }
    }

    given("an invalid ISO string") {
        `when`("parseZonedDateTime is called without zone ID brackets") {
            then("should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    ZonedDateTimeSerializer.parseZonedDateTime("2024-06-15T10:30:00+02:00")
                }
            }
        }

        `when`("parseZonedDateTime is called with empty zone ID") {
            then("should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    ZonedDateTimeSerializer.parseZonedDateTime("2024-06-15T10:30:00+02:00[]")
                }
            }
        }
    }

    given("a ZonedDateTime object") {
        `when`("formatZonedDateTime is called with Europe/Rome timezone in summer") {
            val instant = Instant.parse("2024-06-15T08:30:45Z")
            val zdt = ZonedDateTime(instant, TimeZone.of("Europe/Rome"))
            val result = ZonedDateTimeSerializer.formatZonedDateTime(zdt)

            then("should produce a string containing the zone ID") {
                result.contains("[Europe/Rome]") shouldBe true
            }

            then("should produce a string containing the UTC offset") {
                result.contains("+02:00") shouldBe true
            }

            then("should produce a string with the local date-time") {
                result.startsWith("2024-06-15T10:30:45") shouldBe true
            }
        }

        `when`("formatZonedDateTime is called with UTC timezone") {
            val instant = Instant.parse("2024-01-01T00:00:00Z")
            val zdt = ZonedDateTime(instant, TimeZone.of("UTC"))
            val result = ZonedDateTimeSerializer.formatZonedDateTime(zdt)

            then("should produce a string containing [UTC]") {
                result.contains("[UTC]") shouldBe true
            }

            then("should produce a string with zero offset") {
                result.contains("Z") || result.contains("+00:00") shouldBe true
            }
        }
    }

    given("a round-trip serialization") {
        `when`("a ZonedDateTime is formatted and then parsed") {
            val original = ZonedDateTime(
                instant = Instant.parse("2024-06-15T08:30:45Z"),
                timeZone = TimeZone.of("Europe/Rome")
            )
            val formatted = ZonedDateTimeSerializer.formatZonedDateTime(original)
            val parsed = ZonedDateTimeSerializer.parseZonedDateTime(formatted)

            then("should preserve the instant") {
                parsed.instant shouldBe original.instant
            }

            then("should preserve the timezone") {
                parsed.timeZone shouldBe original.timeZone
            }
        }

        `when`("an ISO string is parsed and then formatted") {
            val original = "2024-06-15T10:30:45+02:00[Europe/Rome]"
            val parsed = ZonedDateTimeSerializer.parseZonedDateTime(original)
            val formatted = ZonedDateTimeSerializer.formatZonedDateTime(parsed)
            val reparsed = ZonedDateTimeSerializer.parseZonedDateTime(formatted)

            then("should preserve the instant after re-parsing") {
                reparsed.instant shouldBe parsed.instant
            }

            then("should preserve the timezone after re-parsing") {
                reparsed.timeZone shouldBe parsed.timeZone
            }
        }
    }
})
