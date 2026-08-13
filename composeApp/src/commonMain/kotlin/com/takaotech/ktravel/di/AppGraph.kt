package com.takaotech.ktravel.di

import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.takaotech.ktravel.data.archive.zip.ZipArchiveFactory
import com.takaotech.ktravel.data.archive.zip.createZipArchiveFactory
import com.takaotech.ktravel.data.navigator.EmbeddedNavigatorHost
import com.takaotech.ktravel.data.navigator.NavigatorEndpoint
import com.takaotech.ktravel.data.storage.DatabaseProvider
import com.takaotech.navigator.client.NavigatorBaseUrl
import com.takaotech.navigator.client.NavigatorClient
import com.takaotech.navigator.client.NavigatorClientConfig
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

    /** The embedded gunzo-navigator, so a host can stop it when the application goes away. */
    val embeddedNavigatorHost: EmbeddedNavigatorHost

    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideDatabaseProvider(): DatabaseProvider = DatabaseProvider()

        @Provides
        @SingleIn(AppScope::class)
        fun provideZipArchiveFactory(): ZipArchiveFactory = createZipArchiveFactory()

        /**
         * Which navigator the app talks to.
         *
         * Embedded for now. When the remote deployment exists this reads the choice out of the
         * settings instead, and nothing above it changes: the endpoint is resolved per request, so
         * switching does not require rebuilding the client or restarting the app.
         *
         * Deliberately unscoped. Metro turns any binding into a `() -> T` provider, and
         * [com.takaotech.ktravel.data.navigator.NavigatorBaseUrlResolver] takes it in that form so
         * that it re-reads the choice on every request. Scoping this would freeze the answer at the
         * first one and quietly make the setting take effect only after a restart.
         */
        @Provides
        fun provideNavigatorEndpoint(): NavigatorEndpoint = NavigatorEndpoint.Embedded

        /**
         * The one client the app routes through.
         *
         * Application scoped because it owns a connection pool and threads: one per request would
         * cost more to build than the request it is built for.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideNavigatorClient(baseUrl: NavigatorBaseUrl): NavigatorClient =
            NavigatorClient(NavigatorClientConfig(baseUrl = baseUrl))

        @Provides
        @SingleIn(AppScope::class)
        fun provideCircuit(presenterFactories: Set<Presenter.Factory>, uiFactories: Set<Ui.Factory>): Circuit =
            Circuit.Builder()
                .addPresenterFactories(presenterFactories)
                .addUiFactories(uiFactories)
                .build()
    }
}
