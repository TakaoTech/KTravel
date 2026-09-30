package com.takaotech.ktravel.gunzou.server.endpoint.here.search

import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.here.search.lookup.dto.request.LookupRequest
import com.takaotech.gunzou.here.search.lookup.model.LookupShowOption
import com.takaotech.ktravel.gunzou.server.endpoint.NavigatorException
import com.takaotech.ktravel.gunzou.server.endpoint.search.PlaceLookupCall
import com.vanniktech.locale.Locale

// What this server sends to HERE Lookup.

/**
 * Builds the vendor request HERE Lookup expects.
 *
 * The time zone is asked for on every call: HERE leaves it out otherwise, and without it the opening
 * hours are local times of an unknown place on the clock.
 *
 * @throws NavigatorException [ErrorCode.INVALID_REQUEST] for a blank identifier or a language that
 *   is not a locale. The route already refuses both; this is what keeps a gap there from becoming an
 *   internal error here.
 */
internal fun PlaceLookupCall.toHereLookupRequest(): LookupRequest {
    if (id.isBlank()) {
        throw NavigatorException(code = ErrorCode.INVALID_REQUEST, message = "id cannot be blank")
    }

    val locale = Locale.fromOrNull(language) ?: throw NavigatorException(
        code = ErrorCode.INVALID_REQUEST,
        message = "language: '$language' is not an IETF BCP 47 tag",
    )

    return LookupRequest(id = id, lang = listOf(locale), show = listOf(LookupShowOption.TIME_ZONE))
}
