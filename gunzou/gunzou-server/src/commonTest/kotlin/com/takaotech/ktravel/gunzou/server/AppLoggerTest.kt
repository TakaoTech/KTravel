package com.takaotech.ktravel.gunzou.server

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Who decides where the server's log goes.
 *
 * The case that matters is the embedded one: the host application built a logger through its
 * dependency graph, and the server has to write through it rather than reconfigure the process
 * behind the host's back. That failure is silent — the host keeps `Severity.Debug` to see HTTP
 * traffic, the server would put it back to `Severity.Info`, and the traffic stops the first time a
 * route is planned.
 */
class AppLoggerTest {

    private val recorded = mutableListOf<Triple<Severity, String, String>>()

    private val recordingWriter = object : LogWriter() {
        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            recorded += Triple(severity, tag, message)
        }
    }

    private fun hostLogger(minSeverity: Severity = Severity.Debug): Logger =
        Logger(loggerConfigInit(recordingWriter, minSeverity = minSeverity), tag = "host")

    @AfterTest
    fun restoreOwnLogging() {
        installLogging()
    }

    @Test
    fun `Given a host logger When it is installed Then the server writes through it`() {
        installLogging(hostLogger())

        appLog.i { "started" }

        assertEquals(1, recorded.size, "the server did not write through the host's logger")
        assertEquals(Severity.Info, recorded.single().first)
        assertEquals("started", recorded.single().third)
    }

    @Test
    fun `Given a host logger When it is installed Then the server keeps its own tag`() {
        installLogging(hostLogger())

        appLog.i { "started" }

        assertEquals(
            "gunzou-navigator",
            recorded.single().second,
            "the server's lines have to stay recognisable inside the host's log",
        )
    }

    @Test
    fun `Given a host logger keeping debug When it is installed Then the global severity is untouched`() {
        Logger.setMinSeverity(Severity.Verbose)

        installLogging(hostLogger())

        assertTrue(
            Logger.config.minSeverity == Severity.Verbose,
            "installing a host's logger must not reconfigure the Kermit singleton the host owns",
        )
    }
}
