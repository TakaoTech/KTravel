package com.takaotech.ktravel.presentation.diagnostics

import androidx.compose.runtime.Composable
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import com.takaotech.ktravel.core.logging.InMemoryLogStore
import com.takaotech.ktravel.core.logging.LogEntry
import com.takaotech.ktravel.core.logging.LogFileStore
import com.takaotech.ktravel.core.logging.LogLevel
import com.takaotech.ktravel.core.logging.LogRepository
import com.takaotech.ktravel.core.logging.LogStore
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.core.telemetry.TelemetrySink
import com.takaotech.ktravel.domain.model.AppSettingsDomain
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.TimeZone
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private const val TRAVEL_ID = "trip-42"

/**
 * The diagnostics screen.
 *
 * Four things are worth holding onto here, and none of them is about drawing: that a screen opened
 * from inside a trip shows that trip alone, that changing the consent both stores the answer and
 * hands it to the backend at once, that being forgotten is asked for on its own, and that reporting
 * a problem produces a file to attach and a URL to open rather than doing either itself.
 */
class LogsPresenterTest :
    BehaviorSpec({
        given("a log holding lines of two different trips") {
            `when`("the screen is opened from inside one of them") {
                then("only that trip's lines are shown") {
                    val logStore = liveStore(
                        entry("1", travelId = TRAVEL_ID),
                        entry("2", travelId = "other-trip"),
                        entry("3", travelId = null),
                    )
                    val screen = LogsScreen(TRAVEL_ID)

                    presenterTestOf({ presenter(screen, logStore = logStore) }) {
                        val state = awaitLoaded()

                        state.entries.map { it.id } shouldBe listOf("1")
                    }
                }
            }

            `when`("the screen is opened from the app settings") {
                then("every line is shown, whatever trip it belongs to") {
                    val logStore = liveStore(
                        entry("1", travelId = TRAVEL_ID),
                        entry("2", travelId = null),
                    )

                    presenterTestOf({ presenter(LogsScreen(), logStore = logStore) }) {
                        val state = awaitLoaded()

                        state.entries.map { it.id } shouldBe listOf("1", "2")
                    }
                }
            }
        }

        given("a log with a debug line and an error") {
            `when`("only warnings and above are asked for") {
                then("the debug line goes away") {
                    val logStore = liveStore(
                        entry("quiet", level = LogLevel.Debug),
                        entry("loud", level = LogLevel.Error),
                    )

                    presenterTestOf({ presenter(LogsScreen(), logStore = logStore) }) {
                        awaitLoaded().eventSink(LogsEvent.LevelSelected(LogLevel.Warn))

                        eventually(2.seconds) {
                            awaitItem().entries.map { it.id } shouldBe listOf("loud")
                        }
                    }
                }
            }

            `when`("a word only one of them contains is searched for") {
                then("the other one goes away") {
                    val logStore = liveStore(
                        entry("one", message = "route computed"),
                        entry("two", message = "database opened"),
                    )

                    presenterTestOf({ presenter(LogsScreen(), logStore = logStore) }) {
                        awaitLoaded().eventSink(LogsEvent.QueryChanged("database"))

                        eventually(2.seconds) {
                            awaitItem().entries.map { it.id } shouldBe listOf("two")
                        }
                    }
                }
            }
        }

        given("an installation that had consented") {
            `when`("the consent is revoked from the diagnostics screen") {
                then("the refusal is stored and handed to the backend, which is not asked to forget") {
                    val settings = FakeAppSettings(
                        AppSettingsDomain(
                            telemetryConsent = TelemetryConsent.Granted,
                            acknowledgedIntroVersion = 1,
                            acknowledgedPrivacyVersion = 1,
                            consentDecidedAt = NOW,
                        ),
                    )
                    val sink = RecordingSink()

                    presenterTestOf({ presenter(LogsScreen(), settings = settings, sink = sink) }) {
                        awaitLoaded().eventSink(LogsEvent.ConsentChanged(granted = false))

                        eventually(2.seconds) {
                            settings.settings.value.telemetryConsent shouldBe TelemetryConsent.Denied
                            sink.appliedConsent shouldBe TelemetryConsent.Denied
                        }
                        sink.forgotten shouldBe false

                        // The recomposition the write causes is not what this test is about.
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("the deletion of what was already sent is asked for") {
                then("the backend is asked to forget the installation, and the consent is left alone") {
                    val settings = FakeAppSettings(
                        AppSettingsDomain(
                            telemetryConsent = TelemetryConsent.Granted,
                            acknowledgedIntroVersion = 1,
                            acknowledgedPrivacyVersion = 1,
                            consentDecidedAt = NOW,
                        ),
                    )
                    val sink = RecordingSink()

                    presenterTestOf({ presenter(LogsScreen(), settings = settings, sink = sink) }) {
                        awaitLoaded().eventSink(LogsEvent.ForgetMeRequested)

                        eventually(2.seconds) {
                            sink.forgotten shouldBe true
                        }
                        settings.settings.value.telemetryConsent shouldBe TelemetryConsent.Granted

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("a user reporting a problem") {
            `when`("the report is asked for") {
                then("a file to attach and an issue to open are both prepared") {
                    val logStore = liveStore(entry("1", message = "the route could not be computed"))

                    presenterTestOf({ presenter(LogsScreen(), logStore = logStore) }) {
                        awaitLoaded().eventSink(LogsEvent.ReportRequested)

                        eventually(2.seconds) {
                            val state = awaitItem()
                            state.pendingSave?.content?.contains("the route could not be computed") shouldBe true
                            state.pendingIssueUrl?.startsWith("https://github.com/TakaoTech/KTravel") shouldBe true
                        }
                    }
                }
            }
        }
    })

private val NOW = Instant.parse("2026-09-06T09:00:00Z")

/** The first state whose file read has finished; before that the screen is still loading. */
private suspend fun com.slack.circuit.test.CircuitReceiveTurbine<LogsUiState>.awaitLoaded(): LogsUiState {
    var state = awaitItem()
    while (state.isLoading) {
        state = awaitItem()
    }
    return state
}

private fun entry(
    id: String,
    travelId: String? = null,
    level: LogLevel = LogLevel.Info,
    message: String = "line $id",
) = LogEntry(
    id = id,
    timestamp = NOW,
    level = level,
    tag = "test",
    message = message,
    travelId = travelId,
)

private fun liveStore(vararg entries: LogEntry): LogStore = InMemoryLogStore().apply { entries.forEach(::record) }

/**
 * The presenter over real collaborators, except for the settings and the telemetry backend.
 *
 * The log repository is the real one over an empty directory: what the screen shows in these tests
 * comes from the running session, which is also where it comes from while a user is looking at it.
 */
@Composable
private fun presenter(
    screen: LogsScreen,
    logStore: LogStore = InMemoryLogStore(),
    settings: AppSettingsRepository = FakeAppSettings(AppSettingsDomain()),
    sink: TelemetrySink = RecordingSink(),
) = LogsPresenter(
    screen = screen,
    navigator = FakeNavigator(screen),
    logRepository = LogRepository(
        store = LogFileStore(Path(SystemTemporaryDirectory, "ktravel-logs-test-${Random.nextLong().toULong()}")),
        logStore = logStore,
        timeZone = TimeZone.UTC,
    ),
    logStore = logStore,
    appSettingsRepository = settings,
    telemetrySink = sink,
)

/** The preferences, in memory, with the writes a test needs to observe. */
private class FakeAppSettings(initial: AppSettingsDomain) : AppSettingsRepository {

    private val state = MutableStateFlow(initial)

    override val settings: StateFlow<AppSettingsDomain> = state

    override suspend fun updateNavigatorRemote(baseUrl: String) = error("Not written here")

    override suspend fun updateTelemetryConsent(
        consent: TelemetryConsent,
        introVersion: Int,
        privacyVersion: Int,
        decidedAt: Instant,
    ) {
        state.value = state.value.copy(
            telemetryConsent = consent,
            acknowledgedIntroVersion = introVersion,
            acknowledgedPrivacyVersion = privacyVersion,
            consentDecidedAt = decidedAt,
        )
    }

    override suspend fun updateLogRetentionDays(days: Int) {
        state.value = state.value.copy(logRetentionDays = days)
    }

    override suspend fun installationId(): String = "test-installation"
}

/** A backend that sends nothing and remembers what it was told: the consent, and being forgotten. */
private class RecordingSink : TelemetrySink {

    var forgotten = false
        private set

    var appliedConsent: TelemetryConsent? = null
        private set

    override fun start() = Unit

    override fun applyConsent(consent: TelemetryConsent) {
        appliedConsent = consent
    }

    override fun identify(installationId: String) = Unit

    override fun record(level: LogLevel, tag: String, message: String, throwable: Throwable?) = Unit

    override fun forgetMe() {
        forgotten = true
    }
}
