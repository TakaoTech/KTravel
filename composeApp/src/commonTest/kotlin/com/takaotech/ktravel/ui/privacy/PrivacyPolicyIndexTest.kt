package com.takaotech.ktravel.ui.privacy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicySection
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyNode
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyUiState
import io.kotest.core.spec.style.BehaviorSpec
import kotlinx.collections.immutable.persistentListOf

/** The last section of the document, several screens below the first. */
private const val LAST_PATH = "contact"

/**
 * The index of the privacy policy.
 *
 * The document is long enough that its last sections are several screens down, which is the whole
 * reason the index exists: it has to list every section, including the nested ones, and jumping to
 * one has to actually bring it on screen.
 */
@OptIn(ExperimentalTestApi::class)
class PrivacyPolicyIndexTest :
    BehaviorSpec({
        given("the policy shown on a screen too small to hold it") {
            `when`("the index is shut") {
                then("no entry is listed, and the bar is still there to open") {
                    runComposeUiTest {
                        setContent { PolicyUnderTest() }

                        onNodeWithTag(PrivacyPolicyTestTags.INDEX_TOGGLE).assertIsDisplayed()
                        onNodeWithTag(PrivacyPolicyTestTags.indexEntry(LAST_PATH)).assertDoesNotExist()
                    }
                }
            }

            `when`("the bar is touched") {
                then("every section is listed, the nested ones included") {
                    runComposeUiTest {
                        setContent { PolicyUnderTest() }

                        onNodeWithTag(PrivacyPolicyTestTags.INDEX_TOGGLE).performClick()

                        onNodeWithTag(PrivacyPolicyTestTags.indexEntry("collection")).assertIsDisplayed()
                        onNodeWithTag(PrivacyPolicyTestTags.indexEntry("collection.automatic")).assertIsDisplayed()

                        // The index is taller than it is allowed to be, so its own tail is a scroll
                        // away rather than on screen — which is what the bounded height is for.
                        onNodeWithTag(PrivacyPolicyTestTags.indexEntry(LAST_PATH)).assertExists()
                    }
                }
            }

            `when`("an entry far down the document is picked") {
                then("the document jumps to it and the index gets out of the way") {
                    runComposeUiTest {
                        setContent { PolicyUnderTest() }

                        onNodeWithTag(PrivacyPolicyTestTags.section("collection")).assertIsDisplayed()
                        onNodeWithTag(PrivacyPolicyTestTags.section(LAST_PATH)).assertDoesNotExist()

                        onNodeWithTag(PrivacyPolicyTestTags.INDEX_TOGGLE).performClick()
                        onNodeWithTag(PrivacyPolicyTestTags.indexEntry(LAST_PATH)).performScrollTo().performClick()

                        onNodeWithTag(PrivacyPolicyTestTags.section(LAST_PATH)).assertIsDisplayed()
                        onNodeWithTag(PrivacyPolicyTestTags.section("collection")).assertDoesNotExist()
                        onNodeWithTag(PrivacyPolicyTestTags.indexEntry(LAST_PATH)).assertDoesNotExist()
                    }
                }
            }
        }
    })

/** A window small enough that the last section is several screens below the first. */
@Composable
private fun PolicyUnderTest() {
    Box(modifier = Modifier.requiredSize(width = 360.dp, height = 480.dp)) {
        PrivacyPolicyContent(state = PrivacyPolicyUiState(nodes = TEST_NODES) {})
    }
}

private val TEST_NODES = persistentListOf(
    node("collection", 0, "collection", "1. Information We Collect"),
    node("collection.provided", 1, "provided", "1.1 Information You Provide Directly"),
    node("collection.automatic", 1, "automatic", "1.2 Information Collected Automatically"),
    node("collection.permissions", 1, "permissions", "1.3 Device Permissions"),
    node("use", 0, "use", "2. How We Use Your Information"),
    node("sdks", 0, "sdks", "3. Third-Party SDKs and Services"),
    node("sharing", 0, "sharing", "6. Data Sharing and Disclosure"),
    node("retention", 0, "retention", "8. Data Retention"),
    node("rights", 0, "rights", "9. Your Rights"),
    node("rights.gdpr", 1, "gdpr", "9.2 GDPR Rights (EU/EEA Users)"),
    node("changes", 0, "changes", "13. Changes to This Privacy Policy"),
    node(LAST_PATH, 0, "contact", "14. Contact Us"),
)

private fun node(path: String, depth: Int, id: String, title: String) = PrivacyPolicyNode(
    path = path,
    depth = depth,
    section = PrivacyPolicySection(
        id = id,
        title = title,
        body = "What the section says, in a sentence or two.",
    ),
)
