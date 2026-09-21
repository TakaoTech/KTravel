package com.takaotech.ktravel.ui.privacy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyNode
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.format_list_numbered_24dp
import ktravel.composeapp.generated.resources.keyboard_arrow_down
import ktravel.composeapp.generated.resources.privacy_policy_cd_collapse_index
import ktravel.composeapp.generated.resources.privacy_policy_cd_expand_index
import ktravel.composeapp.generated.resources.privacy_policy_index
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** How far an entry is pushed in for every level it is nested under. */
private val ENTRY_INDENT = 16.dp

/** Where the text of a top level entry starts, and where the whole bar is padded from. */
private val HORIZONTAL_PADDING = 16.dp

/** An entry is a target for a thumb before it is a line of text. */
private val ENTRY_MIN_HEIGHT = 48.dp

/**
 * How tall the open index may grow before it scrolls on its own.
 *
 * Bounded rather than free: the policy has more sections than fit on a phone, and an index that
 * pushes the document off the screen stops being a way back into it.
 */
private val OPEN_INDEX_MAX_HEIGHT = 280.dp

/**
 * The index of the document, pinned above it: shut it says where you are, open it says where you can
 * go.
 *
 * It is built from the sections themselves — one entry per node, indented by how deep the node sits
 * — so a section added to the policy appears here without anybody maintaining a second list.
 *
 * @param nodes The document, flattened, in the order it is read.
 * @param currentPath The path of the section on screen, which is the entry drawn as the current one.
 * @param expanded Whether the entries are shown.
 * @param onToggle Called when the bar is touched.
 * @param onEntryPicked Called with the position in [nodes] of the section to jump to.
 * @param modifier The modifier applied to the index.
 */
@Composable
internal fun PrivacyPolicyIndex(
    nodes: ImmutableList<PrivacyPolicyNode>,
    currentPath: String?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onEntryPicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().testTag(PrivacyPolicyTestTags.INDEX),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            IndexBar(
                currentTitle = nodes.firstOrNull { it.path == currentPath }?.section?.title,
                expanded = expanded,
                onToggle = onToggle,
            )

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .heightIn(max = OPEN_INDEX_MAX_HEIGHT)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                    ) {
                        nodes.forEachIndexed { index, node ->
                            IndexEntry(
                                node = node,
                                isCurrent = node.path == currentPath,
                                onClick = { onEntryPicked(index) },
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
        }
    }
}

/** The bar itself: what it is, where the reader is, and which way it opens. */
@Composable
private fun IndexBar(currentTitle: String?, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle, role = Role.Button)
            .testTag(PrivacyPolicyTestTags.INDEX_TOGGLE)
            .heightIn(min = ENTRY_MIN_HEIGHT)
            .padding(horizontal = HORIZONTAL_PADDING, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.format_list_numbered_24dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.privacy_policy_index),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Shut, the bar is still worth reading: it is the only thing on screen that says which
            // section the wall of text below belongs to.
            if (!expanded && currentTitle != null) {
                Text(
                    text = currentTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        val rotation by animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            label = "privacyPolicyIndexChevron",
        )

        Icon(
            painter = painterResource(Res.drawable.keyboard_arrow_down),
            contentDescription = stringResource(
                if (expanded) {
                    Res.string.privacy_policy_cd_collapse_index
                } else {
                    Res.string.privacy_policy_cd_expand_index
                },
            ),
            modifier = Modifier.graphicsLayer { rotationZ = rotation },
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** One line of the index, indented by its depth and marked when it is the one being read. */
@Composable
private fun IndexEntry(node: PrivacyPolicyNode, isCurrent: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button)
            .testTag(PrivacyPolicyTestTags.indexEntry(node.path))
            .heightIn(min = ENTRY_MIN_HEIGHT)
            .padding(
                start = HORIZONTAL_PADDING + ENTRY_INDENT * node.depth,
                end = HORIZONTAL_PADDING,
                top = 4.dp,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = node.section.title,
            style = if (node.depth == 0) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = when {
                isCurrent -> MaterialTheme.colorScheme.primary
                node.depth == 0 -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

//region Previews

@PreviewFontScale
@Composable
private fun PrivacyPolicyIndexPreview(@PreviewParameter(PrivacyPolicyIndexPreviewParams::class) expanded: Boolean) =
    KTravelTheme {
        PrivacyPolicyIndex(
            nodes = previewPolicyNodes,
            currentPath = "collection.automatic",
            expanded = expanded,
            onToggle = {},
            onEntryPicked = {},
        )
    }
//endregion Previews
