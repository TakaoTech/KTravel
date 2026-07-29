package com.takaotech.ktravel.presentation.intro

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.LocalDate

@Stable
data class TravelSelectionUiState(
    val travelList: PersistentList<TravelSummaryUiState> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null,
    /** When true the list is in multi selection mode and taps select items instead of opening them. */
    val isSelectionMode: Boolean = false,
    val selectedIds: PersistentSet<String> = persistentSetOf()
)

@Stable
data class TravelSummaryUiState(
    val id: String,
    val name: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
)
