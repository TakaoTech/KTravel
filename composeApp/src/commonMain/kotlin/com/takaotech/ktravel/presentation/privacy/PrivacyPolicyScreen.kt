package com.takaotech.ktravel.presentation.privacy

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicySection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

/**
 * The privacy policy in full, as a document to read.
 *
 * Reached from the settings, and from the window the introduction opens over one of its points —
 * which is what [anchor] is for.
 *
 * @property anchor The path of the section to open the document on, e.g. `collection.automatic`; null
 *   opens it at the top.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class PrivacyPolicyScreen(val anchor: String? = null) : Screen

/**
 * What the document draws.
 *
 * @property nodes Every section of the policy, parents before their children, flattened into the
 *   order they are read in; empty while the packaged file is still being read.
 * @property anchor The path to scroll to once, null when the document opens at the top.
 * @property eventSink Where what the user does goes.
 */
data class PrivacyPolicyUiState(
    val nodes: ImmutableList<PrivacyPolicyNode>,
    val anchor: String? = null,
    val eventSink: (PrivacyPolicyEvent) -> Unit,
) : CircuitUiState

/**
 * One section, and how deep in the document it sits.
 *
 * The tree is flattened here rather than in the UI because the document is a lazy list: an item has
 * to be addressable by its path, and its indentation has to be known without walking its parents.
 *
 * @property path The section's own path, e.g. `collection.automatic`. Unique in the document.
 * @property depth Zero for a top level section, one for what is nested under it, and so on.
 * @property section What to draw.
 */
data class PrivacyPolicyNode(val path: String, val depth: Int, val section: PrivacyPolicySection)

/** What the document can report. */
sealed interface PrivacyPolicyEvent : CircuitUiEvent {

    /** The user is done reading. */
    data object Back : PrivacyPolicyEvent

    /** The document has scrolled to the anchor it was opened on, which happens once. */
    data object AnchorConsumed : PrivacyPolicyEvent
}
