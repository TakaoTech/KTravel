package com.takaotech.ktravel.di

import com.takaotech.ktravel.data.storage.DatabaseProvider
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {
    val planningGraphStore: PlanningGraphStore
    val planningGraphFactory: PlanningGraph.Factory

    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideDatabaseProvider(): DatabaseProvider = DatabaseProvider()
    }
}
