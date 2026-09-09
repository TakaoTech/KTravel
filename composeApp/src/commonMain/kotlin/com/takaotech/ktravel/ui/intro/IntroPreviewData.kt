package com.takaotech.ktravel.ui.intro

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.domain.staticflows.IntroMedia
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.domain.staticflows.PrivacyDetail
import com.takaotech.ktravel.presentation.intro.IntroUiState
import kotlinx.collections.immutable.persistentListOf

/**
 * The four states the introduction can be drawn in: still loading, on a reading card, on the privacy
 * page, and on the step that asks the question.
 */
internal class IntroContentPreviewParams : PreviewParameterProvider<IntroUiState> {

    override val values = sequenceOf(
        IntroUiState(steps = persistentListOf()) {},
        IntroUiState(steps = persistentListOf(previewReadingStep)) {},
        IntroUiState(steps = persistentListOf(previewPrivacyStep)) {},
        IntroUiState(steps = persistentListOf(previewReadingStep, previewDecisionStep)) {},
    )
}

private val previewReadingStep = IntroStep.Card(
    id = "diagnostics",
    title = "A log you can read",
    body = "While it runs, the app writes a **local log**: what it did, and what failed.\n\n" +
        "- kept for three days\n" +
        "- readable under *Settings → Diagnostics*",
    media = IntroMedia.Static(name = "description"),
)

private val previewPrivacyStep = IntroStep.Privacy(
    id = "privacy",
    title = "Where your trips live",
    body = "The short version, in three points. Tap one to open the privacy policy exactly where " +
        "it is explained.",
    details = listOf(
        PrivacyDetail(
            id = "provided",
            title = "Your trips stay on this device",
            body = "No account, no server of ours.",
            policyRef = "collection.provided",
        ),
        PrivacyDetail(
            id = "automatic",
            title = "Diagnostics are sent only if you say so",
            body = "And you can change your mind at any time.",
            policyRef = "collection.automatic",
        ),
    ),
)

private val previewDecisionStep = IntroStep.Decision(
    id = "decision",
    title = "Send diagnostics?",
    body = "Crashes and diagnostic logs only. **Your trips are never sent.**",
    media = IntroMedia.Static(name = "info"),
    policyRef = "collection.automatic",
)
