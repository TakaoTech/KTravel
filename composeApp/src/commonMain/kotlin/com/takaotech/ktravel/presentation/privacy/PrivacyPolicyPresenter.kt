package com.takaotech.ktravel.presentation.privacy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.data.staticflows.StaticContentRepository
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.staticflows.PRIVACY_PATH_SEPARATOR
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicy
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicySection
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Reads the packaged policy in the device's language and lays it out flat.
 *
 * @param screen Says which section the document opens on, when it was opened on one.
 * @param navigator Where going back leads.
 * @param staticContentRepository Reads the packaged policy.
 */
@CircuitInject(PrivacyPolicyScreen::class, AppScope::class)
@Composable
fun PrivacyPolicyPresenter(
    screen: PrivacyPolicyScreen,
    navigator: Navigator,
    staticContentRepository: StaticContentRepository,
): PrivacyPolicyUiState {
    val language = Locale.current.language

    val policy: PrivacyPolicy? by produceState(initialValue = null, language, staticContentRepository) {
        value = staticContentRepository.privacyPolicy(language)
    }

    // The anchor is a one shot: it says where to open, not where to stay, so scrolling back up must
    // not be undone by a recomposition.
    var anchor: String? by remember(screen.anchor) { mutableStateOf(screen.anchor) }

    val nodes = remember(policy) { policy?.flatten()?.toImmutableList() ?: persistentListOf() }

    return PrivacyPolicyUiState(nodes = nodes, anchor = anchor) { event ->
        when (event) {
            PrivacyPolicyEvent.Back -> navigator.pop()
            PrivacyPolicyEvent.AnchorConsumed -> anchor = null
        }
    }
}

/** The policy as a list, parents before their children, each one carrying its path and its depth. */
private fun PrivacyPolicy.flatten(): List<PrivacyPolicyNode> = sections.flatMap { it.flatten(prefix = "", depth = 0) }

private fun PrivacyPolicySection.flatten(prefix: String, depth: Int): List<PrivacyPolicyNode> {
    val path = if (prefix.isEmpty()) id else "$prefix$PRIVACY_PATH_SEPARATOR$id"

    return listOf(PrivacyPolicyNode(path = path, depth = depth, section = this)) +
        subsections.flatMap { it.flatten(prefix = path, depth = depth + 1) }
}
