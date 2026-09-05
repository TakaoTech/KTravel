package com.takaotech.ktravel.presentation.plan.day

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.test.CircuitReceiveTurbine
import com.slack.circuit.test.presenterTestOf
import com.takaotech.ktravel.core.data.mime.MimeType
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.presentation.plan.AttachmentUi
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.github.vinceglb.filekit.PlatformFile
import io.kotest.assertions.nondeterministic.continually
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.io.files.Path
import kotlin.time.Duration.Companion.seconds

private const val DAY_ID = "DAY_ID"
private const val STEP_ID = "STEP_ID"

/**
 * The notes block is the same on every detail screen, so this is where its rules are pinned:
 * a note is saved a moment after the typing stops, only when it parses, and a reference to a file
 * that is not in the inventory is reported rather than saved silently.
 */
class StepNotesEditingTest : BehaviorSpec() {

    private fun repository() = mock<TravelPlanRepository>(MockMode.autoUnit)

    private suspend fun editing(
        repository: TravelPlanRepository,
        savedNote: String = "",
        attachments: PersistentList<AttachmentUi> = persistentListOf(),
        block: suspend CircuitReceiveTurbine<StepNotesHolder>.() -> Unit,
    ) = presenterTestOf(
        {
            rememberStepNotesEditing(
                repository = repository,
                resolveFile = { PlatformFile(Path(it)) },
                dayId = DAY_ID,
                stepId = STEP_ID,
                savedNote = savedNote,
                attachments = attachments,
            ).let { StepNotesHolder(it.state, it::invoke) }
        },
        block = block,
    )

    /** `presenterTestOf` needs a state; the sink travels with it so the test can drive it. */
    private data class StepNotesHolder(val state: StepNotesUiState, val sink: (StepNotesEvent) -> Unit) :
        CircuitUiState

    private val attachment = AttachmentUi(
        id = "a1",
        relativePath = "t1/s1/ticket.pdf",
        originalName = "ticket.pdf",
        mimeType = MimeType("application/pdf"),
    )

    init {
        given("a note being typed") {
            `when`("the Markdown is valid") {
                then("it should be saved once the typing settles") {
                    val repository = repository()

                    editing(repository) {
                        awaitItem().sink(StepNotesEvent.NoteChanged("# Where to stop"))

                        eventually(3.seconds) {
                            verifySuspend { repository.updateStepNote(DAY_ID, STEP_ID, "# Where to stop") }
                        }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("the note matches what is already saved") {
                then("nothing should be written") {
                    val repository = repository()

                    editing(repository, savedNote = "unchanged") {
                        awaitItem().sink(StepNotesEvent.NoteChanged("unchanged"))

                        continually(2.seconds) {
                            verifySuspend(not) { repository.updateStepNote(DAY_ID, STEP_ID, "unchanged") }
                        }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("a note referencing a file that is not in the inventory") {
            `when`("the note is typed") {
                then("the dangling reference should be reported") {
                    val repository = repository()

                    editing(repository, attachments = persistentListOf(attachment)) {
                        awaitItem().sink(
                            StepNotesEvent.NoteChanged("[map](ktravel://attachment/t1/s1/missing.jpg)"),
                        )

                        var reported = awaitItem().state.missingReferences
                        while (reported.isEmpty()) {
                            reported = awaitItem().state.missingReferences
                        }
                        reported shouldContainExactly listOf("t1/s1/missing.jpg")

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("the editor being closed with a pending note") {
            `when`("a ToggleEdit(false) event is sent") {
                then("the note should be flushed without waiting for the debounce") {
                    val repository = repository()

                    editing(repository) {
                        val holder = awaitItem()
                        holder.sink(StepNotesEvent.NoteChanged("# Flushed"))
                        holder.sink(StepNotesEvent.ToggleEdit(false))

                        eventually(1.seconds) {
                            verifySuspend { repository.updateStepNote(DAY_ID, STEP_ID, "# Flushed") }
                        }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("an attachment event") {
            `when`("RemoveAttachment is sent") {
                then("it should reach the repository") {
                    val repository = repository()

                    editing(repository, attachments = persistentListOf(attachment)) {
                        awaitItem().sink(StepNotesEvent.RemoveAttachment("a1"))

                        eventually(2.seconds) {
                            verifySuspend { repository.removeAttachment(DAY_ID, STEP_ID, "a1") }
                        }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("Markdown validation") {
            `when`("the text is ordinary Markdown") {
                then("it should be considered valid") {
                    isValidMarkdown("# Title\n- item") shouldBe true
                }
            }
        }
    }
}
