package com.takaotech.ktravel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ServerFactoryTest {

    // ServerTest goes through testApplication, which binds no real socket. These tests start the
    // engine for real, so they run on Dispatchers.Default rather than on the virtual time scheduler
    // runTest installs.
    @Test
    fun `Given a server started on a free port When it is running Then the resolved port is not the placeholder`() =
        runTest {
            withContext(Dispatchers.Default) {
                val running = startServerOnFreePort()

                try {
                    assertNotEquals(EPHEMERAL_PORT, running.port)
                    assertTrue(running.port in 1..MAX_PORT, "Unexpected port ${running.port}")
                } finally {
                    running.stop()
                }
            }
        }

    @Test
    fun `Given two servers started on free ports When both are running Then they bind different ports`() = runTest {
        withContext(Dispatchers.Default) {
            val first = startServerOnFreePort()

            try {
                val second = startServerOnFreePort()

                try {
                    assertNotEquals(first.port, second.port)
                } finally {
                    second.stop()
                }
            } finally {
                first.stop()
            }
        }
    }

    private companion object {
        const val MAX_PORT = 65535
    }
}
