package com.takaotech.ktravel.presentation.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.archive.ImportConflictStrategy
import com.takaotech.ktravel.domain.archive.StagedTravelArchive
import com.takaotech.ktravel.domain.archive.TravelArchiveImporter
import com.takaotech.ktravel.domain.archive.asTravelArchiveError
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class TravelSelectionViewModel(
    private val repository: TravelManagerRepository,
    private val archiveImporter: TravelArchiveImporter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelSelectionUiState())
    val uiState: StateFlow<TravelSelectionUiState> = _uiState.asStateFlow()

    /** Archivio validato in attesa della scelta dell'utente sul conflitto di id. */
    private var pendingImport: StagedTravelArchive? = null

    fun loadTravelPlans() {
        viewModelScope.launch {
            refresh()
        }
    }

    /**
     * Enters multi selection mode selecting the item the long press started from.
     */
    fun enterSelectionMode(id: String) {
        _uiState.update {
            it.copy(isSelectionMode = true, selectedIds = it.selectedIds.add(id))
        }
    }

    /**
     * Adds or removes an item from the current selection, leaving the selection mode once empty.
     */
    fun toggleSelection(id: String) {
        _uiState.update { state ->
            val selectedIds = if (id in state.selectedIds) {
                state.selectedIds.remove(id)
            } else {
                state.selectedIds.add(id)
            }
            state.copy(
                isSelectionMode = selectedIds.isNotEmpty(),
                selectedIds = selectedIds,
            )
        }
    }

    fun exitSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = false, selectedIds = persistentSetOf()) }
    }

    /**
     * Permanently deletes the given travel plans and reloads the list only once the database
     * confirmed every deletion.
     */
    fun deleteTravels(ids: Set<String>) {
        if (ids.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                ids.forEach { id -> repository.deleteTravelPlan(id) }
            }.onSuccess {
                exitSelectionMode()
                refresh()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message)
                }
            }
        }
    }

    /**
     * Legge e valida l'archivio scelto dall'utente. Se l'id è già presente si attende una scelta,
     * altrimenti l'import procede subito.
     */
    fun stageImport(source: PlatformFile) {
        viewModelScope.launch {
            _uiState.update { it.copy(import = ImportUiState.Reading) }
            archiveImporter.stage(source)
                .onSuccess { staged ->
                    pendingImport = staged
                    val existingName = staged.conflictingTravelName
                    if (existingName != null) {
                        _uiState.update {
                            it.copy(
                                import = ImportUiState.AwaitingConflictChoice(
                                    importedName = staged.travelName,
                                    existingName = existingName,
                                ),
                            )
                        }
                    } else {
                        runImport(staged, ImportConflictStrategy.DUPLICATE, nameOverride = null)
                    }
                }
                .onFailure(::failImport)
        }
    }

    /**
     * Applica la scelta fatta sul conflitto. [duplicateName] arriva già localizzato dalla UI,
     * perché `stringResource` è invocabile solo da un composable.
     */
    fun confirmImport(strategy: ImportConflictStrategy, duplicateName: String) {
        val staged = pendingImport ?: return
        viewModelScope.launch {
            runImport(
                staged = staged,
                strategy = strategy,
                nameOverride = duplicateName.takeIf { strategy == ImportConflictStrategy.DUPLICATE },
            )
        }
    }

    fun cancelImport() {
        val staged = pendingImport ?: return
        pendingImport = null
        viewModelScope.launch {
            archiveImporter.discard(staged)
            _uiState.update { it.copy(import = ImportUiState.Idle) }
        }
    }

    /** Da chiamare dopo aver mostrato l'esito dell'import. */
    fun onImportMessageShown() {
        _uiState.update { it.copy(import = ImportUiState.Idle) }
    }

    private suspend fun runImport(
        staged: StagedTravelArchive,
        strategy: ImportConflictStrategy,
        nameOverride: String?,
    ) {
        _uiState.update { it.copy(import = ImportUiState.Importing) }
        archiveImporter.import(staged, strategy, nameOverride)
            .onSuccess { summary ->
                pendingImport = null
//                if (strategy == ImportConflictStrategy.REPLACE) {
//                    // Il grafo del viaggio sostituito tiene uno stato caricato una sola volta in
//                    // init: senza rilasciarlo mostrerebbe ancora il piano precedente.
//                    planningCache.invalidate(summary.id)
//                }
                _uiState.update { it.copy(import = ImportUiState.Completed(summary.name)) }
                refresh()
            }
            .onFailure(::failImport)
    }

    private fun failImport(throwable: Throwable) {
        pendingImport = null
        _uiState.update {
            it.copy(import = ImportUiState.Failed(throwable.asTravelArchiveError()))
        }
    }

    private suspend fun refresh() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        runCatching {
            repository.getAllTravelPlans()
        }.onSuccess { plans ->
            _uiState.update { state ->
                val travelList = plans.map { plan ->
                    TravelSummaryUiState(
                        id = plan.id,
                        name = plan.name,
                        periodStart = plan.periodStart,
                        periodEnd = plan.periodEnd,
                    )
                }.toPersistentList()

                // Drops the ids that no longer exist so the selection cannot outlive its items.
                val selectedIds =
                    state.selectedIds.retainAll(travelList.mapTo(mutableSetOf()) { it.id })

                state.copy(
                    isLoading = false,
                    travelList = travelList,
                    isSelectionMode = state.isSelectionMode && selectedIds.isNotEmpty(),
                    selectedIds = selectedIds,
                )
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(isLoading = false, error = error.message)
            }
        }
    }
}
