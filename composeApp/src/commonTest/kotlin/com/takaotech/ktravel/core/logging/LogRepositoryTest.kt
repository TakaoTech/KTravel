package com.takaotech.ktravel.core.logging

import com.takaotech.ktravel.testutil.tempdir
import io.github.vinceglb.filekit.path
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import kotlin.time.Instant

/**
 * What the diagnostics screen reads: the files, plus the session that is still running.
 *
 * The merge is the part that can go wrong. A line that has been written to disk *and* is still in
 * the in-memory buffer must appear once, and the whole thing has to come back in the order it
 * happened rather than in the order the two sources were read.
 */
class LogRepositoryTest :
    BehaviorSpec({
        val tempDir = tempdir("test-log-repository")
        val json = Json { encodeDefaults = false }

        given("a line that is both on disk and still in memory") {
            `when`("the kept log is read") {
                then("it appears once, and the log is ordered by time") {
                    val store = LogFileStore(Path(tempDir.path, "merge"))
                    val onDisk = entry("shared", "2026-09-06T09:00:00Z")
                    val older = entry("older", "2026-09-06T08:00:00Z")
                    store.append(LocalDate(2026, 9, 6), listOf(json.encodeToString(older), json.encodeToString(onDisk)))

                    val memory = InMemoryLogStore().apply {
                        record(onDisk)
                        record(entry("newest", "2026-09-06T10:00:00Z"))
                    }
                    val repository = LogRepository(store, memory, TimeZone.UTC)

                    repository.allEntries().map { it.id } shouldBe listOf("older", "shared", "newest")
                }
            }
        }

        given("a log with two days on disk") {
            `when`("one day is asked for") {
                then("only that day comes back") {
                    val store = LogFileStore(Path(tempDir.path, "days"))
                    store.append(LocalDate(2026, 9, 5), listOf(json.encodeToString(entry("y", "2026-09-05T09:00:00Z"))))
                    store.append(LocalDate(2026, 9, 6), listOf(json.encodeToString(entry("t", "2026-09-06T09:00:00Z"))))
                    val repository = LogRepository(store, InMemoryLogStore(), TimeZone.UTC)

                    repository.persistedEntries(LocalDate(2026, 9, 5)).map { it.id } shouldBe listOf("y")
                    repository.days() shouldBe listOf(LocalDate(2026, 9, 5), LocalDate(2026, 9, 6))
                }
            }
        }

        given("a file with a line that was cut in half by a process that died") {
            `when`("the log is read") {
                then("the broken line is dropped and the rest survives") {
                    val store = LogFileStore(Path(tempDir.path, "truncated"))
                    store.append(
                        LocalDate(2026, 9, 6),
                        listOf(json.encodeToString(entry("good", "2026-09-06T09:00:00Z")), "{\"id\":\"hal"),
                    )
                    val repository = LogRepository(store, InMemoryLogStore(), TimeZone.UTC)

                    repository.persistedEntries().map { it.id } shouldBe listOf("good")
                }
            }
        }
    })

private fun entry(id: String, at: String) = LogEntry(
    id = id,
    timestamp = Instant.parse(at),
    level = LogLevel.Info,
    tag = "test",
    message = "line $id",
)
