package com.takaotech.navigation.common

import com.takaotech.navigation.common.dto.HereErrorResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Performs a GET against a HERE API and maps the outcome to a [HereApiResult].
 *
 * Every HERE API shares the same failure modes — an error payload on a non-2xx response, a body
 * that does not match the documented schema, a call that never reaches the service — so they are
 * handled here once instead of in each API client.
 *
 * @param url Absolute URL of the endpoint
 * @param block Request configuration, typically the query parameters of the call
 */
@Suppress("TooGenericExceptionCaught")
internal suspend inline fun <reified T> HttpClient.hereGet(
    url: String,
    crossinline block: HttpRequestBuilder.() -> Unit,
): HereApiResult<T> = try {
    val response = get(url) { block() }

    if (response.status.isSuccess()) {
        HereApiResult.Success(response.body<T>())
    } else {
        response.toHereApiError()
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    e.toHereApiError()
}

/**
 * Maps a non-2xx response to an error, parsing the HERE error payload when the body carries one.
 */
@Suppress("TooGenericExceptionCaught")
internal suspend fun HttpResponse.toHereApiError(): HereApiResult.Error {
    val errorResponse = try {
        body<HereErrorResponse>()
    } catch (e: Exception) {
        if (!e.isDeserializationFailure()) throw e
        unparsedError()
    }

    return HereApiResult.Error(httpStatusCode = status.value, errorResponse = errorResponse)
}

/**
 * Maps a call that failed before a response was read — a transport failure, or a success payload
 * that does not match the expected schema.
 */
internal fun Throwable.toHereApiError(): HereApiResult.Error = HereApiResult.Error(
    httpStatusCode = 0,
    errorResponse = HereErrorResponse(
        title = if (isDeserializationFailure()) SERIALIZATION_ERROR_TITLE else NETWORK_ERROR_TITLE,
        status = 0,
        cause = message,
    ),
    exception = this,
)

/**
 * True when the failure is about reading the body rather than about reaching the API.
 *
 * Content negotiation wraps deserialization failures in a [ContentConvertException], which is not
 * a [SerializationException], so both have to be recognised.
 */
private fun Throwable.isDeserializationFailure(): Boolean =
    this is SerializationException || this is ContentConvertException || this is NoTransformationFoundException

/**
 * Error payload for a response whose body is not a HERE error object: the raw body is kept as the
 * cause so the failure stays diagnosable.
 */
private suspend fun HttpResponse.unparsedError(): HereErrorResponse = HereErrorResponse(
    title = UNKNOWN_ERROR_TITLE,
    status = status.value,
    cause = bodyAsText(),
)

private const val UNKNOWN_ERROR_TITLE = "Unknown error"
private const val SERIALIZATION_ERROR_TITLE = "Serialization error"
private const val NETWORK_ERROR_TITLE = "Network error"
