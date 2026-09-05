package com.takaotech.ktravel.presentation.plan

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import kotlinx.serialization.Serializable

/**
 * One trip, open: its name, its dates and the days it is made of.
 *
 * The root of everything scoped to a trip, and the destination whose lifetime the trip's object
 * graph follows.
 *
 * @property travelId The trip being planned.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class PlanOverviewScreen(val travelId: String) : Screen

/**
 * Everything the presenter owns for this screen, which for now is only where it can go next.
 *
 * The screen's own state — what it shows and what the user is editing — still lives in its view
 * model, and the UI reads it from there. Once that view model becomes the presenter, its state
 * replaces this type and the two halves are one again.
 *
 * @property eventSink Where the screen sends what the user did.
 */
data class PlanOverviewNavState(val eventSink: (PlanOverviewEvent) -> Unit) : CircuitUiState

/** What the trip overview can ask for. */
sealed interface PlanOverviewEvent : CircuitUiEvent {
    /** The traveller left the trip. */
    data object Back : PlanOverviewEvent

    /** A place is being added to the trip rather than to a day. */
    data object AddPlace : PlanOverviewEvent

    /**
     * A day was opened.
     *
     * @property dayId The day to open.
     */
    data class OpenDay(val dayId: String) : PlanOverviewEvent

    /** The traveller wants the settings of this trip. */
    data object OpenSettings : PlanOverviewEvent
}
