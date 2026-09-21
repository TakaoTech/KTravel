package com.takaotech.gunzou.here.common

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * That the HERE API key never reaches a writer.
 *
 * HERE authenticates by query parameter, so `LogLevel.ALL` would put the key in the logged URL —
 * and this log is shown on a diagnostics screen and attached to an issue report. The redaction is
 * the only thing standing between the two, and it has to hold whatever the build is, so it is
 * checked here rather than left to a release only severity.
 */
class KermitKtorLoggerTest {

    private val recorded = mutableListOf<String>()

    private val recordingWriter = object : LogWriter() {
        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            recorded += message
        }
    }

    @Test
    fun `Given a request URL carrying the key When it is logged Then the key is redacted`() {
        logger(apiKey = "the-secret-key").log(
            "REQUEST: https://router.hereapi.com/v8/routes?origin=44.49,11.34&apiKey=the-secret-key",
        )

        assertEquals(1, recorded.size)
        assertFalse(recorded.single().contains("the-secret-key"), "the key was written: ${recorded.single()}")
        assertTrue(recorded.single().contains("apiKey=***"), "expected a redacted parameter: ${recorded.single()}")
        assertTrue(recorded.single().contains("origin=44.49,11.34"), "the rest of the URL must survive")
    }

    @Test
    fun `Given a key belonging to another caller When it is logged Then the parameter is redacted anyway`() {
        logger(apiKey = "").log("REQUEST: https://transit.hereapi.com/v8/routes?apiKey=someone-elses-key&r=1")

        assertFalse(recorded.single().contains("someone-elses-key"), "the parameter is redacted by name, not by value")
        assertTrue(recorded.single().contains("r=1"), "only the key is taken out")
    }

    @Test
    fun `Given the key echoed outside a URL When it is logged Then it is redacted there too`() {
        logger(apiKey = "the-secret-key").log("""RESPONSE 401: {"error":"apikey the-secret-key is invalid"}""")

        assertFalse(recorded.single().contains("the-secret-key"), "the key was written: ${recorded.single()}")
    }

    private fun logger(apiKey: String): KermitKtorLogger = KermitKtorLogger(
        logger = Logger(loggerConfigInit(recordingWriter, minSeverity = Severity.Verbose)),
        apiKey = apiKey,
    )
}
