package com.takaotech.ktravel.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single
class SettingsRepositoryImpl : SettingsRepository {

    private val _hereApiKey = MutableStateFlow("")
    override val hereApiKey: StateFlow<String> = _hereApiKey.asStateFlow()

    override fun updateHereApiKey(apiKey: String) {
        _hereApiKey.value = apiKey
    }
}
