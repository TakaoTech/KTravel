package com.takaotech.ktravel.di

import co.touchlab.kermit.Logger
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.backstack.ViewModelNavStackRecordLocalProvider
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.serialization.CircuitSerializerRegistration
import com.slack.circuit.serialization.SerializableCircuitSaver
import com.slack.circuitx.gesturenavigation.GestureNavigationDecorationFactory
import com.takaotech.gunzou.client.NavigatorClient
import com.takaotech.gunzou.client.NavigatorClientConfig
import com.takaotech.ktravel.core.createAppLogger
import com.takaotech.ktravel.core.logging.AppLogger
import com.takaotech.ktravel.core.logging.DiagnosticsInitializer
import com.takaotech.ktravel.core.logging.InMemoryLogStore
import com.takaotech.ktravel.core.logging.KermitAppLogger
import com.takaotech.ktravel.core.logging.LogBufferWriter
import com.takaotech.ktravel.core.logging.LogFileSink
import com.takaotech.ktravel.core.logging.LogFileStore
import com.takaotech.ktravel.core.logging.LogRepository
import com.takaotech.ktravel.core.logging.LogScope
import com.takaotech.ktravel.core.logging.LogStore
import com.takaotech.ktravel.core.logging.installPlatformLogBridge
import com.takaotech.ktravel.core.telemetry.KTravelTelemetry
import com.takaotech.ktravel.core.telemetry.TelemetryLogWriter
import com.takaotech.ktravel.core.telemetry.TelemetrySink
import com.takaotech.ktravel.data.archive.zip.ZipArchiveFactory
import com.takaotech.ktravel.data.archive.zip.createZipArchiveFactory
import com.takaotech.ktravel.data.navigator.EmbeddedNavigatorHost
import com.takaotech.ktravel.data.staticflows.IntroFlowDataSource
import com.takaotech.ktravel.data.staticflows.PrivacyPolicyDataSource
import com.takaotech.ktravel.data.staticflows.StaticContentRepository
import com.takaotech.ktravel.data.storage.DatabaseProvider
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.toKotlinxIoPath
import kotlin.time.Clock

@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {
    val planningGraphStore: PlanningGraphStore

    val planningGraphFactory: PlanningGraph.Factory

    /** Istanza Circuit con le factory di presenter/UI generate da Metro (`@CircuitInject`). */
    val circuit: Circuit

    /** The application logger, also used to record what the back stack does. */
    val logger: Logger

    /** What the application's own code logs through. */
    val appLogger: AppLogger

    /** The lines of the running session, which the diagnostics screen watches. */
    val logStore: LogStore

    /** The trip a log line belongs to, stamped by navigation. */
    val logScope: LogScope

    /** Reads the kept log, from the files and from memory alike. */
    val logRepository: LogRepository

    /** The remote diagnostics backend, no-op in a build without a Kotzilla project file. */
    val telemetrySink: TelemetrySink

    /** Starts the log file writer and keeps telemetry in step with the user's consent. */
    val diagnosticsInitializer: DiagnosticsInitializer

    /** The introduction and the privacy policy, read by navigation to decide what is due. */
    val staticContentRepository: StaticContentRepository

    /** The installation's preferences, read by navigation for the same reason. */
    val appSettingsRepository: AppSettingsRepository

    /** The embedded gunzo-navigator, so a host can stop it when the application goes away. */
    val embeddedNavigatorHost: EmbeddedNavigatorHost

    companion object {

        /** Where the log files live, under the application's data directory. */
        private const val LOGS_DIRECTORY = "logs"

        /**
         * The one logger of the process.
         *
         * Bound here rather than reached for through the Kermit singleton so that a component which
         * logs has to be given a logger, and so that what it writes through is the same instance
         * the HTTP clients and the embedded navigator write through. See
         * [com.takaotech.ktravel.core.createAppLogger] for what it is configured with.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideLogger(
            logStore: LogStore,
            logFileSink: LogFileSink,
            logScope: LogScope,
            telemetrySink: TelemetrySink,
            appSettingsRepository: AppSettingsRepository,
        ): Logger = createAppLogger(
            extraWriters = listOf(
                LogBufferWriter(store = logStore, sink = logFileSink, scope = logScope),
                TelemetryLogWriter(sink = telemetrySink) {
                    appSettingsRepository.settings.value.effectiveConsent(Clock.System.now())
                },
            ),
        ).also {
            // Every library that logs through SLF4J writes here too, which is what puts a Ktor or a
            // Couchbase line on the diagnostics screen next to the application's own.
            installPlatformLogBridge(it)
        }

        /** What the application's own code logs through, so no call site names Kermit. */
        @Provides
        @SingleIn(AppScope::class)
        fun provideAppLogger(logger: Logger): AppLogger = KermitAppLogger(logger)

        @Provides
        @SingleIn(AppScope::class)
        fun provideLogStore(): LogStore = InMemoryLogStore()

        @Provides
        @SingleIn(AppScope::class)
        fun provideLogScope(): LogScope = LogScope()

        /**
         * The log files, under the application's own data directory.
         *
         * Resolved lazily, when something first logs: `FileKit.filesDir` needs the platform to have
         * been initialised, which on the desktop happens in `main`.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideLogFileStore(): LogFileStore = LogFileStore((FileKit.filesDir / LOGS_DIRECTORY).toKotlinxIoPath())

        /**
         * The queue that writes the log to disk.
         *
         * The retention is read through the settings flow rather than captured, so changing it in the
         * diagnostics screen deletes the files that just fell out of the window, without a restart.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideLogFileSink(logFileStore: LogFileStore, appSettingsRepository: AppSettingsRepository): LogFileSink =
            LogFileSink(
                store = logFileStore,
                retentionDays = { appSettingsRepository.settings.value.logRetentionDays },
            )

        @Provides
        @SingleIn(AppScope::class)
        fun provideLogRepository(logFileStore: LogFileStore, logStore: LogStore): LogRepository =
            LogRepository(store = logFileStore, logStore = logStore)

        /** The telemetry backend of the process, the same instance the entry points started. */
        @Provides
        @SingleIn(AppScope::class)
        fun provideTelemetrySink(): TelemetrySink = KTravelTelemetry.sink

        /**
         * The embedded navigator, built here rather than by its own `@Inject` constructor so the
         * application's logger reaches it: the server writes through whatever it is given, and what
         * it is given has to be the same logger everything else in the process writes through.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideEmbeddedNavigatorHost(logger: Logger): EmbeddedNavigatorHost = EmbeddedNavigatorHost(logger)

        /**
         * Reads the packaged introduction.
         *
         * Provided rather than `@Inject`ed because its one parameter is the function that reads a
         * resource, which a test replaces and a graph has no business binding.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideIntroFlowDataSource(): IntroFlowDataSource = IntroFlowDataSource()

        /** Reads the packaged privacy policy, provided for the same reason as the introduction. */
        @Provides
        @SingleIn(AppScope::class)
        fun providePrivacyPolicyDataSource(): PrivacyPolicyDataSource = PrivacyPolicyDataSource()

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
         * [com.takaotech.ktravel.data.navigator.NavigatorTargetResolver], because the answer
         * depends on the plan and on what the traveller picked a moment ago. The base URL below is
         * only the fallback for a call that names none, and the embedded server is the one that
         * always exists.
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

        /**
         * The Circuit instance the whole application composes through.
         *
         * The three registrations beyond the factories are what let Circuit own the back stack:
         * a saver that speaks kotlinx-serialization rather than the platform registry, a
         * `ViewModelStoreOwner` per back stack record, and the platform's own back gesture.
         *
         * @param presenterFactories Every `@CircuitInject` presenter, contributed by Metro.
         * @param uiFactories Every `@CircuitInject` UI, contributed by Metro.
         * @param serializerRegistrations One per `@CircuitSerializable` screen, generated by Metro.
         * @param logger Records a record that could not be restored instead of losing it silently.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideCircuit(
            presenterFactories: Set<Presenter.Factory>,
            uiFactories: Set<Ui.Factory>,
            serializerRegistrations: Set<CircuitSerializerRegistration>,
            logger: Logger,
        ): Circuit = Circuit.Builder()
            .addPresenterFactories(presenterFactories)
            .addUiFactories(uiFactories)
            // The screens are @Serializable, not Parcelable: the default registry-backed saver
            // would refuse them on Android, so persistence goes through kotlinx-serialization.
            .setCircuitSaver(
                SerializableCircuitSaver(serializerRegistrations) { error ->
                    logger.e(error) { "A back stack record could not be restored and was dropped" }
                },
            )
            // Scopes a ViewModelStoreOwner to each back stack record, so the existing Metro view
            // models keep working inside Circuit UIs.
            .addNavStackRecordLocalProvider(ViewModelNavStackRecordLocalProvider)
            // Predictive back on Android 14+, interactive pop on iOS, the default elsewhere.
            .setAnimatedNavDecoratorFactory(GestureNavigationDecorationFactory())
            .build()
    }
}
