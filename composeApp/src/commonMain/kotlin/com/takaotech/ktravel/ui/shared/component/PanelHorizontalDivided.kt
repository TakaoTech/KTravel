package com.takaotech.ktravel.ui.shared.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

/**
 * Two panes side by side, with a handle the user drags to give one of them more room.
 *
 * A thin wrapper over [SupportingPaneScaffold] whose only addition is that drag handle, which the
 * scaffold does not provide by default: without it the split is whatever the window size decides,
 * and a list beside a map is exactly the case where the reader wants to decide instead.
 *
 * @param modifier The modifier applied to the scaffold.
 * @param scaffoldNavigator The navigator deciding which panes are visible at the current size.
 * @param paneExpansionState The state holding where the split currently sits.
 * @param extraPane The third pane, when the layout has one.
 * @param mainPane The pane carrying the primary content.
 * @param supportingPane The pane beside it.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PanelHorizontalDivided(
    modifier: Modifier = Modifier,
    scaffoldNavigator: ThreePaneScaffoldNavigator<Any> = rememberSupportingPaneScaffoldNavigator(),
    paneExpansionState: PaneExpansionState = rememberPaneExpansionState(keyProvider = scaffoldNavigator.scaffoldValue),
    extraPane: (@Composable ThreePaneScaffoldPaneScope.() -> Unit)? = null,
    mainPane: @Composable ThreePaneScaffoldPaneScope.() -> Unit,
    supportingPane: @Composable ThreePaneScaffoldPaneScope.() -> Unit,
) {
    SupportingPaneScaffold(
        modifier = modifier,
        directive = scaffoldNavigator.scaffoldDirective,
        scaffoldState = scaffoldNavigator.scaffoldState,
        mainPane = mainPane,
        supportingPane = supportingPane,
        extraPane = extraPane,
        paneExpansionState = paneExpansionState,
        paneExpansionDragHandle = { state ->
            val interactionSource = remember { MutableInteractionSource() }
            VerticalDragHandle(
                modifier = Modifier
                    .zIndex(10f)
                    .paneExpansionDraggable(
                        state,
                        LocalMinimumInteractiveComponentSize.current,
                        interactionSource,
                    ),
                interactionSource = interactionSource,
            )
        },
    )
}
