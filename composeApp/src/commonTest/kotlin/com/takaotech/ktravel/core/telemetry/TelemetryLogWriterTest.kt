package com.takaotech.ktravel.core.telemetry

import co.touchlab.kermit.Severity
import com.takaotech.ktravel.core.logging.LogLevel
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * The one thing standing between a log line and the network.
 *
 * Every case here is a privacy statement rather than a formatting detail: nothing leaves the device
 * before the user has answered, nothing leaves it after they revoke, and the debug chatter of the
 * HTTP clients never leaves it at all.
 */
class TelemetryLogWriterTest :
    BehaviorSpec({
        given("a user who has not answered the notice yet") {
            `when`("a line is logged") {
                then("nothing reaches the backend") {
                    val sink = RecordingSink()
                    val writer = TelemetryLogWriter(sink) { TelemetryConsent.Unknown }

                    writer.log(Severity.Error, "boom", "KTravel", null)

                    sink.recorded.size shouldBe 0
                }
            }
        }

        given("a user who refused") {
            `when`("a line is logged") {
                then("nothing reaches the backend") {
                    val sink = RecordingSink()
                    val writer = TelemetryLogWriter(sink) { TelemetryConsent.Denied }

                    writer.log(Severity.Error, "boom", "KTravel", null)

                    sink.recorded.size shouldBe 0
                }
            }
        }

        given("a user who consented") {
            `when`("lines of every level are logged") {
                then("only Info and above are sent") {
                    val sink = RecordingSink()
                    val writer = TelemetryLogWriter(sink) { TelemetryConsent.Granted }

                    Severity.entries.forEach { writer.log(it, "line", "KTravel", null) }

                    sink.recorded.map { it.first } shouldBe listOf(
                        LogLevel.Info,
                        LogLevel.Warn,
                        LogLevel.Error,
                        LogLevel.Assert,
                    )
                }
            }

            `when`("a failure is logged") {
                then("the throwable travels with it") {
                    val sink = RecordingSink()
                    val writer = TelemetryLogWriter(sink) { TelemetryConsent.Granted }
                    val failure = IllegalStateException("nope")

                    writer.log(Severity.Error, "boom", "KTravel", failure)

                    sink.recorded.single().second shouldBe failure
                }
            }
        }
    })

/** A backend that sends nothing and remembers everything it was handed. */
private class RecordingSink : TelemetrySink {

    val recorded = mutableListOf<Pair<LogLevel, Throwable?>>()

    override fun start() = Unit

    override fun applyConsent(consent: TelemetryConsent) = Unit

    override fun identify(installationId: String) = Unit

    override fun record(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        recorded += level to throwable
    }

    override fun forgetMe() = Unit
}
