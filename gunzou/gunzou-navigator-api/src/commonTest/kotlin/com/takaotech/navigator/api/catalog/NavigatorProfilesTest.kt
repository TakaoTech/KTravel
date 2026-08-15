package com.takaotech.navigator.api.catalog

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.here.HereTransportMode
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The half of the catalog that does not travel.
 *
 * What is checked here is mostly that it stays honest against the rest of the contract: a profile
 * that advertises a path nobody serves, or a mode vocabulary quietly merged with another API's, would
 * both compile and both mislead every caller that reads this list to build a selector.
 */
class NavigatorProfilesTest {

    private val routingPaths = setOf(NavigatorApi.HERE_CAR, NavigatorApi.HERE_TRANSIT)

    @Test
    fun `Given every declared profile When its path is read Then the contract serves that path`() {
        NavigatorProfile.ALL.forEach { profile ->
            assertContains(routingPaths, profile.descriptor.path, "${profile.id} advertises an unserved path")
        }
    }

    @Test
    fun `Given every declared profile When it is looked up by identity Then it is found`() {
        NavigatorProfile.ALL.forEach { profile ->
            val found = NavigatorProfile.find(profile.descriptor.provider, profile.descriptor.profile)
            assertEquals(profile, found)
            assertEquals(profile, NavigatorProfile.find(profile.descriptor))
        }
    }

    @Test
    fun `Given a profile this version does not have When it is looked up Then nothing is found`() {
        // What a client does when a newer navigator serves something it was not built against: it has
        // to degrade to "unknown", not to a wrong profile.
        assertNull(NavigatorProfile.find(ProviderId.VALHALLA, ProviderProfile.CAR))
    }

    @Test
    fun `Given the declared profiles When they are listed Then no identity appears twice`() {
        val ids = NavigatorProfile.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "Two profiles share an identity: $ids")
    }

    // ---- the vocabularies stay apart ------------------------------------------------------------

    @Test
    fun `Given the road profile When its modes are read Then they are the whole HERE routing vocabulary`() {
        // The upstream parameter is required and single valued, so every value it accepts is offerable.
        assertEquals(HereTransportMode.entries.toSet(), NavigatorProfile.HereCar.modes.toSet())
    }

    @Test
    fun `Given the transit profile When its filter is read Then it is the transit vocabulary without the unknown`() {
        assertEquals(
            TransitMode.entries.toSet() - TransitMode.OTHER,
            NavigatorProfile.HereTransit.modeFilter.toSet(),
        )
    }

    @Test
    fun `Given both HERE profiles When their vocabularies are compared Then neither borrows from the other`() {
        val road = NavigatorProfile.HereCar.modes.map { it.name }.toSet()
        val transit = NavigatorProfile.HereTransit.modeFilter.map { it.name }.toSet()

        // `BUS` and `PRIVATE_BUS` are spelled the same on both sides and mean different things: a
        // coach the traveller drives, against a scheduled service. They are allowed to collide by
        // name precisely because they are never drawn from one shared set.
        assertTrue(road.intersect(transit).isNotEmpty(), "The names really do collide; that is the point")

        // Walking is the asymmetry that proves the two are not one list: it is something you ask the
        // road profile for, and something the transit profile only ever reports back.
        assertContains(road, HereTransportMode.PEDESTRIAN.name)
        assertTrue(
            transit.none { it.contains("PEDESTRIAN") || it.contains("WALK") },
            "A walking entry in the transit filter means the two vocabularies were merged: $transit",
        )
    }

    @Test
    fun `Given the published descriptors When their modes are read Then each carries its own API vocabulary`() {
        // The descriptor is the half of the catalog that travels, so this is the check that a client
        // reading `GET /v1/profiles` is offered what the endpoint accepts and not a coarser stand-in.
        val road = NavigatorProfile.HereCar.descriptor.supportedModes
        val transit = NavigatorProfile.HereTransit.descriptor.supportedModes

        assertIs<SupportedModes.HereRoad>(road)
        assertEquals(NavigatorProfile.HereCar.modes, road.modes)

        assertIs<SupportedModes.Transit>(transit)
        assertEquals(NavigatorProfile.HereTransit.modeFilter, transit.modes)
    }

    @Test
    fun `Given the transit descriptor When its modes are read Then they are the fifteen Public Transit v8 names`() {
        // The `modes` query parameter of GET /v8/routes: highSpeedTrain, intercityTrain,
        // interRegionalTrain, regionalTrain, cityTrain, bus, ferry, subway, lightRail, privateBus,
        // inclined, aerial, busRapid, monorail, flight. `OTHER` is ours, not HERE's.
        val modes = assertIs<SupportedModes.Transit>(NavigatorProfile.HereTransit.descriptor.supportedModes).modes

        assertEquals(15, modes.size)
        assertFalse(modes.contains(TransitMode.OTHER), "OTHER is a decoding fallback, not a requestable mode")
    }

    @Test
    fun `Given the road profile When the shortest distance modes are read Then only those HERE accepts are there`() {
        // HERE Routing v8 supports `routingMode=short` on cars and trucks; everything else, taxis
        // included, is fast only. Offering it elsewhere buys a request the provider refuses.
        val shortest = NavigatorProfile.HereCar.modesSupportingShortest

        assertEquals(setOf(HereTransportMode.CAR, HereTransportMode.TRUCK), shortest)
        assertTrue(NavigatorProfile.HereCar.modes.containsAll(shortest), "A mode that is not offerable at all")
    }

    // ---- capabilities the app reads to shape its controls ----------------------------------------

    @Test
    fun `Given the road profile When its capabilities are read Then they match the HERE routing limits`() {
        val descriptor = NavigatorProfile.HereCar.descriptor

        assertEquals(6, descriptor.maxAlternatives)
        assertEquals(20, descriptor.maxVia)
        assertTrue(descriptor.supportsTolls)
        assertTrue(descriptor.requiresApiKey)
    }

    @Test
    fun `Given the transit profile When its capabilities are read Then a journey admits no via and no tolls`() {
        val descriptor = NavigatorProfile.HereTransit.descriptor

        assertEquals(5, descriptor.maxAlternatives)
        assertEquals(0, descriptor.maxVia, "Transfers are the provider's to choose, not the caller's")
        assertTrue(!descriptor.supportsTolls)
        assertTrue(descriptor.requiresApiKey)
    }

    @Test
    fun `Given a profile requiring a key When the catalog is read Then the display name is not empty`() {
        // It is the fallback a client shows for a profile it has no localized name for.
        NavigatorProfile.ALL.forEach {
            assertTrue(it.descriptor.displayName.isNotBlank(), "${it.id} has no display name")
        }
    }

    @Test
    fun `Given the profile identity When it is printed Then it reads as provider over profile`() {
        assertNotNull(NavigatorProfile.find(ProviderId.HERE, ProviderProfile.TRANSIT))
        assertEquals("here/transit", NavigatorProfile.HereTransit.id.toString())
    }
}
