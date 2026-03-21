package com.takaotech.ktravel.presentation.intro

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

@Stable
data class TravelSelectionUiState(
    val travelList: PersistentList<TravelSummaryUiState> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@Stable
data class TravelSummaryUiState(
    val id: String,
    val name: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
)
