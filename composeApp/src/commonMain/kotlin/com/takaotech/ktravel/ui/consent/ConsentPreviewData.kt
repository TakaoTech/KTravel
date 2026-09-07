package com.takaotech.ktravel.ui.consent

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.domain.staticflows.ConsentCard
import com.takaotech.ktravel.domain.staticflows.ConsentMedia
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The three states the notice can be drawn in: still loading, on a reading card, and on the card
 * that asks the question.
 */
internal class ConsentContentPreviewParams : PreviewParameterProvider<ImmutableList<ConsentCard>> {

    override val values = sequenceOf(
        persistentListOf(),
        persistentListOf(previewReadingCard),
        persistentListOf(previewReadingCard, previewDecisionCard),
    )
}

private val previewReadingCard = ConsentCard(
    id = "diagnostics",
    title = "A log you can read",
    message = "While it runs, the app writes a **local log**: what it did, and what failed.\n\n" +
        "- kept for three days\n" +
        "- readable under *Settings → Diagnostics*",
    media = ConsentMedia.Static(name = "description"),
)

private val previewDecisionCard = ConsentCard(
    id = "decision",
    title = "Send diagnostics?",
    message = "Crashes and diagnostic logs only. **Your trips are never sent.**",
    media = ConsentMedia.Static(name = "info"),
    isDecision = true,
)
