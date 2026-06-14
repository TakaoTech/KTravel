package com.takaotech.ktravel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.takaotech.ktravel.di.LocalAppGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appGraph = (application as KTravelApplication).appGraph

        setContent {
            CompositionLocalProvider(LocalAppGraph provides appGraph) {
                App()
            }
        }
    }
}

//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}