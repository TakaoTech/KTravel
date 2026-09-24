package com.takaotech.gunzou.here.search.lookup.dto.request

import com.takaotech.gunzou.here.search.lookup.model.LookupShowOption
import com.vanniktech.locale.Locale

/**
 * Request parameters for the Lookup endpoint: the details of a result already known by its HERE
 * identifier.
 *
 * @property id HERE identifier of the result, as returned by Browse, Autosuggest, Reverse Geocode or
 *   any other Search endpoint
 * @property lang Preferred languages for the response, most preferred first
 * @property show Optional sections to add to the result
 * @property requestId Identifier echoed in error responses, sent as the `X-Request-ID` header, to
 *   correlate a request with its response in logs. A UUID is recommended.
 */
data class LookupRequest(
    val id: String,
    val lang: List<Locale>,
    val show: List<LookupShowOption>? = null,
    val requestId: String? = null,
) {
    init {
        require(id.isNotBlank()) { "Id cannot be blank" }
        require(lang.isNotEmpty()) { "Language list cannot be empty" }
    }
}
