package com.takaotech.ktravel.presentation.plan.day

import com.takaotech.ktravel.presentation.plan.StepUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * One row of the itinerary, as the list draws it.
 *
 * Where a leg can be added is worked out here rather than while drawing, so the list stays a
 * renderer and never has to reason about which row sits next to which.
 *
 * [key] is stable and unique, which is what `LazyColumn` needs to keep rows in place across edits.
 */
sealed interface StepRow {
    val key: String

    data class Step(val step: StepUi) : StepRow {
        override val key: String get() = step.id
    }

    /**
     * The gap between two places, where a leg joining [startPlaceId] to [endPlaceId] can be added.
     */
    data class AddTransportSlot(val startPlaceId: String, val endPlaceId: String) : StepRow {
        override val key: String get() = "add_transport_${startPlaceId}_$endPlaceId"
    }
}

/**
 * Lays the steps out as rows, opening a [StepRow.AddTransportSlot] after every [StepUi.Place] that
 * is not already followed by a [StepUi.Transport].
 *
 * No gap is opened after a leg, even when a place follows it: two legs in a row is not something
 * the itinerary offers to build, and changing that is a product decision rather than a rendering
 * one.
 */
fun buildStepRows(steps: List<StepUi>): ImmutableList<StepRow> = buildList {
    steps.forEachIndexed { index, step ->
        add(StepRow.Step(step))

        val next = steps.getOrNull(index + 1)
        if (step is StepUi.Place && next != null && next !is StepUi.Transport) {
            add(StepRow.AddTransportSlot(startPlaceId = step.id, endPlaceId = next.id))
        }
    }
}.toPersistentList()

/**
 * The two places a transport sits between: the [StepUi.Place] before it and the one after.
 *
 * Null when either is missing, which is the same rule the transport screen is keyed on — without
 * two places there is nothing to compute a route between. Reads the steps out of the rows rather
 * than taking a separate list, because the rows are what the presenter already holds and the slots
 * between them carry no step of their own.
 */
fun List<StepRow>.transportNeighbours(stepId: String): Pair<String, String>? {
    val steps = filterIsInstance<StepRow.Step>().map(StepRow.Step::step)
    val index = steps.indexOfFirst { it.id == stepId }
    if (index == -1) return null

    val before = steps.take(index).lastOrNull { it is StepUi.Place } ?: return null
    val after = steps.drop(index + 1).firstOrNull { it is StepUi.Place } ?: return null

    return before.id to after.id
}
