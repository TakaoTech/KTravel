package com.takaotech.ktravel

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PanelHorizontalDivided(
    modifier: Modifier = Modifier,
    scaffoldNavigator: ThreePaneScaffoldNavigator<Any> = rememberListDetailPaneScaffoldNavigator(),

    mainPane: @Composable ThreePaneScaffoldPaneScope.() -> Unit,
    supportPane: @Composable ThreePaneScaffoldPaneScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val density = LocalDensity.current

    var offsetX by remember { mutableStateOf(0f) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }


    SupportingPaneScaffold(
        modifier = modifier,
        directive = scaffoldNavigator.scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        mainPane = mainPane,
        supportingPane = supportPane,
        paneExpansionState = rememberPaneExpansionState(keyProvider = scaffoldNavigator.scaffoldValue),
        paneExpansionDragHandle = { state ->
            val interactionSource = remember { MutableInteractionSource() }
            VerticalDragHandle(
                modifier =
                    Modifier
                        .zIndex(10f)
                        .paneExpansionDraggable(
                            state,
                            LocalMinimumInteractiveComponentSize.current,
                            interactionSource
                        ),
                interactionSource = interactionSource
            )
        }
    )

//    BoxWithConstraints(
//        modifier = modifier
//            .onGloballyPositioned { layoutCoordinates ->
//                componentSize = layoutCoordinates.size
//                if (offsetX == 0f) {
//                    offsetX = componentSize.width / 2f
//                }
//            }
//    ) {
//
//        Row(modifier = Modifier.fillMaxSize()) {
//            val leftWidth = with(density) { offsetX.toDp() }
//            val rightWidth = with(density) { (componentSize.width - offsetX).toDp() }
//
//            // LEFT BOX
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(leftWidth),
//                contentAlignment = Alignment.Center,
//                content = leftBox
//            )
//
//            // DRAG HANDLE
//            VerticalDragHandle(
//                modifier = Modifier
//                    .align(Alignment.CenterVertically)
//                    .draggable(
//                        orientation = Orientation.Horizontal,
//                        state = rememberDraggableState { delta ->
//                            offsetX = (offsetX + delta).coerceIn(
//                                with(density) { 48.dp.toPx() },
//                                componentSize.width.toFloat() - with(density) { 48.dp.toPx() },
//                            )
//                        },
//                    )
////                        .systemGestureExclusion()
//            )
//
//
//            // RIGHT BOX
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(rightWidth),
//                content = rightBox
//            )
//        }
//    }
}