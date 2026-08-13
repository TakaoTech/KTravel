package com.takaotech.ktravel

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.NavigatorJson
import com.takaotech.navigator.api.catalog.HealthResponse
import com.takaotech.navigator.api.catalog.ProviderCatalogResponse
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.error.ErrorCode
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The endpoints that describe the server itself rather than route anything.
 *
 * `GET /v1/profiles` is the one that earns its keep: it is what lets the app draw its provider
 * selector from the server instead of from a `when` it would have to ship an update to change.
 */
class ServerTest {

    // The shared module is installed explicitly instead of through configure(): loading
    // application.conf pulls in reflection based module resolution, which is JVM only.
    @Test
    fun `Given the server When health is requested Then it reports ok and the running version`() = testApplication {
        application { module() }

        val response = client.get(NavigatorApi.HEALTH)

        assertEquals(HttpStatusCode.OK, response.status)
        val health = NavigatorJson.decodeFromString(HealthResponse.serializer(), response.bodyAsText())
        assertEquals(HealthResponse.STATUS_OK, health.status)
        assertTrue(health.version.isNotBlank())
    }

    @Test
    fun `Given the server When the catalog is requested Then it lists the HERE road profile`() = testApplication {
        application { module() }

        val response = client.get(NavigatorApi.PROFILES)

        assertEquals(HttpStatusCode.OK, response.status)
        val catalog = NavigatorJson.decodeFromString(ProviderCatalogResponse.serializer(), response.bodyAsText())
        val profile = catalog.profiles.single()
        assertEquals(ProviderId.HERE, profile.provider)
        assertEquals(ProviderProfile.CAR, profile.profile)
        assertEquals(NavigatorApi.HERE_CAR, profile.path)
    }

    @Test
    fun `Given the catalog When a profile is read Then it advertises the limits the server enforces`() =
        testApplication {
            application { module() }

            val response = client.get(NavigatorApi.PROFILES)

            val profile = NavigatorJson
                .decodeFromString(ProviderCatalogResponse.serializer(), response.bodyAsText())
                .profiles.single()
            assertTrue(profile.requiresApiKey, "The HERE profile is served through the caller's own key")
            assertTrue(profile.maxAlternatives >= 1)
        }

    @Test
    fun `Given a path nobody serves When it is called Then it fails with the contract error body`() = testApplication {
        application { module() }

        val response = client.get("/v1/here/hovercraft")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(ErrorCode.INVALID_REQUEST, response.decodeError().code)
    }

    @Test
    fun `Given the demo scaffolding is gone When its paths are called Then they are no longer served`() =
        testApplication {
            application { module() }

            listOf("/", "/articles", "/json/kotlinx-serialization").forEach { path ->
                assertEquals(HttpStatusCode.NotFound, client.get(path).status, "$path should not be served")
            }
        }
}
