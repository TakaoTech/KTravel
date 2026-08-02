package com.takaotech.ktravel.di

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class PlanningGraphStore(
    private val factory: PlanningGraph.Factory
) {
    private val graphs = mutableMapOf<String, PlanningGraph>()

    fun getOrCreate(travelId: String): PlanningGraph =
        graphs.getOrPut(travelId) { factory.create(travelId) }

    fun release(travelId: String) {
        graphs.remove(travelId)
    }
}
