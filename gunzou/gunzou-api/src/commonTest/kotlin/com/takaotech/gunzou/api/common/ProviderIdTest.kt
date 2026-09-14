package com.takaotech.gunzou.api.common

import com.takaotech.gunzou.api.NavigatorJson
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class ProviderIdTest {

    @Test
    fun `Given a known routing provider When encoding Then it travels as its bare id`() {
        assertEquals(
            JsonPrimitive("valhalla"),
            NavigatorJson.encodeToJsonElement(RoutingProviderId.serializer(), RoutingProviderId.Valhalla),
        )
    }

    @Test
    fun `Given a known id When decoding a routing provider Then the matching case is returned`() {
        assertSame(RoutingProviderId.Here, NavigatorJson.decodeFromString(RoutingProviderId.serializer(), "\"here\""))
        assertSame(
            RoutingProviderId.Valhalla,
            NavigatorJson.decodeFromString(RoutingProviderId.serializer(), "\"valhalla\""),
        )
        assertSame(RoutingProviderId.Osrm, NavigatorJson.decodeFromString(RoutingProviderId.serializer(), "\"osrm\""))
        assertSame(
            RoutingProviderId.OpenTripPlanner,
            NavigatorJson.decodeFromString(RoutingProviderId.serializer(), "\"otp\""),
        )
    }

    @Test
    fun `Given a known id When decoding a search provider Then the matching case is returned`() {
        assertSame(SearchProviderId.Here, NavigatorJson.decodeFromString(SearchProviderId.serializer(), "\"here\""))
        assertSame(SearchProviderId.Photon, NavigatorJson.decodeFromString(SearchProviderId.serializer(), "\"photon\""))
    }

    @Test
    fun `Given an id this build does not know When decoding Then it is kept as unknown and encoded back unchanged`() {
        val decoded = NavigatorJson.decodeFromString(SearchProviderId.serializer(), "\"nominatim\"")

        assertIs<SearchProviderId.Unknown>(decoded)
        assertEquals(SearchProviderId.from("nominatim"), decoded)
        assertEquals(
            JsonPrimitive("nominatim"),
            NavigatorJson.encodeToJsonElement(SearchProviderId.serializer(), decoded),
        )
    }

    @Test
    fun `Given an engine of another service When resolving a routing provider Then it is unknown`() {
        assertIs<RoutingProviderId.Unknown>(RoutingProviderId.from("photon"))
    }

    @Test
    fun `Given HERE for routing and for search When compared Then they are different ids with the same value`() {
        assertNotEquals<ProviderId>(RoutingProviderId.Here, SearchProviderId.Here)
        assertEquals(RoutingProviderId.Here.value, SearchProviderId.Here.value)
    }
}
