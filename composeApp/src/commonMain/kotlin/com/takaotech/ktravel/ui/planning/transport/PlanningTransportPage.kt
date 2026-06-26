package com.takaotech.ktravel.ui.planning.transport

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.ktravel.domain.routing.RoutingProviderType
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportUiState
import com.takaotech.ktravel.presentation.planning.transport.PlanningTransportViewModel
import com.takaotech.ktravel.ui.planning.transport.settings.GMapsProviderSettings
import com.takaotech.ktravel.ui.planning.transport.settings.HereProviderSettings
import com.takaotech.ktravel.ui.planning.transport.settings.LocalProviderSettings
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Serializable
object PlanningTransportNavigation

@Serializable
data class PlanningTransportPageNavigation(val dayId: String, val startPlaceId: String, val endPlaceId: String)

@Serializable
class PlanningTransportRoutePreviewPageNavigation(val dayId: String, val startPlaceId: String, val endPlaceId: String)

@Composable
fun PlanningTransportPage(
    viewModel: PlanningTransportViewModel,
    modifier: Modifier = Modifier,
    onNavigationBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlanningTransportPage(
        modifier = modifier,
        uiState = uiState,
        onNavigationBackClick = onNavigationBackClick,
        onProviderChange = {
            viewModel.selectProvider(it)
        },
        onProviderSettingsChange = {
            viewModel.updateProviderSettings(it)
        },
        onCalculateClick = {
            viewModel.calculateTransport()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanningTransportPage(
    modifier: Modifier = Modifier,
    uiState: PlanningTransportUiState,
    onNavigationBackClick: () -> Unit,
    onCalculateClick: () -> Unit,
    onProviderChange: (RoutingProviderType) -> Unit,
    onProviderSettingsChange: (RoutingProviderSettings) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Transport") },
                navigationIcon = {
                    IconButton(onClick = onNavigationBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCalculateClick
                ) {
                    Text("Calculate")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val startPlace = uiState.startPlace
            val endPlace = uiState.endPlace

            if (startPlace != null && endPlace != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    PlaceDestination(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        text = uiState.startPlace.name
                    )

                    val composition by rememberLottieComposition {
                        LottieCompositionSpec.JsonString(
                            Res.readBytes("files/paper_airplane.json").decodeToString()
                        )
                    }

                    Image(
                        modifier = Modifier.padding(8.dp),
                        painter = rememberLottiePainter(
                            composition = composition,
                            iterations = Compottie.IterateForever
                        ),
                        contentDescription = "Lottie animation"
                    )

//                    Icon(
//                        modifier = Modifier.padding(8.dp),
//                        painter = painterResource(Res.drawable.arrow_right_alt),
//                        contentDescription = null
//                    )

                    PlaceDestination(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        text = uiState.endPlace.name
                    )
                }
            }



            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    label = {
                        Text(
                            text = "Routing Provider",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    value = stringResource(uiState.selectedProvider.stringName),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    uiState.availableProviders.forEach { providerType ->
                        DropdownMenuItem(
                            text = { Text(stringResource(providerType.stringName)) },
                            onClick = {
                                onProviderChange(providerType)

                                expanded = false
                            }
                        )
                    }
                }
            }

            when (val settings = uiState.providerSettings) {
                is RoutingProviderSettings.Local -> LocalProviderSettings(
                    settings = settings,
                    onSettingsChange = { onProviderSettingsChange(it) }
                )

                is RoutingProviderSettings.Here -> HereProviderSettings(
                    settings = settings,
                    onSettingsChange = { onProviderSettingsChange(it) }
                )

                is RoutingProviderSettings.GMaps -> GMapsProviderSettings(
                    settings = settings,
                    onSettingsChange = { onProviderSettingsChange(it) }
                )
            }
        }
    }
}

@Composable
private fun PlaceDestination(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .basicMarquee(),
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp),
                text = text,
            )
        }
    }
}

@PreviewScreenSizes
@Composable
private fun PlanningTransportPagePreview() {
    PlanningTransportPage(
        uiState = PlanningTransportUiState(
            startPlace = StepUi.Place(
                name = "P.za del Colosseo, 1, 00184 Roma RM",
                lat = 0.0,
                lng = 0.0
            ),
            endPlace = StepUi.Place(name = "Piazza di Trevi, 00187 Roma RM", lat = 0.0, lng = 0.0)
        ),
        onNavigationBackClick = {},
        onCalculateClick = {},
        onProviderChange = {},
        onProviderSettingsChange = {}
    )
}
