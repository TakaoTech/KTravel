package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavEvent
import com.takaotech.ktravel.PanelHorizontalDivided
import com.takaotech.ktravel.presentation.planning.detail.PlacesBacklogScreen
import com.takaotech.ktravel.presentation.planning.detail.PlanningDetailEvent
import com.takaotech.ktravel.presentation.planning.detail.PlanningDetailUiState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class PlanningDetailPageNavigation(val id: String)

/**
 * Pagina del dettaglio giorno: layout adattivo che compone i pannelli itinerario e backlog come
 * `CircuitContent` annidati.
 *
 * Su larghezze medium+ i pannelli sono affiancati (con drag handle); su compatto la directive
 * collassa a un solo pannello e il backlog si apre a schermo intero via `OpenBacklog`/`Close`
 * (intercettati qui come [NavEvent] e tradotti in navigazione dello scaffold). Ogni altro
 * [NavEvent] dei figli risale al presenter padre tramite [PlanningDetailEvent.ChildNav].
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PlanningDetailPage(state: PlanningDetailUiState, modifier: Modifier = Modifier) {
    val coroutine = rememberCoroutineScope()
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = calculatePaneScaffoldDirective(windowAdaptiveInfo)

    val scaffoldNavigator = rememberSupportingPaneScaffoldNavigator(scaffoldDirective = directive)
    val paneExpansionState: PaneExpansionState =
        rememberPaneExpansionState(keyProvider = scaffoldNavigator.scaffoldValue)

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = scaffoldNavigator.canNavigateBack(),
        onBackCompleted = {
            coroutine.launch {
                scaffoldNavigator.navigateBack()
            }
        },
    )

    PanelHorizontalDivided(
        scaffoldNavigator = scaffoldNavigator,
        paneExpansionState = paneExpansionState,
        modifier = modifier,
        mainPane = {
            AnimatedPane {
                CircuitContent(
                    screen = state.stepsPaneScreen,
                    onNavEvent = { event ->
                        if (event is NavEvent.GoTo && event.screen is PlacesBacklogScreen) {
                            coroutine.launch {
                                scaffoldNavigator.navigateTo(SupportingPaneScaffoldRole.Supporting)
                            }
                        } else {
                            state.eventSink(PlanningDetailEvent.ChildNav(event))
                        }
                    },
                )
            }
        },
        supportingPane = {
            AnimatedPane {
                CircuitContent(
                    modifier = Modifier.fillMaxSize(),
                    screen = state.placesBacklogScreen,
                    onNavEvent = { event ->
                        if (event is NavEvent.Pop) {
                            coroutine.launch {
                                scaffoldNavigator.navigateBack()
                            }
                        } else {
                            state.eventSink(PlanningDetailEvent.ChildNav(event))
                        }
                    },
                )
            }
        },
    )
}
