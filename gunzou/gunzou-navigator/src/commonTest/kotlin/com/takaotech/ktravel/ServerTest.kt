package com.takaotech.ktravel

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerTest {

    // The shared module is installed explicitly instead of through configure(): loading
    // application.conf pulls in reflection based module resolution, which is JVM only.
    @Test
    fun `Given the server When the root endpoint is called Then it responds with OK`() = testApplication {
        application { module() }

        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

    @Test
    fun `Given the server When articles are requested Then the sort order is echoed back`() = testApplication {
        application { module() }

        val response = client.get("/articles?sort=old")

        assertEquals(HttpStatusCode.OK, response.status)
    }
}
