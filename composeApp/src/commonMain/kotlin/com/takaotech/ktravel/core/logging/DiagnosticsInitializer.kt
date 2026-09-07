package com.takaotech.ktravel.core.logging

import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.core.telemetry.TelemetrySink
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock

/**
 * Everything about diagnostics that has to happen once, when the application starts.
 *
 * Two jobs, both of which need a scope that outlives a screen:
 *
 * - draining the log queue onto disk, which is what makes the log survive the process;
 * - keeping the telemetry backend in step with the user's answer, including the day it expires.
 *
 * @param logFileSink The queue that writes the log files.
 * @param telemetry The backend the consent is applied to.
 * @param appSettingsRepository Where the answer and the retention are kept.
 * @param clock Used to tell whether the stored answer has expired.
 */
@OptIn(ExperimentalAtomicApi::class)
@SingleIn(AppScope::class)
@Inject
class DiagnosticsInitializer(
    private val logFileSink: LogFileSink,
    private val telemetry: TelemetrySink,
    private val appSettingsRepository: AppSettingsRepository,
    private val clock: Clock = Clock.System,
) {

    /**
     * The scope both jobs live in: the application's, not a screen's.
     *
     * Owned here rather than passed in because it has to outlive every composition — the log has to
     * keep being written while the user is on any screen, and while they are on none.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var started = AtomicBoolean(false)

    /** Starts both jobs, once. Called from the application's composition root. */
    fun start() {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) return

        scope.launch(Dispatchers.IO) { logFileSink.run() }

        scope.launch {
            appSettingsRepository.settings
                .map { it.effectiveConsent(clock.now()) }
                .distinctUntilChanged()
                .collect { consent ->
                    telemetry.applyConsent(consent)

                    if (consent == TelemetryConsent.Granted) {
                        telemetry.identify(appSettingsRepository.installationId())
                    }
                }
        }
    }
}
