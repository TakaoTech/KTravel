package com.takaotech.gunzou.client

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import com.takaotech.gunzou.api.NavigatorApi
import com.takaotech.gunzou.api.NavigatorJson
import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.common.ProviderProfile
import com.takaotech.gunzou.api.common.RoutingProviderId
import com.takaotech.gunzou.api.here.HereRoutingRequest
import com.takaotech.gunzou.api.here.HereTransportMode
import com.takaotech.gunzou.api.response.RoutingRouteResponse
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * That HTTP traffic reaches the logger the caller passed, and that its minimum severity is what
 * decides whether it does.
 *
 * There is no logging flag on [NavigatorClientConfig], so this is the whole switch: an application
 * whose logger keeps `Severity.Debug` sees the calls, one that stays at `Severity.Info` sees none of
 * them. It is worth a test because the failure is silent in both directions — either nothing is
 * logged and the client looks mute, or the traffic is logged and nobody notices what is in it.
 *
 * What must not depend on that switch is the provider key: it is the traveller's own credential, and
 * the log it would land in is shown on a diagnostics screen and attached to an issue report. So the
 * dump it appears in is checked for it, at the severity that produces the dump.
 *
 * Nothing here touches the Kermit singleton: a logger is built for the test and handed to the
 * client, which is how the application does it too.
 */
class NavigatorClientLoggingTest {

    private val routingRequest = HereRoutingRequest(
        origin = GeoPoint(lat = 44.4949, lng = 11.3426),
        destination = GeoPoint(lat = 43.7696, lng = 11.2558),
    )

    private val recorded = mutableListOf<Pair<Severity, String>>()

    private val recordingWriter = object : LogWriter() {
        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            recorded += severity to message
        }
    }

    @Test
    fun `Given a logger keeping debug When a call is made Then the traffic is logged`() = runTest {
        client(minSeverity = Severity.Debug).hereRouting(HereTransportMode.CAR, routingRequest)

        assertTrue(recorded.isNotEmpty(), "the Ktor plugin wrote nothing through the logger")
        assertTrue(
            recorded.all { (severity, _) -> severity == Severity.Debug },
            "HTTP traffic must be written at Debug, so raising the minimum severity silences it",
        )
        assertTrue(
            recorded.any { (_, message) -> message.contains("http://navigator.test") },
            "the request was expected in the log, got: ${recorded.map { it.second }}",
        )
    }

    @Test
    fun `Given a call carrying a provider key When the traffic is logged Then the key is redacted`() = runTest {
        client(minSeverity = Severity.Debug)
            .hereRouting(HereTransportMode.CAR, routingRequest, apiKey = "the-provider-key")

        val logged = recorded.joinToString("\n") { it.second }

        assertTrue(logged.contains(NavigatorApi.PROVIDER_KEY_HEADER), "the header itself is expected in the dump")
        assertFalse(logged.contains("the-provider-key"), "the provider key was written into the log: $logged")
    }

    @Test
    fun `Given a logger keeping info only When a call is made Then nothing is logged`() = runTest {
        client(minSeverity = Severity.Info).hereRouting(HereTransportMode.CAR, routingRequest)

        assertEquals(emptyList(), recorded, "a release severity must not let request bodies through")
    }

    private fun client(minSeverity: Severity): NavigatorClient {
        val engine = MockEngine {
            respond(
                content = NavigatorJson.encodeToString(
                    RoutingRouteResponse.serializer(),
                    RoutingRouteResponse(provider = RoutingProviderId.Here, profile = ProviderProfile.ROUTING),
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        return NavigatorClient.withEngine(
            engine,
            NavigatorClientConfig(
                baseUrl = NavigatorBaseUrl { "http://navigator.test" },
                logger = Logger(loggerConfigInit(recordingWriter, minSeverity = minSeverity)),
            ),
        )
    }
}
