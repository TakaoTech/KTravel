package com.takaotech.gunzou.api.search

import com.takaotech.gunzou.api.common.SearchProviderId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * What one search service of one provider can do.
 *
 * The search counterpart of [com.takaotech.gunzou.api.catalog.ProviderProfileDescriptor], kept
 * apart from it because the two share nothing past the identity: a search has no alternatives, no
 * via waypoints and no tolls, and a routing profile has no result limit or area filter.
 *
 * Every capability defaults to the most conservative answer, so a descriptor from a server older
 * than one of these fields still decodes and promises nothing it did not say.
 *
 * @property provider The engine.
 * @property service Which of its search services this describes.
 * @property path Where to POST a request for it, such as `/v1/here/search/autocomplete`.
 * @property displayName A name to put in a selector. English, like everything else in the contract.
 * @property maxResults The largest `limit` a request may ask for.
 * @property supportedAreas The shapes of [SearchArea] it accepts. Empty means it takes no area.
 * @property requiresLocation Whether a request has to say where the user is looking: either an `at`
 *   point, or an area that locates the search on its own — a circle or a bounding box. A countries
 *   filter narrows the results without locating them, so it still needs `at` here.
 * @property supportsQuerySuggestions Whether the answer can carry
 *   [com.takaotech.gunzou.api.search.autocomplete.AutocompleteSuggestion.Query] items and term
 *   suggestions, or only places.
 * @property supportsHighlights Whether results say which parts of their text matched the query.
 * @property requiresApiKey Whether a call without a provider key will be rejected.
 */
@Serializable
data class SearchProfileDescriptor(
    @SerialName("provider") val provider: SearchProviderId,
    @SerialName("service") val service: SearchService,
    @SerialName("path") val path: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("maxResults") val maxResults: Int,
    @SerialName("supportedAreas") val supportedAreas: List<SearchAreaKind> = emptyList(),
    @SerialName("requiresLocation") val requiresLocation: Boolean = false,
    @SerialName("supportsQuerySuggestions") val supportsQuerySuggestions: Boolean = false,
    @SerialName("supportsHighlights") val supportsHighlights: Boolean = false,
    @SerialName("requiresApiKey") val requiresApiKey: Boolean = false,
)

/**
 * Body of `GET /v1/search/profiles`: what one navigator can search with.
 *
 * @property profiles The search services this deployment actually mounts. As with the routing
 *   catalog, subtracting them from [SearchProfile.ALL] tells "this server does not serve it" from
 *   "this version cannot ask for it".
 * @property version The build answering, defaulted so that a response from an older server decodes.
 */
@Serializable
data class SearchCatalogResponse(
    @SerialName("profiles") val profiles: List<SearchProfileDescriptor> = emptyList(),
    @SerialName("version") val version: String = "",
)
