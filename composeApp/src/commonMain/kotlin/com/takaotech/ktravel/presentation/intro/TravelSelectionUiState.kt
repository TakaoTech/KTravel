package com.takaotech.ktravel.presentation.intro

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.takaotech.ktravel.domain.archive.TravelArchiveError
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
    val selectedIds: PersistentSet<String> = persistentSetOf(),
    val import: ImportUiState = ImportUiState.Idle,
)

/**
 * Avanzamento dell'import di un archivio.
 *
 * Contiene solo tipi stabili: l'archivio in staging vive in un campo privato del ViewModel, perché
 * portarlo qui renderebbe instabile l'intero stato della lista.
 */
@Immutable
sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Reading : ImportUiState

    /** Un viaggio con lo stesso id è già presente: serve una scelta dell'utente. */
    data class AwaitingConflictChoice(val importedName: String, val existingName: String) :
        ImportUiState

    data object Importing : ImportUiState
    data class Completed(val travelName: String) : ImportUiState
    data class Failed(val error: TravelArchiveError) : ImportUiState
}

@Stable
data class TravelSummaryUiState(
    val id: String,
    val name: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate
)
