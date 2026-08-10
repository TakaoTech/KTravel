package com.takaotech.ktravel.presentation.intro

import com.takaotech.ktravel.di.PlanningGraph
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.archive.ImportConflictStrategy
import com.takaotech.ktravel.domain.archive.StagedPlanPayload
import com.takaotech.ktravel.domain.archive.StagedTravelArchive
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.archive.TravelArchiveException
import com.takaotech.ktravel.domain.archive.TravelArchiveImporter
import com.takaotech.ktravel.domain.model.TravelPlanSummary
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.github.vinceglb.filekit.PlatformFile
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds

/**
 * The import can now ask up to three questions in a row — conflict, then whether to take the API
 * key, then the password — and nothing must be written until the last one is answered.
 */
class TravelSelectionImportSecretsTest : BehaviorSpec() {
    init {
        coroutineTestScope = true

        val summary = TravelPlanSummary(
            id = "id-1",
            name = "Tokyo",
            periodStart = LocalDate(2026, 4, 1),
            periodEnd = LocalDate(2026, 4, 10),
        )

        val planningGraphStore = PlanningGraphStore(
            PlanningGraph.Factory { error("PlanningGraph must not be created in these tests") },
        )

        fun staged(hasSecrets: Boolean, conflictingName: String? = null) = StagedTravelArchive(
            travelId = "id-1",
            travelName = "Tokyo",
            schemaVersion = 1,
            exportedAtEpochMillis = 0L,
            attachmentCount = 0,
            conflictingTravelName = conflictingName,
            hasSecrets = hasSecrets,
            archive = PlatformFile("archive.ktravel"),
            stagingDir = PlatformFile("staging"),
            payload = object : StagedPlanPayload {},
        )

        given("an archive with no secrets and no conflict") {
            val repository: TravelManagerRepository = mock()
            everySuspend { repository.getAllTravelPlans() } returns emptyList()
            val importer: TravelArchiveImporter = mock()
            everySuspend { importer.stage(any()) } returns Result.success(staged(hasSecrets = false))
            everySuspend {
                importer.import(any(), any(), any(), any())
            } returns Result.success(summary)

            val viewModel = TravelSelectionViewModel(repository, importer, planningGraphStore)

            `when`("it is staged") {
                viewModel.stageImport(PlatformFile("in.ktravel"))

                then("it imports straight away, without asking anything") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.import
                            .shouldBeInstanceOf<ImportUiState.Completed>()
                    }
                }
            }
        }

        given("an archive carrying secrets, with no conflict") {
            val repository: TravelManagerRepository = mock()
            everySuspend { repository.getAllTravelPlans() } returns emptyList()
            val importer: TravelArchiveImporter = mock()
            everySuspend { importer.stage(any()) } returns Result.success(staged(hasSecrets = true))
            everySuspend {
                importer.import(any(), any(), any(), any())
            } returns Result.success(summary)

            val viewModel = TravelSelectionViewModel(repository, importer, planningGraphStore)

            `when`("it is staged") {
                viewModel.stageImport(PlatformFile("in.ktravel"))

                then("the user is asked whether to take the key") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.import shouldBe ImportUiState.AwaitingSecretsChoice
                    }
                }
            }

            `when`("the user declines the key") {
                viewModel.onSecretsChoice(includeSecrets = false)

                then("the import completes without ever asking for a password") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.import
                            .shouldBeInstanceOf<ImportUiState.Completed>()
                    }
                }
            }
        }

        given("an archive carrying secrets whose password is wrong the first time") {
            val repository: TravelManagerRepository = mock()
            everySuspend { repository.getAllTravelPlans() } returns emptyList()
            val importer: TravelArchiveImporter = mock()
            everySuspend { importer.stage(any()) } returns Result.success(staged(hasSecrets = true))
            everySuspend {
                importer.import(any(), any(), any(), "wrong")
            } returns Result.failure(TravelArchiveException(TravelArchiveError.WrongPassword))
            everySuspend {
                importer.import(any(), any(), any(), "right")
            } returns Result.success(summary)

            val viewModel = TravelSelectionViewModel(repository, importer, planningGraphStore)
            viewModel.stageImport(PlatformFile("in.ktravel"))

            `when`("the user asks for the key") {
                viewModel.onSecretsChoice(includeSecrets = true)

                then("the password is prompted for, with no failure marked yet") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.import shouldBe
                            ImportUiState.AwaitingSecretsPassword(attemptFailed = false)
                    }
                }
            }

            `when`("a wrong password is submitted") {
                viewModel.submitSecretsPassword("wrong")

                then("the prompt comes back marked as failed instead of aborting the import") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.import shouldBe
                            ImportUiState.AwaitingSecretsPassword(attemptFailed = true)
                    }
                }
            }

            `when`("the right password is submitted afterwards") {
                viewModel.submitSecretsPassword("right")

                then("the import completes, so the retry used the same staged archive") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.import
                            .shouldBeInstanceOf<ImportUiState.Completed>()
                    }
                }
            }
        }

        given("an archive that both conflicts and carries secrets") {
            val repository: TravelManagerRepository = mock()
            everySuspend { repository.getAllTravelPlans() } returns emptyList()
            val importer: TravelArchiveImporter = mock()
            everySuspend {
                importer.stage(any())
            } returns Result.success(staged(hasSecrets = true, conflictingName = "Tokyo"))
            everySuspend {
                importer.import(any(), any(), any(), any())
            } returns Result.success(summary)

            val viewModel = TravelSelectionViewModel(repository, importer, planningGraphStore)

            `when`("it is staged") {
                viewModel.stageImport(PlatformFile("in.ktravel"))

                then("the conflict is asked about first") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.import
                            .shouldBeInstanceOf<ImportUiState.AwaitingConflictChoice>()
                    }
                }
            }

            `when`("the conflict is resolved") {
                viewModel.confirmImport(ImportConflictStrategy.DUPLICATE, "Tokyo (copy)")

                then("the key question comes only after it") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.import shouldBe ImportUiState.AwaitingSecretsChoice
                    }
                }
            }
        }

        given("an archive whose import fails for a reason other than the password") {
            val repository: TravelManagerRepository = mock()
            everySuspend { repository.getAllTravelPlans() } returns emptyList()
            val importer: TravelArchiveImporter = mock()
            everySuspend { importer.stage(any()) } returns Result.success(staged(hasSecrets = true))
            everySuspend {
                importer.import(any(), any(), any(), any())
            } returns Result.failure(
                TravelArchiveException(TravelArchiveError.Io("disk full")),
            )

            val viewModel = TravelSelectionViewModel(repository, importer, planningGraphStore)
            viewModel.stageImport(PlatformFile("in.ktravel"))

            `when`("the user supplies a password") {
                viewModel.onSecretsChoice(includeSecrets = true)
                viewModel.submitSecretsPassword("whatever")

                then("it fails outright, because retrying would not help") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.import.shouldBeInstanceOf<ImportUiState.Failed>()
                    }
                }
            }
        }
    }
}
