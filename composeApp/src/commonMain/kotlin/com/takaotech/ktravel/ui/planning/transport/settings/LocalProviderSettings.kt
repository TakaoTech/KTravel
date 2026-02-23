package com.takaotech.ktravel.ui.planning.transport.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.LocalTransportMode
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalProviderSettings(
    settings: RoutingProviderSettings.Local,
    onSettingsChange: (RoutingProviderSettings.Local) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Avoid Tolls")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = settings.avoidTolls,
                onCheckedChange = { onSettingsChange(settings.copy(avoidTolls = it)) }
            )
        }

        Text("Transport Mode")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            LocalTransportMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = settings.transportMode == mode,
                    onClick = { onSettingsChange(settings.copy(transportMode = mode)) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = LocalTransportMode.entries.size
                    )
                ) {
                    Text(mode.name)
                }
            }
        }
    }
}
