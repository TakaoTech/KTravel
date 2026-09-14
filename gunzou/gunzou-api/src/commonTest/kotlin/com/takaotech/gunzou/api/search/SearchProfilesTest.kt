package com.takaotech.gunzou.api.search

import com.takaotech.gunzou.api.NavigatorApi
import com.takaotech.gunzou.api.NavigatorJson
import com.takaotech.gunzou.api.common.SearchProviderId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class SearchProfilesTest {

    @Test
    fun `Given the HERE autocomplete When its descriptor is read Then it is served at the HERE search path`() {
        val descriptor = SearchProfile.HereAutocomplete.descriptor

        assertEquals(NavigatorApi.HERE_SEARCH_AUTOCOMPLETE, descriptor.path)
        assertEquals(SearchProviderId.Here, descriptor.provider)
        assertEquals(SearchService.AUTOCOMPLETE, descriptor.service)
    }

    @Test
    fun `Given every declared search service When it is looked up by identity Then it is found`() {
        SearchProfile.ALL.forEach { profile ->
            assertSame(profile, SearchProfile.find(profile.descriptor))
        }
    }

    @Test
    fun `Given a search service this version does not have When it is looked up Then nothing is found`() {
        assertNull(SearchProfile.find(SearchProviderId.Photon, SearchService("lookup")))
    }

    @Test
    fun `Given the declared services When they are listed Then none is missing its descriptor`() {
        // Guards the lazy list: built eagerly it would hold nested objects not initialized yet.
        assertEquals(SearchProfile.ALL.size, SearchProfile.ALL.map { it.descriptor.path }.toSet().size)
    }

    @Test
    fun `Given a catalog from an older server When decoding Then the missing capabilities are conservative`() {
        val json = """
            {
              "profiles": [
                {
                  "provider": "photon",
                  "service": "autocomplete",
                  "path": "/v1/photon/search/autocomplete",
                  "displayName": "Photon autocomplete",
                  "maxResults": 50
                }
              ]
            }
        """.trimIndent()

        val descriptor = NavigatorJson.decodeFromString(SearchCatalogResponse.serializer(), json).profiles.single()

        assertEquals(emptyList(), descriptor.supportedAreas)
        assertEquals(false, descriptor.requiresLocation)
        assertEquals(false, descriptor.supportsQuerySuggestions)
        assertEquals(false, descriptor.requiresApiKey)
    }
}
