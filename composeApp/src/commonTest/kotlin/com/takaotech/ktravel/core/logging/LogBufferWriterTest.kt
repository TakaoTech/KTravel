package com.takaotech.ktravel.core.logging

import co.touchlab.kermit.Severity
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.TimeZone
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The writer that turns a Kermit line into something the application can show and keep.
 *
 * The trip stamped on the line is the part worth a test: it is what makes the diagnostics screen of
 * a trip show that trip, and it comes from navigation rather than from the caller.
 */
class LogBufferWriterTest :
    BehaviorSpec({
        given("a line logged while a trip is open") {
            `when`("the writer records it") {
                then("it carries the level, the tag and the trip") {
                    val store = InMemoryLogStore()
                    val scope = LogScope().apply { travelId = "trip-42" }
                    val writer = writerOver(store, scope)

                    writer.log(Severity.Warn, "the route is stale", "gunzou-client", null)

                    val entry = store.entries.value.single()
                    entry.level shouldBe LogLevel.Warn
                    entry.tag shouldBe "gunzou-client"
                    entry.message shouldBe "the route is stale"
                    entry.travelId shouldBe "trip-42"
                    entry.stackTrace shouldBe null
                }
            }
        }

        given("a failure logged outside any trip") {
            `when`("the writer records it") {
                then("the stack trace is kept and no trip is claimed") {
                    val store = InMemoryLogStore()
                    val writer = writerOver(store, LogScope())

                    writer.log(Severity.Error, "boom", "KTravel", IllegalStateException("nope"))

                    val entry = store.entries.value.single()
                    entry.travelId shouldBe null
                    entry.stackTrace?.contains("nope") shouldBe true
                }
            }
        }
    })

/** A writer over a sink pointed at a throwaway directory: this test is about the entry, not the file. */
private fun writerOver(store: LogStore, scope: LogScope) = LogBufferWriter(
    store = store,
    sink = LogFileSink(
        store = LogFileStore(Path(SystemTemporaryDirectory, "ktravel-log-buffer-test")),
        retentionDays = { MIN_LOG_RETENTION_DAYS },
        timeZone = TimeZone.UTC,
    ),
    scope = scope,
    clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-09-06T09:00:00Z")
    },
    newId = { "fixed" },
)
