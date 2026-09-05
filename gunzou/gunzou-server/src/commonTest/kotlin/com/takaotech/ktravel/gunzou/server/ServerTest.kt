package com.takaotech.ktravel.gunzou.server

import com.takaotech.gunzou.api.NavigatorApi
import com.takaotech.gunzou.api.NavigatorJson
import com.takaotech.gunzou.api.catalog.HealthResponse
import com.takaotech.gunzou.api.catalog.ProviderCatalogResponse
import com.takaotech.gunzou.api.common.ProviderId
import com.takaotech.gunzou.api.common.ProviderProfile
import com.takaotech.gunzou.api.error.ErrorCode
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
    fun `Given the server When the catalog is requested Then it lists both HERE profiles`() = testApplication {
        application { module() }

        val response = client.get(NavigatorApi.PROFILES)

        assertEquals(HttpStatusCode.OK, response.status)
        val catalog = NavigatorJson.decodeFromString(ProviderCatalogResponse.serializer(), response.bodyAsText())
        assertEquals(listOf(ProviderId.HERE, ProviderId.HERE), catalog.profiles.map { it.provider })
        assertEquals(listOf(ProviderProfile.ROUTING, ProviderProfile.TRANSIT), catalog.profiles.map { it.profile })
        assertEquals(
            listOf(NavigatorApi.HERE_ROUTING_TEMPLATE, NavigatorApi.HERE_TRANSIT),
            catalog.profiles.map { it.path },
        )
    }

    @Test
    fun `Given the catalog When the two profiles are compared Then each publishes its own limits`() = testApplication {
        application { module() }

        val response = client.get(NavigatorApi.PROFILES)

        val profiles = NavigatorJson
            .decodeFromString(ProviderCatalogResponse.serializer(), response.bodyAsText())
            .profiles.associateBy { it.profile }
        val road = profiles.getValue(ProviderProfile.ROUTING)
        val transit = profiles.getValue(ProviderProfile.TRANSIT)

        assertTrue(road.requiresApiKey && transit.requiresApiKey, "Both are served through the caller's own key")
        assertTrue(road.supportsTolls && !transit.supportsTolls, "Only a road route has tolls to pay")
        assertTrue(road.maxVia > 0 && transit.maxVia == 0, "A journey is planned between two places")
        assertTrue(road.maxAlternatives > transit.maxAlternatives)
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
