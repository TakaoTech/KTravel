package com.takaotech.ktravel.ui.settings.licenses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import ktravel.composeapp.generated.resources.Res

/**
 * The dependencies this installation is built on, and the license each one is distributed under.
 *
 * The list is not written by hand: the AboutLibraries Gradle plugin collects it from the module
 * classpaths and the build packages it as a Compose resource, so Android, iOS and desktop all read
 * the same file. A build made outside the release CI collects it offline, which leaves the license
 * texts empty — see the `aboutLibraries` block in composeApp/build.gradle.kts.
 */
@Composable
fun LicensesPage(onNavigationBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val libraries by produceLibraries {
        Res.readBytes(ABOUT_LIBRARIES_RESOURCE).decodeToString()
    }

    LicensesContent(
        libraries = libraries,
        onNavigationBackClick = onNavigationBackClick,
        modifier = modifier,
    )
}
