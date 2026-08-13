package com.takaotech.navigator.api.catalog

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.NavigatorJson
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.error.ErrorResponse
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CatalogAndErrorSerializationTest {

    private val catalog = ProviderCatalogResponse(
        profiles = listOf(
            ProviderProfileDescriptor(
                provider = ProviderId.HERE,
                profile = ProviderProfile.CAR,
                path = NavigatorApi.HERE_CAR,
                displayName = "HERE road routing",
                supportedModes = listOf(
                    TravelMode.CAR,
                    TravelMode.TRUCK,
                    TravelMode.PEDESTRIAN,
                    TravelMode.BICYCLE,
                    TravelMode.SCOOTER,
                ),
                maxAlternatives = 6,
                maxVia = 20,
                supportsArriveBy = true,
                supportsTolls = true,
                requiresApiKey = true,
            ),
            ProviderProfileDescriptor(
                provider = ProviderId.HERE,
                profile = ProviderProfile.TRANSIT,
                path = NavigatorApi.HERE_TRANSIT,
                displayName = "HERE public transit",
                supportedModes = listOf(TravelMode.TRANSIT, TravelMode.PEDESTRIAN),
                maxAlternatives = 5,
                supportsArriveBy = true,
                requiresApiKey = true,
            ),
        ),
    )

    @Test
    fun `Given a catalog of two profiles When encoding and decoding Then it is unchanged`() {
        val encoded = NavigatorJson.encodeToString(ProviderCatalogResponse.serializer(), catalog)

        assertEquals(catalog, NavigatorJson.decodeFromString(ProviderCatalogResponse.serializer(), encoded))
    }

    @Test
    fun `Given a descriptor When decoding Then the path it advertises is the one the client would call`() {
        val encoded = NavigatorJson.encodeToString(ProviderCatalogResponse.serializer(), catalog)

        val paths = NavigatorJson.decodeFromString(ProviderCatalogResponse.serializer(), encoded)
            .profiles.map { it.path }

        assertEquals(listOf(NavigatorApi.HERE_CAR, NavigatorApi.HERE_TRANSIT), paths)
    }

    @Test
    fun `Given a server that returns no profiles When decoding Then the list is empty rather than absent`() {
        val decoded = NavigatorJson.decodeFromString(ProviderCatalogResponse.serializer(), "{}")

        assertEquals(emptyList(), decoded.profiles)
    }

    @Test
    fun `Given a failure carrying the provider own words When encoding and decoding Then they survive`() {
        val error = ErrorResponse(
            code = ErrorCode.PROVIDER_UNAUTHORIZED,
            message = "The HERE routing API rejected the supplied key",
            providerStatus = 401,
            providerMessage = "Invalid credentials for this resource",
        )

        val encoded = NavigatorJson.encodeToString(ErrorResponse.serializer(), error)

        assertEquals(error, NavigatorJson.decodeFromString(ErrorResponse.serializer(), encoded))
    }

    @Test
    fun `Given a failure with no upstream behind it When encoding Then the provider fields are omitted`() {
        val error = ErrorResponse(code = ErrorCode.INVALID_REQUEST, message = "alternatives must be at least 1")

        val encoded = NavigatorJson.encodeToJsonElement(ErrorResponse.serializer(), error).jsonObject

        assertFalse(encoded.containsKey("providerStatus"))
        assertFalse(encoded.containsKey("providerMessage"))
    }

    @Test
    fun `Given a health payload When encoding and decoding Then the status defaults to ok`() {
        val health = HealthResponse(version = "1.0.0-SNAPSHOT")

        val encoded = NavigatorJson.encodeToString(HealthResponse.serializer(), health)
        val decoded = NavigatorJson.decodeFromString(HealthResponse.serializer(), encoded)

        assertEquals(HealthResponse.STATUS_OK, decoded.status)
        assertEquals("1.0.0-SNAPSHOT", decoded.version)
    }
}
