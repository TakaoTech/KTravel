package com.takaotech.gunzou.api.search.autocomplete

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.search.SearchArea
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body of every `POST /v1/{provider}/search/autocomplete`: what the user has typed so far, and where
 * they are looking.
 *
 * One body for every provider. What a particular provider accepts of it — the largest [limit], the
 * shapes of [area], whether a location is required — is published by its
 * [com.takaotech.gunzou.api.search.SearchProfileDescriptor], and a request outside it is refused
 * before any upstream call is made.
 *
 * @property query Text typed so far, such as `colosseo` or `via del cors`. Expected not to be blank.
 * @property language IETF BCP 47 tag of the language to answer in, such as `it-IT`. Required: the
 *   titles and addresses of the suggestions are what the user reads, and a server picking a
 *   language on their behalf would answer in one they may not speak. A tag the server cannot read
 *   is refused as an invalid request.
 * @property at Where the user is looking, used to rank nearby results first. A profile that
 *   [requires a location][com.takaotech.gunzou.api.search.SearchProfileDescriptor.requiresLocation]
 *   takes either this or a circle or bounding box [area], not both.
 * @property area Where the results must lie. Results outside it are not returned.
 * @property limit Maximum number of suggestions, at least one and at most what the profile publishes.
 */
@Serializable
data class AutocompleteRequest(
    @SerialName("query") val query: String,
    @SerialName("language") val language: String,
    @SerialName("at") val at: GeoPoint? = null,
    @SerialName("area") val area: SearchArea? = null,
    @SerialName("limit") val limit: Int = DEFAULT_LIMIT,
) {
    /** Defaults of the request. */
    companion object {
        /** How many suggestions a request asks for when it does not say: about a dropdown's worth. */
        const val DEFAULT_LIMIT: Int = 10
    }
}
