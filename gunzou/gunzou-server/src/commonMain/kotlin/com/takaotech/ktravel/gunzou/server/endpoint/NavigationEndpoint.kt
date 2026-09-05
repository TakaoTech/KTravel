package com.takaotech.ktravel.gunzou.server.endpoint

import com.takaotech.gunzou.api.catalog.ProviderProfileDescriptor
import com.takaotech.gunzou.api.common.RouteTime
import com.takaotech.gunzou.api.error.ErrorCode
import kotlin.jvm.JvmInline

/**
 * One routing profile of one provider: everything behind a single path.
 *
 * Deliberately thin. Ktor already dispatches on the path, so this is not a registry and there is no
 * `when` over providers anywhere in the server — a request for `/v1/here/routing/car` can only ever
 * reach the endpoint mounted there. What is left for the interface to guarantee is the failure half
 * of the contract: however different two profiles are, both refuse with the same
 * [com.takaotech.gunzou.api.error.ErrorResponse].
 *
 * The answer is a type parameter and not one shared response, because a road route and a journey on
 * scheduled services are not one thing described twice. What they do have in common is the machinery
 * around them — credentials, published limits, error codes — and that is what lives here.
 *
 * [REQ] is contravariant so an endpoint declared against a supertype of the body still satisfies the
 * path that receives it; [RES] is covariant for the mirror reason.
 */
interface NavigationEndpoint<in REQ : Any, out RES : Any> {
    /** What this profile can do, published verbatim through `GET /v1/profiles`. */
    val descriptor: ProviderProfileDescriptor

    /**
     * Computes the routes for [request].
     *
     * @param credentials The caller's key for the upstream provider, already checked to be present
     *   when [ProviderProfileDescriptor.requiresApiKey] says so. Null only for a profile that needs
     *   none.
     * @throws NavigatorException for every failure a caller is meant to act on. Anything else
     *   escaping this method is a bug and is reported as
     *   [com.takaotech.gunzou.api.error.ErrorCode.INTERNAL].
     */
    suspend fun route(request: REQ, credentials: ProviderCredentials?): RES
}

/**
 * The caller's credentials for the provider behind a profile.
 *
 * The key belongs to the caller, not to the server: embedded, the server runs inside the app that
 * owns the key, and nothing about that changes when the same app points at a remote deployment. It
 * therefore arrives on the request and is never stored.
 *
 * @property apiKey The provider key, verbatim.
 */
@JvmInline
value class ProviderCredentials(val apiKey: String)

/**
 * Rejects what this profile has published that it cannot do, before any upstream call is paid for.
 *
 * These limits live here and not in the request validation plugin because they are the descriptor's
 * own words: `GET /v1/profiles` promises a ceiling on alternatives and via waypoints, and this is
 * what makes the promise true. Validation that holds for every provider — a coordinate off the
 * globe, a count below one — belongs in the plugin instead, where it runs once for every path.
 *
 * @throws NavigatorException [ErrorCode.INVALID_REQUEST] when a published limit is exceeded, and
 *   [ErrorCode.UNSUPPORTED_OPTION] when the request asks for something the profile does not offer.
 */
fun ProviderProfileDescriptor.requireWithinLimits(alternatives: Int, viaCount: Int, time: RouteTime) {
    val violation = when {
        alternatives > maxAlternatives ->
            ErrorCode.INVALID_REQUEST to "$profile accepts at most $maxAlternatives alternatives, $alternatives asked"

        viaCount > maxVia ->
            ErrorCode.INVALID_REQUEST to "$profile accepts at most $maxVia via waypoints, $viaCount asked"

        time is RouteTime.ArriveBy && !supportsArriveBy ->
            ErrorCode.UNSUPPORTED_OPTION to "$profile cannot plan backwards from an arrival time"

        else -> null
    }

    violation?.let { (code, message) -> throw NavigatorException(code = code, message = message) }
}
