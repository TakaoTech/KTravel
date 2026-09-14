package com.takaotech.gunzou.api.search

import com.takaotech.gunzou.api.NavigatorApi
import com.takaotech.gunzou.api.common.SearchProviderId

/**
 * Every search service this version of the contract supports.
 *
 * The search counterpart of [com.takaotech.gunzou.api.catalog.NavigatorProfile]: the half of the
 * search catalog that does not travel, which `GET /v1/search/profiles` is subtracted from.
 *
 * Every service answers in one provider neutral body, so what tells two providers of the same
 * service apart is not what they return but what they can be asked, and that is what the descriptor
 * publishes.
 */
sealed class SearchProfile {

    /** What the navigator publishes about this service. */
    abstract val descriptor: SearchProfileDescriptor

    /**
     * Autocomplete through HERE Geocoding and Search v7 Autosuggest.
     *
     * Limits from the Autosuggest specification: at most a hundred results, and a request located
     * either by a point or by a circle or bounding box — a country filter alone is refused upstream.
     */
    data object HereAutocomplete : SearchProfile() {
        override val descriptor: SearchProfileDescriptor = SearchProfileDescriptor(
            provider = SearchProviderId.Here,
            service = SearchService.AUTOCOMPLETE,
            path = NavigatorApi.HERE_SEARCH_AUTOCOMPLETE,
            displayName = "HERE autocomplete",
            maxResults = 100,
            supportedAreas = listOf(SearchAreaKind.COUNTRIES, SearchAreaKind.CIRCLE, SearchAreaKind.BOUNDING_BOX),
            requiresLocation = true,
            supportsQuerySuggestions = true,
            supportsHighlights = true,
            requiresApiKey = true,
        )
    }

    /** The whole list of services, and the lookups over it. */
    companion object {

        /**
         * Every search service, in the order a selector should offer them.
         *
         * Lazy for the same initialization order reason as
         * [com.takaotech.gunzou.api.catalog.NavigatorProfile.ALL]: built eagerly, it would capture
         * the nested objects before their own initializers ran.
         */
        val ALL: List<SearchProfile> by lazy { listOf(HereAutocomplete) }

        /** The service with this identity, or `null` when the caller knows one this version does not. */
        fun find(provider: SearchProviderId, service: SearchService): SearchProfile? =
            ALL.firstOrNull { it.descriptor.provider == provider && it.descriptor.service == service }

        /** The service a served [descriptor] refers to, matched on identity rather than on contents. */
        fun find(descriptor: SearchProfileDescriptor): SearchProfile? = find(descriptor.provider, descriptor.service)
    }
}
