package com.takaotech.ktravel.core.logging

import com.takaotech.ktravel.testutil.tempdir
import io.github.vinceglb.filekit.path
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.io.files.Path
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The log on disk: one file per day, and one JSON object per line.
 *
 * Against a real directory rather than a fake file system, because the two things worth checking are
 * exactly the ones a fake would assume: that a line written today and one written yesterday end up
 * in different files, and that what is read back is what was written, stack trace included.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LogFileSinkTest :
    BehaviorSpec({
        val tempDir = tempdir("test-log-sink")

        given("lines written on two different days") {
            `when`("the sink has drained them") {
                then("each day has its own file, and the entries survive the round trip") {
                    runTest(UnconfinedTestDispatcher()) {
                        val store = LogFileStore(Path(tempDir.path, "two-days"))
                        val sink = LogFileSink(
                            store = store,
                            retentionDays = { MAX_LOG_RETENTION_DAYS },
                            clock = Clock.System,
                            timeZone = TimeZone.UTC,
                        )
                        backgroundScope.launch { sink.run() }

                        sink.write(entry("1", "2026-09-05T23:30:00Z", stackTrace = "boom\n\tat here"))
                        sink.write(entry("2", "2026-09-06T00:30:00Z"))
                        testScheduler.advanceUntilIdle()

                        store.days() shouldBe listOf(LocalDate(2026, 9, 5), LocalDate(2026, 9, 6))

                        val yesterday = store.readLines(LocalDate(2026, 9, 5))
                        yesterday.size shouldBe 1
                        yesterday.single().contains("boom") shouldBe true

                        store.readLines(LocalDate(2026, 9, 6)).size shouldBe 1
                    }
                }
            }
        }

        given("files older than the retention") {
            `when`("the sink purges") {
                then("only the days being kept are left") {
                    runTest(UnconfinedTestDispatcher()) {
                        val store = LogFileStore(Path(tempDir.path, "purge"))
                        val today = LocalDate(2026, 9, 6)
                        listOf(LocalDate(2026, 9, 1), LocalDate(2026, 9, 5), today).forEach {
                            store.append(it, listOf("{}"))
                        }

                        val sink = LogFileSink(
                            store = store,
                            retentionDays = { 2 },
                            clock = FixedClock(Instant.parse("2026-09-06T10:00:00Z")),
                            timeZone = TimeZone.UTC,
                        )

                        sink.purgeExpired()

                        store.days() shouldBe listOf(LocalDate(2026, 9, 5), today)
                    }
                }
            }
        }
    })

/** A clock that does not move, so "today" is the same in the test as in its assertions. */
private class FixedClock(private val now: Instant) : Clock {
    override fun now(): Instant = now
}

private fun entry(id: String, at: String, stackTrace: String? = null) = LogEntry(
    id = id,
    timestamp = Instant.parse(at),
    level = LogLevel.Info,
    tag = "test",
    message = "line $id",
    stackTrace = stackTrace,
)
