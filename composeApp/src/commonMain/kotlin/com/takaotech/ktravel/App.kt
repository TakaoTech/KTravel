package com.takaotech.ktravel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.takaotech.os_map.MapForge
import kotlinx.coroutines.launch
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val coroutine = rememberCoroutineScope()
        val navigator = rememberListDetailPaneScaffoldNavigator()


        PanelHorizontalDivided(
            modifier = Modifier.fillMaxSize(),
            scaffoldNavigator = navigator,
            mainPane = {
                Text("Left Box")
            },
            supportPane = {
                Scaffold(
                    topBar = {
                        IconButton(
                            modifier = Modifier,
                            onClick = {
                                coroutine.launch {
                                    navigator.navigateBack()
                                }
                            }
                        ) {
                            Icon(painter = painterResource(Res.drawable.arrow_back), contentDescription = null)
                        }
                    }
                ) {
                    MapForge(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        showFps = true
                    )
                }
            }
        )
    }
}