package com.takaotech.gunzou.here

import com.takaotech.gunzou.here.common.HereClientConfig
import com.takaotech.gunzou.here.common.createHereHttpClient
import com.takaotech.gunzou.here.common.withHereDefaults
import com.takaotech.gunzou.here.publictransit.client.HereTransitApi
import com.takaotech.gunzou.here.routing.client.HereRoutingApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

/**
 * Entry point to the HERE APIs.
 *
 * The APIs live on different hosts but share an API key, a JSON configuration and an error
 * contract, so they share a single HTTP client owned by this facade. Build one per API key and
 * [close] it when done:
 *
 * ```
 * val here = HereClient(HereClientConfig(apiKey = key))
 * here.routing.getRoutes(request)
 * here.publicTransit.getRoutes(transitRequest)
 * here.close()
 * ```
 *
 * @property config Shared configuration of every API reachable from this client
 */
class HereClient private constructor(private val httpClient: HttpClient, val config: HereClientConfig) : AutoCloseable {

    /**
     * Creates a client on the default engine of the current platform.
     */
    constructor(config: HereClientConfig) : this(
        httpClient = createHereHttpClient(config),
        config = config,
    )

    /** HERE Routing API v8. */
    val routing: HereRoutingApi by lazy { HereRoutingApi(httpClient, config.routingBaseUrl) }

    /** HERE Public Transit API v8. */
    val publicTransit: HereTransitApi by lazy {
        HereTransitApi(
            httpClient,
            config.publicTransitBaseUrl,
        )
    }

    /**
     * Closes the underlying HTTP client. An engine passed to [withEngine] outlives it, as its owner
     * is the caller.
     */
    override fun close() {
        httpClient.close()
    }

    /** Alternative ways to build a client. */
    companion object {
        /**
         * Creates a client on an engine supplied by the caller — a test engine, or one shared with
         * the rest of the application. The HERE configuration is applied here, so the caller only
         * provides the engine.
         */
        fun withEngine(engine: HttpClientEngine, config: HereClientConfig): HereClient =
            HereClient(httpClient = HttpClient(engine).withHereDefaults(config), config = config)
    }
}
