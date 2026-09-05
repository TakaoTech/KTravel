package com.takaotech.ktravel.ui.settings.licenses

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.mikepenz.aboutlibraries.Libs
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Stands in for the metadata the AboutLibraries Gradle plugin generates. Inline rather than read
 * from the generated Compose resource: the test is about the rendering, and pinning it to the real
 * dependency list would make it fail on every dependency bump.
 */
private const val LIBRARIES_JSON = """
{
  "libraries": [
    {
      "uniqueId": "com.example:sample",
      "artifactVersion": "1.2.3",
      "name": "Sample Library",
      "licenses": ["Apache-2.0"]
    }
  ],
  "licenses": {
    "Apache-2.0": {
      "name": "Apache-2.0",
      "url": "https://spdx.org/licenses/Apache-2.0.html",
      "spdxId": "Apache-2.0"
    }
  }
}
"""

@OptIn(ExperimentalTestApi::class)
class LicensesPageTest : BehaviorSpec() {

    init {
        given("the license metadata of the dependencies") {
            `when`("the page is shown with it") {
                then("it should list every library it declares") {
                    val libraries = Libs.Builder().withJson(LIBRARIES_JSON).build()

                    runComposeUiTest {
                        setContent {
                            LicensesContent(libraries = libraries, onNavigationBackClick = {})
                        }

                        onNodeWithText("Sample Library").assertIsDisplayed()
                    }
                }
            }

            `when`("the page is shown before the metadata is read") {
                then("it should render the empty list rather than fail") {
                    runComposeUiTest {
                        setContent {
                            LicensesContent(libraries = null, onNavigationBackClick = {})
                        }

                        onNodeWithTag(LicensesTestTags.LIBRARIES).assertIsDisplayed()
                        onNodeWithText("Sample Library").assertDoesNotExist()
                    }
                }
            }
        }
    }
}
