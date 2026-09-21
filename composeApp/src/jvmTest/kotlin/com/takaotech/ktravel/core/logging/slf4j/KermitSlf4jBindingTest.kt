package com.takaotech.ktravel.core.logging.slf4j

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory

/**
 * The application is its own SLF4J backend.
 *
 * This is what puts a Ktor or a Couchbase line on the diagnostics screen next to the application's
 * own, and it only works if two things hold: that the provider resolved through `ServiceLoader` is
 * ours — logback is deliberately absent from this classpath — and that the line arrives with its
 * level, its logger name and its throwable intact.
 */
class KermitSlf4jBindingTest :
    BehaviorSpec({
        given("the SLF4J backend of this application") {
            `when`("a logger is asked for") {
                then("it comes from the Kermit binding, not from another provider") {
                    LoggerFactory.getLogger("anything")::class shouldBe KermitSlf4jLogger::class
                }
            }
        }

        given("a logger writing through SLF4J") {
            `when`("it logs with placeholders and a failure") {
                then("Kermit receives the formatted message, the level and the throwable") {
                    val recorded = mutableListOf<Triple<Severity, String, String>>()
                    var recordedThrowable: Throwable? = null
                    val writer = object : LogWriter() {
                        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
                            recorded += Triple(severity, tag, message)
                            recordedThrowable = throwable
                        }
                    }
                    KermitSlf4jBridge.install(
                        Logger(loggerConfigInit(writer, minSeverity = Severity.Verbose), tag = "test"),
                    )
                    val failure = IllegalStateException("nope")

                    LoggerFactory.getLogger("io.ktor.client").warn("call to {} failed", "/v1/route", failure)

                    recorded.single() shouldBe Triple(Severity.Warn, "io.ktor.client", "call to /v1/route failed")
                    recordedThrowable shouldBe failure
                }
            }
        }
    })
