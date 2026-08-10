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
 * Progress of an archive import.
 *
 * It holds stable types only: the staged archive lives in a private field of the ViewModel, because
 * carrying it here would make the whole list state unstable.
 */
@Immutable
sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Reading : ImportUiState

    /** A trip with the same id is already there: the user has to choose. */
    data class AwaitingConflictChoice(val importedName: String, val existingName: String) : ImportUiState

    /**
     * The archive carries an encrypted API key: does the user want it?
     *
     * Comes after the conflict choice, so the user decides what happens to the trip before being
     * asked about its credentials.
     */
    data object AwaitingSecretsChoice : ImportUiState

    /**
     * The user asked for the key and now has to unlock it.
     *
     * [attemptFailed] keeps the dialog open after a wrong password instead of aborting the import:
     * nothing has been written yet, so retrying costs nothing.
     */
    data class AwaitingSecretsPassword(val attemptFailed: Boolean = false) : ImportUiState

    data object Importing : ImportUiState
    data class Completed(val travelName: String) : ImportUiState
    data class Failed(val error: TravelArchiveError) : ImportUiState
}

@Stable
data class TravelSummaryUiState(val id: String, val name: String, val periodStart: LocalDate, val periodEnd: LocalDate)
