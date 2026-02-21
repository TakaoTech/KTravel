package com.takaotech.ktravel.domain.routing

import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.navigation.routing.client.HereRoutingClient

class HereRoutingProvider(
    private val settingsRepository: SettingsRepository
) : RoutingProvider {

    val routingClient: HereRoutingClient
        get() = HereRoutingClient(apiKey = settingsRepository.hereApiKey.value)
}
