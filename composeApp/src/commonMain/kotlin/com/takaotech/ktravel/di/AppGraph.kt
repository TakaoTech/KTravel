package com.takaotech.ktravel.di

import co.touchlab.kermit.Logger
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.takaotech.gunzou.client.NavigatorClient
import com.takaotech.gunzou.client.NavigatorClientConfig
import com.takaotech.ktravel.core.createAppLogger
import com.takaotech.ktravel.data.archive.zip.ZipArchiveFactory
import com.takaotech.ktravel.data.archive.zip.createZipArchiveFactory
import com.takaotech.ktravel.data.navigator.EmbeddedNavigatorHost
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

    /** The embedded gunzo-navigator, so a host can stop it when the application goes away. */
    val embeddedNavigatorHost: EmbeddedNavigatorHost

    companion object {
        /**
         * The one logger of the process.
         *
         * Bound here rather than reached for through the Kermit singleton so that a component which
         * logs has to be given a logger, and so that what it writes through is the same instance the
         * HTTP clients and the embedded navigator write through. See
         * [com.takaotech.ktravel.core.createAppLogger] for what it is configured with.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideLogger(): Logger = createAppLogger()

        /**
         * The embedded navigator, built here rather than by its own `@Inject` constructor so the
         * application's logger reaches it: the server writes through whatever it is given, and what
         * it is given has to be the same logger everything else in the process writes through.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideEmbeddedNavigatorHost(logger: Logger): EmbeddedNavigatorHost = EmbeddedNavigatorHost(logger)

        @Provides
        @SingleIn(AppScope::class)
        fun provideDatabaseProvider(): DatabaseProvider = DatabaseProvider()

        @Provides
        @SingleIn(AppScope::class)
        fun provideZipArchiveFactory(): ZipArchiveFactory = createZipArchiveFactory()

        /**
         * The one client the app routes through, for every navigator it talks to.
         *
         * Application scoped because it owns a connection pool and threads: one per request would
         * cost more to build than the request it is built for, and one per navigator would double
         * that for a screen that has to ask both which profiles they serve.
         *
         * Which navigator a given call goes to is not configured here — it is passed per call, by
         * [com.takaotech.ktravel.data.navigator.NavigatorTargetResolver], because the answer depends
         * on the plan and on what the traveller picked a moment ago. The base URL below is only the
         * fallback for a call that names none, and the embedded server is the one that always exists.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideNavigatorClient(embeddedHost: EmbeddedNavigatorHost, logger: Logger): NavigatorClient =
            NavigatorClient(
                NavigatorClientConfig(
                    baseUrl = { embeddedHost.baseUrl() },
                    logger = logger,
                ),
            )

        @Provides
        @SingleIn(AppScope::class)
        fun provideCircuit(presenterFactories: Set<Presenter.Factory>, uiFactories: Set<Ui.Factory>): Circuit =
            Circuit.Builder()
                .addPresenterFactories(presenterFactories)
                .addUiFactories(uiFactories)
                .build()
    }
}
