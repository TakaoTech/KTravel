package com.takaotech.ktravel

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.NavigatorJson
import com.takaotech.navigator.api.error.ErrorResponse
import com.takaotech.navigator.api.response.RouteResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.KSerializer

// The bodies are encoded and decoded by hand with NavigatorJson rather than through a content
// negotiation plugin on the test client. The point is to exercise the bytes the server actually
// writes: a client configured with its own Json would paper over a mismatch between the two
// configurations, which is precisely the failure these tests exist to catch.

/** Posts a body to [path], optionally presenting a provider key. */
suspend fun <T> HttpClient.postJson(
    path: String,
    serializer: KSerializer<T>,
    body: T,
    providerKey: String? = TEST_PROVIDER_KEY,
): HttpResponse = post(path) {
    contentType(ContentType.Application.Json)
    providerKey?.let { header(NavigatorApi.PROVIDER_KEY_HEADER, it) }
    setBody(NavigatorJson.encodeToString(serializer, body))
}

/** Posts a raw string, for the cases where the body is deliberately not valid. */
suspend fun HttpClient.postRaw(path: String, body: String, providerKey: String? = TEST_PROVIDER_KEY): HttpResponse =
    post(path) {
        contentType(ContentType.Application.Json)
        providerKey?.let { header(NavigatorApi.PROVIDER_KEY_HEADER, it) }
        setBody(body)
    }

/** Reads a successful answer. */
suspend fun HttpResponse.decodeRoutes(): RouteResponse =
    NavigatorJson.decodeFromString(RouteResponse.serializer(), bodyAsText())

/** Reads a failure. */
suspend fun HttpResponse.decodeError(): ErrorResponse =
    NavigatorJson.decodeFromString(ErrorResponse.serializer(), bodyAsText())

/**
 * Any non blank string will do: the endpoint under test is the fake, which never calls HERE. What
 * matters is that the header is present, because the profile declares it required.
 */
const val TEST_PROVIDER_KEY: String = "test-provider-key"
