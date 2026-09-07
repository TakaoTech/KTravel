package com.takaotech.ktravel.navigation.interceptor

import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.navigation.intercepting.InterceptedResult
import com.slack.circuitx.navigation.intercepting.NavigationContext
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor
import com.takaotech.ktravel.core.logging.LogScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.presentation.plan.PlanOverviewScreen

/**
 * Opens and closes a trip's object graph around the navigation that enters and leaves it.
 *
 * This is the one cross-cutting rule the navigation host used to enforce by hand, in three places
 * that had to stay in agreement about every way into and out of a trip. Stated once, at the point
 * navigation actually happens, it cannot fall out of step with itself.
 *
 * It also stamps the open trip onto the log: navigation is the only place that knows which trip the
 * user is inside, and a diagnostics screen opened from a trip shows only that trip's lines.
 *
 * @param store The store holding one object graph per open trip.
 * @param logScope The trip every log line written from now on belongs to.
 */
internal class PlanningGraphInterceptor(private val store: PlanningGraphStore, private val logScope: LogScope) :
    NavigationInterceptor {

    override fun goTo(screen: Screen, navigationContext: NavigationContext): InterceptedResult {
        if (screen is PlanOverviewScreen) {
            store.getOrCreate(screen.travelId)
            logScope.travelId = screen.travelId
        }
        return NavigationInterceptor.Skipped
    }

    override fun pop(result: PopResult?, navigationContext: NavigationContext): InterceptedResult {
        // The context is the stack before the pop, so the top of it is the screen being left.
        val leaving = navigationContext.peek()
        if (leaving is PlanOverviewScreen) {
            store.release(leaving.travelId)
            logScope.travelId = null
        }
        return NavigationInterceptor.Skipped
    }
}
