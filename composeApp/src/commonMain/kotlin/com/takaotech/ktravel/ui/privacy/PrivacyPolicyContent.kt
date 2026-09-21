package com.takaotech.ktravel.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyEvent
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyNode
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyUiState
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.privacy_policy_cd_loading
import ktravel.composeapp.generated.resources.privacy_policy_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** How far a nested section is pushed in, one level after another. */
private val NESTING_INDENT = 16.dp

/**
 * The privacy policy in full, as a list of sections that can be opened on any one of them.
 *
 * A lazy list rather than one long scrolling column: the anchor the introduction hands over is a
 * path, and a list is what can be told to scroll to the item that carries it. Above it sits the
 * index, which never scrolls away — a document this long is unreadable without a way of jumping
 * around it, and of knowing where in it you currently are.
 *
 * @param state What to draw, and where what the user does goes.
 * @param modifier The modifier applied to the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrivacyPolicyContent(state: PrivacyPolicyUiState, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isIndexExpanded by rememberSaveable { mutableStateOf(false) }

    // Once, and only once: the anchor says where the document opens, not where it has to stay.
    LaunchedEffect(state.anchor, state.nodes) {
        val anchor = state.anchor ?: return@LaunchedEffect
        val index = state.nodes.indexOfFirst { it.path == anchor }

        if (index >= 0) {
            listState.scrollToItem(index)
            state.eventSink(PrivacyPolicyEvent.AnchorConsumed)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.privacy_policy_title)) },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(PrivacyPolicyEvent.Back) }) {
                        Icon(painterResource(Res.drawable.arrow_back), contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        if (state.nodes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                val loading = stringResource(Res.string.privacy_policy_cd_loading)
                CircularProgressIndicator(
                    modifier = Modifier
                        .testTag(PrivacyPolicyTestTags.LOADING)
                        .semantics { contentDescription = loading },
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                PrivacyPolicyIndex(
                    nodes = state.nodes,
                    currentPath = listState.currentPath(state.nodes),
                    expanded = isIndexExpanded,
                    onToggle = { isIndexExpanded = !isIndexExpanded },
                    // Jumped to rather than animated to: an entry can be twelve sections away, and
                    // scrolling through all of them is neither quick nor legible.
                    onEntryPicked = { index ->
                        isIndexExpanded = false
                        scope.launch { listState.scrollToItem(index) }
                    },
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().testTag(PrivacyPolicyTestTags.LIST),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    items(state.nodes.size, key = { state.nodes[it].path }) { index ->
                        PrivacyPolicySectionItem(state.nodes[index])
                    }
                }
            }
        }
    }
}

/**
 * The path of the section being read, which is the first one still on screen.
 *
 * Derived rather than observed directly, so that scrolling inside one section — which is most of the
 * scrolling in a document like this — does not recompose the index on every frame.
 */
@Composable
private fun LazyListState.currentPath(nodes: ImmutableList<PrivacyPolicyNode>): String? {
    val path by remember(this, nodes) {
        derivedStateOf { nodes.getOrNull(firstVisibleItemIndex)?.path }
    }

    return path
}

/** One section of the document: its heading, indented by how deep it sits, and its text. */
@Composable
private fun PrivacyPolicySectionItem(node: PrivacyPolicyNode, modifier: Modifier = Modifier) {
    val style = if (node.depth == 0) {
        MaterialTheme.typography.headlineSmall
    } else {
        MaterialTheme.typography.titleMedium
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = NESTING_INDENT * node.depth)
            .testTag(PrivacyPolicyTestTags.section(node.path)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = node.section.title, style = style, modifier = Modifier.semantics { heading() })

        Markdown(content = node.section.body, modifier = Modifier.fillMaxWidth())
    }
}

//region Previews

@PreviewScreenSizes
@Composable
private fun PrivacyPolicyContentPreview(
    @PreviewParameter(PrivacyPolicyContentPreviewParams::class) state: PrivacyPolicyUiState,
) = KTravelTheme {
    PrivacyPolicyContent(state = state)
}
//endregion Previews
