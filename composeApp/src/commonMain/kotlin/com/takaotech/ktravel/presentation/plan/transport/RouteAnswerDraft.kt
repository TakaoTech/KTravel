package com.takaotech.ktravel.presentation.plan.transport

import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.model.RouteResult
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * What the navigator answered, and which of its alternatives the traveller is looking at.
 *
 * Two screens read this: the composer that asked for it, and the preview that shows it. They used
 * to share it by sharing a view model scoped to the navigation graph that contained both — a shape
 * with no equivalent once the back stack is Circuit's, which has no nested graphs. Scoping the
 * answer to the plan instead makes the two screens independent of how they are hosted.
 *
 * @see RouteOptionsDraft for the other half, the request that produced this.
 */
@SingleIn(PlanningGraphScope::class)
@Inject
class RouteAnswerDraft {

    private val current = MutableStateFlow(RouteAnswer())

    /** The answer as it stands, empty until a calculation succeeds. */
    val answer: StateFlow<RouteAnswer> = current.asStateFlow()

    /**
     * Records a fresh answer, always on its first alternative.
     *
     * @param result What the navigator returned.
     * @param request The request it returned it for, kept because filing the leg needs it.
     */
    fun publish(result: RouteResult, request: RouteSelection) {
        current.value = RouteAnswer(result = result, requestUsed = request, selectedIndex = 0)
    }

    /** Moves to another alternative of the same answer. */
    fun select(index: Int) = current.update { it.copy(selectedIndex = index) }

    /** Forgets the answer, which is what asking for a new one means. */
    fun clear() {
        current.value = RouteAnswer()
    }
}

/**
 * One answer from the navigator.
 *
 * @property result The alternatives, or null before anything has been computed.
 * @property requestUsed The request they were computed for, needed to file the chosen one.
 * @property selectedIndex Which alternative is being shown.
 */
data class RouteAnswer(
    val result: RouteResult? = null,
    val requestUsed: RouteSelection? = null,
    val selectedIndex: Int = 0,
)
