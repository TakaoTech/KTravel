package com.takaotech.ktravel.testutil

import co.touchlab.kermit.Logger
import com.takaotech.ktravel.core.logging.AppLogger
import com.takaotech.ktravel.core.logging.KermitAppLogger

/**
 * The logger a test hands to something that needs one.
 *
 * Kermit alone: the buffer, the log file and the telemetry writer are configured by the dependency
 * graph, which a unit test does not build. What it logs still shows up in the test output, which is
 * the only thing a failing test wants from it.
 */
fun testAppLogger(): AppLogger = TEST_APP_LOGGER

/** One instance for the whole test run, the way the graph binds one for the whole application. */
private val TEST_APP_LOGGER: AppLogger = KermitAppLogger(Logger)
