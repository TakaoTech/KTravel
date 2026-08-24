package com.takaotech.ktravel.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.variant.LibrariesVariant
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.licenses_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Route to the licenses of the dependencies, reachable from the app settings. */
@Serializable
object LicensesNavigation

internal object LicensesTestTags {
    const val LIBRARIES = "licenses_libraries"
}

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

    LicensesPage(
        libraries = libraries,
        onNavigationBackClick = onNavigationBackClick,
        modifier = modifier,
    )
}

/** Rendering half of [LicensesPage], with the metadata already loaded. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LicensesPage(libraries: Libs?, onNavigationBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.licenses_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigationBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        LibrariesContainer(
            libraries = libraries,
            variant = LibrariesVariant.Refined,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag(LicensesTestTags.LIBRARIES),
        )
    }
}

private const val ABOUT_LIBRARIES_RESOURCE = "files/aboutlibraries.json"
