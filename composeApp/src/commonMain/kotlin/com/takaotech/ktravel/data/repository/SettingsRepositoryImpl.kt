package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.repository.SettingsRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SettingsRepositoryImpl @Inject constructor() : SettingsRepository {

    private val _hereApiKey = MutableStateFlow("")
    override val hereApiKey: StateFlow<String> = _hereApiKey.asStateFlow()

    override fun updateHereApiKey(apiKey: String) {
        _hereApiKey.value = apiKey
    }
}
