package com.takaotech.ktravel.presentation.planning

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.archive.TravelArchiveExporter
import com.takaotech.ktravel.domain.archive.asTravelArchiveError
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class PlanningViewModel(
    @Assisted private val travelId: String,
    private val planningGraphStore: PlanningGraphStore,
    private val archiveExporter: TravelArchiveExporter,
) : ViewModel() {

    @AssistedFactory
    @ContributesIntoMap(AppScope::class)
    @ManualViewModelAssistedFactoryKey
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(travelId: String): PlanningViewModel
    }

    private val repository get() = planningGraphStore.getOrCreate(travelId).travelPlanRepository

    private val _uiState = MutableStateFlow(PlanningUiState())
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    init {
        repository.planningState
            .onEach { domainState ->
                val domainName = domainState.name
                _uiState.update { current ->
                    // The mapper rebuilds the whole state, so anything not coming from the domain
                    // (export progress) must be carried over explicitly.
                    val mappedState = with(TravelPlanUiMapper) { domainState.toUiState() }
                        .copy(export = current.export)
                    if (domainName != current.planHeader.name.text) {
                        // External change — safe to replace TextFieldValue
                        mappedState
                    } else {
                        // Same text — preserve existing TextFieldValue (cursor/selection intact)
                        mappedState.copy(
                            planHeader = mappedState.planHeader.copy(name = current.planHeader.name),
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onPlanNameChanged(name: TextFieldValue) {
        // Immediately update UI state preserving full TextFieldValue (cursor/selection)
        _uiState.update { it.copy(planHeader = it.planHeader.copy(name = name)) }
        // Send only the String to the domain layer
        viewModelScope.launch {
            repository.updatePlanName(name.text)
        }
    }

    fun onPlanDateChanged(start: Long, end: Long) {
        viewModelScope.launch {
            repository.updatePeriod(start, end)
        }
    }

    fun onPlaceMovedToDate(placeId: String, dayId: String) {
        viewModelScope.launch {
            repository.movePlaceToDay(placeId, dayId)
        }
    }

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            repository.deletePlace(placeId, null)
        }
    }

    /**
     * True when the plan has an API key, which is what decides whether the export asks about
     * secrets at all. With no key configured the flow is unchanged from before this feature.
     */
    val hasApiKey: Boolean get() = repository.planningState.value.hereApiKey.isNotEmpty()

    /** Opens the question about including the API key. */
    fun startExport() {
        if (_uiState.value.export !is ExportUiState.Idle) return
        _uiState.update { it.copy(export = ExportUiState.AwaitingSecretsChoice) }
    }

    /** Abandons the export before a destination has even been picked. */
    fun cancelExport() {
        _uiState.update { it.copy(export = ExportUiState.Idle) }
    }

    /**
     * Exports the trip to the file the user picked, which may be an Android `content://`.
     *
     * [secretsPassword] null means the API key is left out; the plan entry never carries it either
     * way.
     */
    fun exportTravel(destination: PlatformFile, secretsPassword: String? = null) {
        if (_uiState.value.export is ExportUiState.InProgress) return

        viewModelScope.launch {
            _uiState.update { it.copy(export = ExportUiState.InProgress) }
            val export = archiveExporter.export(travelId, destination, secretsPassword)

            export
                .onSuccess { result ->
                    // Avoid flicker for high speed export
                    delay(1.seconds)
                    _uiState.update {
                        it.copy(export = ExportUiState.Completed(result.skippedAttachments.size))
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(export = ExportUiState.Failed(throwable.asTravelArchiveError()))
                    }
                }
        }
    }

    /** To be called once the export outcome has been shown, so it is not repeated on every recomposition. */
    fun onExportMessageShown() {
        _uiState.update { it.copy(export = ExportUiState.Idle) }
    }
}
