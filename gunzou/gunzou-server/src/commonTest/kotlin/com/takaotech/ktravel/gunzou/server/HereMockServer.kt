package com.takaotech.ktravel.gunzou.server

import com.takaotech.ktravel.gunzou.server.endpoint.here.HereClientPool
import com.takaotech.gunzou.here.HereClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * HERE, standing still.
 *
 * The endpoints are exercised against recorded payloads rather than the live API. That is not only
 * about cost and about not needing a key in CI: a test that calls HERE is testing HERE, and would go
 * red the day a road is resurfaced. What is worth pinning is the two translations — the query string
 * this server builds and what it makes of the answer — and both are deterministic.
 *
 * The requests the engine saw are kept, so a test can assert on the URL that was actually built
 * rather than on the return value of a mapper nobody calls in production.
 *
 * @param body What HERE answers with.
 * @param status The status it answers with, for the failure paths.
 */
class HereMockServer(private val body: String = "{}", private val status: HttpStatusCode = HttpStatusCode.OK) {

    private val recorded = mutableListOf<HttpRequestData>()

    private val engine = MockEngine { request ->
        recorded += request
        respond(content = body, status = status, headers = headersOf(HttpHeaders.ContentType, "application/json"))
    }

    /** Every request the HERE client made, in order. */
    val requests: List<HttpRequestData> get() = recorded

    /** The URL of the only request made, failing loudly when more than one was. */
    val requestUrl: Url get() = recorded.single().url

    /** A Koin override putting this engine behind both profiles. */
    fun asKoinModule(): Module = module {
        single { HereClientPool(createClient = { config -> HereClient.withEngine(engine, config) }) }
    }
}

/** The value of a query parameter on the request the HERE client made. */
fun Url.query(name: String): String? = parameters[name]

/** Every value of a repeated query parameter, such as `via`. */
fun Url.queryAll(name: String): List<String> = parameters.getAll(name).orEmpty()
