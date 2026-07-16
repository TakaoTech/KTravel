package com.takaotech.ktravel.presentation.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdown
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Presenter Circuit del dettaglio step (function-based, registrato via Metro `@CircuitInject`).
 *
 * Risolve il repository con `getOrCreate(travelId)` (vincolo V3), osserva il flow del giorno e
 * seleziona lo [StepUi.Place] con id [StepDetailScreen.stepId]. Le note vengono salvate
 * automaticamente con debounce di 1s, ma **solo se il Markdown è valido**: in caso contrario
 * [StepDetailUiState.noteInvalid] segnala l'errore e la nota non viene persistita.
 */
@CircuitInject(StepDetailScreen::class, AppScope::class)
@Composable
fun StepDetailPresenter(
    screen: StepDetailScreen,
    navigator: Navigator,
    planningGraphStore: PlanningGraphStore
): StepDetailUiState {
    val repository = remember(screen.travelId) {
        planningGraphStore.getOrCreate(screen.travelId).travelPlanRepository
    }
    val scope = rememberCoroutineScope()

    val placeFlow = remember(repository, screen.dayId, screen.stepId) {
        repository.getTravelDayFlow(screen.dayId).map { day ->
            with(TravelPlanUiMapper) { day.toUiDay() }.steps
                .filterIsInstance<StepUi.Place>()
                .firstOrNull { it.id == screen.stepId }
        }
    }
    val place: StepUi.Place? by placeFlow.collectAsState(initial = null)

    var isEditing by rememberSaveable(screen.stepId) { mutableStateOf(false) }
    var noteInvalid by rememberSaveable(screen.stepId) { mutableStateOf(false) }
    // Ultima nota digitata in attesa di salvataggio; null = nessuna modifica pendente.
    var pendingNote by remember(screen.stepId) { mutableStateOf<String?>(null) }

    // Salvataggio automatico con debounce di 1s.
    LaunchedEffect(pendingNote) {
        val note = pendingNote ?: return@LaunchedEffect
        if (note == place?.note) return@LaunchedEffect
        delay(NOTE_SAVE_DEBOUNCE_MS)
        noteInvalid = !repository.persistNoteIfValid(screen.dayId, screen.stepId, note)
    }

    return StepDetailUiState(
        place = place,
        isEditing = isEditing,
        noteInvalid = noteInvalid
    ) { event ->
        when (event) {
            StepDetailEvent.NavigateBack -> navigator.pop()

            is StepDetailEvent.ToggleEdit -> {
                isEditing = event.editing
                // Uscendo dalla modifica: flush immediato della nota pendente (senza attendere il debounce).
                if (!event.editing) {
                    val note = pendingNote
                    if (note != null && note != place?.note) {
                        scope.launch {
                            noteInvalid = !repository.persistNoteIfValid(
                                screen.dayId, screen.stepId, note
                            )
                        }
                    }
                }
            }

            is StepDetailEvent.NoteChanged -> {
                pendingNote = event.note
            }
        }
    }
}

private const val NOTE_SAVE_DEBOUNCE_MS = 1000L

/**
 * Persiste la nota solo se il Markdown è valido. Ritorna `true` se salvata, `false` se il Markdown
 * non è valido (nota non persistita).
 */
private suspend fun TravelPlanRepository.persistNoteIfValid(
    dayId: String,
    stepId: String,
    note: String
): Boolean = if (isValidMarkdown(note)) {
    updatePlaceNote(dayId, stepId, note)
    true
} else {
    false
}

/** Valida il Markdown tentandone il parse (parser GFM di mikepenz). */
internal fun isValidMarkdown(text: String): Boolean =
    runCatching { parseMarkdown(text) is State.Success }.getOrDefault(false)
