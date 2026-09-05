package com.takaotech.gunzou.client

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import com.takaotech.gunzou.api.NavigatorJson
import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.common.ProviderId
import com.takaotech.gunzou.api.common.ProviderProfile
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
import kotlin.test.assertTrue

/**
 * That HTTP traffic reaches the logger the caller passed, and that its minimum severity is what
 * decides whether it does.
 *
 * There is no logging flag on [NavigatorClientConfig], so this is the whole switch: an application
 * whose logger keeps `Severity.Debug` sees the calls, one that stays at `Severity.Info` sees none of
 * them. It is worth a test because the failure is silent in both directions — either nothing is
 * logged and the client looks mute, or a release build writes provider keys into the log.
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
    fun `Given a logger keeping info only When a call is made Then nothing is logged`() = runTest {
        client(minSeverity = Severity.Info).hereRouting(HereTransportMode.CAR, routingRequest)

        assertEquals(emptyList(), recorded, "a release severity must not let request bodies through")
    }

    private fun client(minSeverity: Severity): NavigatorClient {
        val engine = MockEngine {
            respond(
                content = NavigatorJson.encodeToString(
                    RoutingRouteResponse.serializer(),
                    RoutingRouteResponse(provider = ProviderId.HERE, profile = ProviderProfile.ROUTING),
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
