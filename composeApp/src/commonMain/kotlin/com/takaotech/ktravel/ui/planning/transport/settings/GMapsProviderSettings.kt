package com.takaotech.ktravel.ui.planning.transport.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.GMapsTravelMode
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GMapsProviderSettings(
    settings: RoutingProviderSettings.GMaps,
    onSettingsChange: (RoutingProviderSettings.GMaps) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Avoid Tolls")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = settings.avoidTolls,
                onCheckedChange = { onSettingsChange(settings.copy(avoidTolls = it)) },
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Avoid Ferries")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = settings.avoidFerries,
                onCheckedChange = { onSettingsChange(settings.copy(avoidFerries = it)) },
            )
        }

        Text("Travel Mode")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            GMapsTravelMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = settings.travelMode == mode,
                    onClick = { onSettingsChange(settings.copy(travelMode = mode)) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = GMapsTravelMode.entries.size,
                    ),
                ) {
                    Text(mode.name)
                }
            }
        }
    }
}
