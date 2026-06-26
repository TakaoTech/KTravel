package com.takaotech.ktravel.di

import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.takaotech.ktravel.data.storage.DatabaseProvider
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {
    val planningGraphStore: PlanningGraphStore
    val planningGraphFactory: PlanningGraph.Factory

    /** Istanza Circuit con le factory di presenter/UI generate da Metro (`@CircuitInject`). */
    val circuit: Circuit

    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideDatabaseProvider(): DatabaseProvider = DatabaseProvider()

        @Provides
        @SingleIn(AppScope::class)
        fun provideCircuit(
            presenterFactories: Set<Presenter.Factory>,
            uiFactories: Set<Ui.Factory>
        ): Circuit = Circuit.Builder()
            .addPresenterFactories(presenterFactories)
            .addUiFactories(uiFactories)
            .build()
    }
}
