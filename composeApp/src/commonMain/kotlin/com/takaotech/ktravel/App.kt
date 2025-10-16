package com.takaotech.ktravel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import org.jetbrains.compose.ui.tooling.preview.Preview

import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.compose_multiplatform

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
@Preview
fun App() {
    val scaffoldNavigator = rememberSupportingPaneScaffoldNavigator()
    val scope = rememberCoroutineScope()

//    ThreePaneScaffoldPredictiveBackHandler(
//        navigator = scaffoldNavigator,
//        backBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange
//    )

    MaterialTheme {
        SupportingPaneScaffold(
            mainPane = {
                Text("testo")
            },
            directive = scaffoldNavigator.scaffoldDirective,
            value = scaffoldNavigator.scaffoldValue,
            supportingPane = {
                Text("supporto")
            },
        )
    }
}