package com.takaotech.ktravel.ui.shared.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * How long a map waits for the navigation enter transition before giving up on it.
 *
 * Building the map costs a frame a screen cannot spare while it is being animated in, and the
 * entry being animated only reaches `RESUMED` once the transition ends. A host that never resumes
 * its content must not keep the map out forever, hence the bound.
 */
internal val MAP_TRANSITION_TIMEOUT = 1.seconds

/**
 * Whether a map can be built now: once the screen is resumed, or after [MAP_TRANSITION_TIMEOUT]
 * regardless.
 *
 * `MapLibre.getInstance` loads the native library and `MapView` creates its surface, both on the
 * main thread, and doing that mid transition is what makes opening a page with a map stutter. The
 * caller draws a placeholder until this turns true, and it never turns back: a map that is up stays
 * up.
 */
@Composable
internal fun rememberMapReady(): Boolean {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var isMapReady by remember { mutableStateOf(false) }
    LaunchedEffect(lifecycle) {
        if (!isMapReady) {
            withTimeoutOrNull(MAP_TRANSITION_TIMEOUT) {
                lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            }
            isMapReady = true
        }
    }
    return isMapReady
}
