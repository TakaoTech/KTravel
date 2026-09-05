package com.takaotech.ktravel.presentation.travels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.archive.ImportConflictStrategy
import com.takaotech.ktravel.domain.archive.StagedTravelArchive
import com.takaotech.ktravel.domain.archive.TravelArchiveError
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
class TravelListViewModel(
    private val repository: TravelManagerRepository,
    private val archiveImporter: TravelArchiveImporter,
    private val planningGraphStore: PlanningGraphStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelListUiState())
    val uiState: StateFlow<TravelListUiState> = _uiState.asStateFlow()

    /** Validated archive awaiting the user's choice on the id conflict. */
    private var pendingImport: StagedTravelArchive? = null

    /**
     * The conflict choice, held between the dialogs.
     *
     * The import now spans up to three questions — conflict, secrets, password — and nothing is
     * written until the last one is answered, so the earlier answers have to live somewhere.
     */
    private var pendingStrategy: ImportConflictStrategy? = null
    private var pendingNameOverride: String? = null

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
     * Reads and validates the archive the user picked. When the id is already there the choice is
     * awaited, otherwise the flow moves straight to the credentials question (or to the import,
     * when there are none).
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
                        pendingStrategy = ImportConflictStrategy.DUPLICATE
                        pendingNameOverride = null
                        continueAfterConflictChoice(staged)
                    }
                }
                .onFailure(::failImport)
        }
    }

    /**
     * Applies the choice made on the conflict. [duplicateName] arrives already localized from the
     * UI, because `stringResource` can only be called from a composable.
     *
     * The choice is remembered rather than acted on immediately: the secrets question comes next,
     * and only then is anything written.
     */
    fun confirmImport(strategy: ImportConflictStrategy, duplicateName: String) {
        val staged = pendingImport ?: return
        pendingStrategy = strategy
        pendingNameOverride = duplicateName.takeIf { strategy == ImportConflictStrategy.DUPLICATE }
        viewModelScope.launch { continueAfterConflictChoice(staged) }
    }

    /** Answers "do you want the API key too?". Declining imports the plan without it. */
    fun onSecretsChoice(includeSecrets: Boolean) {
        val staged = pendingImport ?: return
        if (includeSecrets) {
            _uiState.update { it.copy(import = ImportUiState.AwaitingSecretsPassword()) }
        } else {
            viewModelScope.launch { runImport(staged, secretsPassword = null) }
        }
    }

    /**
     * Tries to unlock the archived key.
     *
     * A wrong password comes back as a retry rather than a failure: nothing was written, and the
     * staged archive is still there.
     */
    fun submitSecretsPassword(password: String) {
        val staged = pendingImport ?: return
        viewModelScope.launch { runImport(staged, secretsPassword = password) }
    }

    private suspend fun continueAfterConflictChoice(staged: StagedTravelArchive) {
        if (staged.hasSecrets) {
            _uiState.update { it.copy(import = ImportUiState.AwaitingSecretsChoice) }
        } else {
            runImport(staged, secretsPassword = null)
        }
    }

    fun cancelImport() {
        val staged = pendingImport ?: return
        pendingImport = null
        pendingStrategy = null
        pendingNameOverride = null
        viewModelScope.launch {
            archiveImporter.discard(staged)
            _uiState.update { it.copy(import = ImportUiState.Idle) }
        }
    }

    /** To be called once the import outcome has been shown. */
    fun onImportMessageShown() {
        _uiState.update { it.copy(import = ImportUiState.Idle) }
    }

    private suspend fun runImport(staged: StagedTravelArchive, secretsPassword: String?) {
        val strategy = pendingStrategy ?: ImportConflictStrategy.DUPLICATE

        _uiState.update { it.copy(import = ImportUiState.Importing) }
        archiveImporter.import(staged, strategy, pendingNameOverride, secretsPassword)
            .onSuccess { summary ->
                pendingImport = null
                pendingStrategy = null
                pendingNameOverride = null
                if (strategy == ImportConflictStrategy.REPLACE) {
                    // The graph of the replaced trip holds a state loaded once in init: without
                    // releasing it, it would still show the previous plan.
                    planningGraphStore.release(summary.id)
                }
                _uiState.update { it.copy(import = ImportUiState.Completed(summary.name)) }
                refresh()
            }
            .onFailure { throwable ->
                // A wrong password is the one failure that is not terminal: the archive is still
                // staged, so the user goes back to the prompt instead of starting over.
                if (throwable.asTravelArchiveError() == TravelArchiveError.WrongPassword) {
                    _uiState.update {
                        it.copy(import = ImportUiState.AwaitingSecretsPassword(attemptFailed = true))
                    }
                } else {
                    failImport(throwable)
                }
            }
    }

    private fun failImport(throwable: Throwable) {
        pendingImport = null
        pendingStrategy = null
        pendingNameOverride = null
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

                val selectedIds =
                    state.selectedIds.retainingAll(travelList.mapTo(mutableSetOf()) { it.id })

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
