package com.takaotech.ktravel.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.presentation.settings.SettingsViewModel
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import org.jetbrains.compose.resources.painterResource

@Serializable
object SettingsNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    viewModel: SettingsViewModel,
    onNavigationBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "HERE API Key",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = uiState.hereApiKey,
                onValueChange = { viewModel.onHereApiKeyChanged(it) },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
