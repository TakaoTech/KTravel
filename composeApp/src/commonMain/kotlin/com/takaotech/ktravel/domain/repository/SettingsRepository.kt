package com.takaotech.ktravel.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val hereApiKey: StateFlow<String>

    fun updateHereApiKey(apiKey: String)
}
