package com.takaotech.navigator.client

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.NavigatorJson
import com.takaotech.navigator.api.catalog.HealthResponse
import com.takaotech.navigator.api.catalog.ProviderCatalogResponse
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.error.ErrorResponse
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import com.takaotech.navigator.api.response.RouteResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlin.coroutines.cancellation.CancellationException

/**
 * Talks to gunzo-navigator.
 *
 * One method per path, and no `when` over providers anywhere: the request types differ because the
 * inputs genuinely differ, and choosing between them is choosing which method to call. What the
 * methods have in common is their return type, which is the whole reason the server exists.
 *
 * The same instance serves an embedded server and a remote one. Nothing here knows which: the origin
 * comes from [NavigatorClientConfig.baseUrl] on every call, so a server that restarts on a new port
 * is followed without the client being rebuilt.
 *
 * Build one and keep it — it owns an HTTP client, a connection pool and threads — and [close] it when
 * the thing that owns it goes away.
 */
class NavigatorClient private constructor(
    private val httpClient: HttpClient,
    private val config: NavigatorClientConfig,
) : AutoCloseable {

    /** Builds a client on the default engine of the current platform. */
    constructor(config: NavigatorClientConfig) : this(
        httpClient = createNavigatorHttpClient(config),
        config = config,
    )

    /**
     * Routes on roads, whatever the vehicle.
     *
     * @param apiKey The caller's key for the provider behind this profile. Required by every profile
     *   whose descriptor says [com.takaotech.navigator.api.catalog.ProviderProfileDescriptor.requiresApiKey].
     * @param target Which navigator to ask, when it is not the configured one.
     */
    suspend fun hereCar(
        request: HereCarRouteRequest,
        apiKey: String? = null,
        target: NavigatorTarget? = null,
    ): NavigatorResult<RouteResponse> =
        post(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request, apiKey, target)

    /** Routes on public transport. */
    suspend fun hereTransit(
        request: HereTransitRouteRequest,
        apiKey: String? = null,
        target: NavigatorTarget? = null,
    ): NavigatorResult<RouteResponse> =
        post(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request, apiKey, target)

    /**
     * What the navigator can route with.
     *
     * Worth calling before drawing a provider selector: it is what lets the app offer a profile the
     * server gained without the app being rebuilt for it, and what tells it which of the profiles the
     * contract declares this particular deployment actually mounts.
     */
    suspend fun profiles(target: NavigatorTarget? = null): NavigatorResult<ProviderCatalogResponse> =
        get(NavigatorApi.PROFILES, target)

    /**
     * Whether the navigator is answering.
     *
     * Liveness only — it says nothing about the providers behind it — which is exactly what an
     * embedded host needs to decide whether the server it started is still there, and what a settings
     * screen needs to tell a mistyped address from a server that is down.
     */
    suspend fun health(target: NavigatorTarget? = null): NavigatorResult<HealthResponse> =
        get(NavigatorApi.HEALTH, target)

    /** Releases the HTTP client. Nothing works afterwards. */
    override fun close() {
        httpClient.close()
    }

    private suspend inline fun <reified T> get(path: String, target: NavigatorTarget?): NavigatorResult<T> = call {
        httpClient.get(origin(target) + path) { presentAccessToken(target) }
    }

    private suspend inline fun <REQ, reified T> post(
        path: String,
        serializer: SerializationStrategy<REQ>,
        request: REQ,
        apiKey: String?,
        target: NavigatorTarget?,
    ): NavigatorResult<T> = call {
        httpClient.post(origin(target) + path) {
            contentType(ContentType.Application.Json)
            presentAccessToken(target)
            // The provider key and the access token answer different questions — which routing
            // engine account to bill, and who is allowed to ask — so they travel separately, and a
            // deployment that holds a key of its own means this one is simply absent.
            apiKey?.let { header(NavigatorApi.PROVIDER_KEY_HEADER, it) }
            // Encoded here rather than handed to content negotiation as an object: the body must be
            // written by the contract's own Json, the same one the server reads it with.
            setBody(NavigatorJson.encodeToString(serializer, request))
        }
    }

    /** Where this one call goes: the target when the caller named one, the configuration otherwise. */
    private suspend fun origin(target: NavigatorTarget?): String =
        target?.baseUrl?.trimEnd('/') ?: config.baseUrl.resolve()

    /**
     * Runs a call and sorts the outcome into the three cases the caller distinguishes.
     *
     * Catching broadly is deliberate here and nowhere else: every transport failure of every engine
     * on four platforms ends up in this one place, and the alternative is a list of engine specific
     * exception types that would be wrong on whichever platform was not thought of. Cancellation is
     * rethrown, because a cancelled call is not a failed one.
     */
    @Suppress("TooGenericExceptionCaught")
    private suspend inline fun <reified T> call(block: () -> HttpResponse): NavigatorResult<T> = try {
        val response = block()

        if (response.status.isSuccess()) {
            NavigatorResult.Success(response.body<T>())
        } else {
            NavigatorResult.ServerError(status = response.status.value, error = response.readError())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        NavigatorResult.TransportError(e)
    }

    /**
     * Says who is calling, when there is a token to say it with.
     *
     * A named target supplies its own token and never borrows the configured one, even when it has
     * none. Falling back would present a credential issued by one deployment to a different host,
     * which is a leak the caller did not ask for and could not see.
     */
    private suspend fun HttpRequestBuilder.presentAccessToken(target: NavigatorTarget?) {
        val token = if (target != null) target.accessToken else config.accessToken.resolve()

        token?.takeIf { it.isNotBlank() }?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    /** Alternative ways to build a client. */
    companion object {
        /**
         * Builds a client on an engine supplied by the caller — a test engine, or one shared with
         * the rest of the application. The navigator configuration is applied here, so the caller
         * only provides the engine.
         */
        fun withEngine(engine: HttpClientEngine, config: NavigatorClientConfig): NavigatorClient =
            NavigatorClient(httpClient = HttpClient(engine).withNavigatorDefaults(config), config = config)
    }
}

/**
 * The contract's account of a failure, or a stand in when the body was not one.
 *
 * A navigator always answers failures with an [ErrorResponse]. Something else on that status means
 * the request did not reach a navigator at all — a proxy, a captive portal, a wrong base URL — and
 * saying so is more useful than a decoding exception three frames up.
 */
@Suppress("TooGenericExceptionCaught")
private suspend fun HttpResponse.readError(): ErrorResponse = try {
    body<ErrorResponse>()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    if (e !is SerializationException && e !is ContentConvertException) throw e

    ErrorResponse(
        code = ErrorCode.INTERNAL,
        message = "Answered ${status.value} with a body that is not a navigator error",
    )
}
