package com.takaotech.ktravel.ui.plan.day.placestep

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.plan.StepUi
import com.takaotech.ktravel.presentation.plan.VisitScheduleUi
import com.takaotech.ktravel.presentation.plan.day.StepNotesUiState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime

/**
 * Layout of the schedule fields at different font scales. [LocalInspectionMode] keeps MapLibre out
 * of the composition (it needs a real graphics context), and [LocalDensity] carries the font scale:
 * `DeviceConfigurationOverride.FontScale` throws on skiko, which is the only target these UI suites
 * run on.
 */
@OptIn(ExperimentalTestApi::class)
class PlaceStepContentTest : BehaviorSpec() {

    private val place = StepUi.Place(
        id = "step-a",
        name = "Tokyo Tower",
        lat = 35.6586,
        lng = 139.7454,
        schedule = VisitScheduleUi(startTime = LocalTime(9, 30), endTime = LocalTime(11, 0)),
    )

    private fun ComposeUiTest.setStepDetail(fontScale: Float, width: Dp) {
        setContent {
            CompositionLocalProvider(
                LocalInspectionMode provides true,
                LocalDensity provides Density(density = 1f, fontScale = fontScale),
            ) {
                Box(modifier = Modifier.width(width)) {
                    PlaceStepContent(
                        place = place,
                        notes = StepNotesUiState(),
                        onBack = {},
                        onNotesEvent = {},
                        onSetStartTime = {},
                        onSetEndTime = {},
                    )
                }
            }
        }
    }

    init {
        given("the step detail at the default font scale on a phone-sized window") {
            then("the two schedule fields should sit side by side") {
                runComposeUiTest {
                    setStepDetail(fontScale = 1f, width = PHONE_WIDTH)

                    val start = onNodeWithTag(PlaceStepTestTags.START_TIME_FIELD)
                        .getUnclippedBoundsInRoot()
                    val end = onNodeWithTag(PlaceStepTestTags.END_TIME_FIELD)
                        .getUnclippedBoundsInRoot()

                    end.top shouldBe start.top
                    start.right shouldBeLessThanOrEqualTo end.left
                    // Both fields wrap their content together, so they keep the same height.
                    (end.bottom - end.top) shouldBe (start.bottom - start.top)
                }
            }
        }

        given("the step detail at a large font scale on a narrow window") {
            then("the two schedule fields should stack full width") {
                runComposeUiTest {
                    setStepDetail(fontScale = LARGE_FONT_SCALE, width = NARROW_WIDTH)

                    val start = onNodeWithTag(PlaceStepTestTags.START_TIME_FIELD)
                        .getUnclippedBoundsInRoot()
                    val end = onNodeWithTag(PlaceStepTestTags.END_TIME_FIELD)
                        .getUnclippedBoundsInRoot()

                    end.top shouldBeGreaterThanOrEqualTo start.bottom
                    onNodeWithTag(PlaceStepTestTags.START_TIME_FIELD)
                        .assertWidthIsAtLeast(NARROW_WIDTH - HORIZONTAL_INSET)
                    onNodeWithTag(PlaceStepTestTags.END_TIME_FIELD)
                        .assertWidthIsAtLeast(NARROW_WIDTH - HORIZONTAL_INSET)
                }
            }
        }

        given("the step detail at a large font scale on a phone-sized window") {
            then("a schedule field should stay wider than it is tall") {
                runComposeUiTest {
                    setStepDetail(fontScale = LARGE_FONT_SCALE, width = PHONE_WIDTH)

                    val start = onNodeWithTag(PlaceStepTestTags.START_TIME_FIELD)
                        .getUnclippedBoundsInRoot()

                    (start.right - start.left) shouldBeGreaterThan (start.bottom - start.top)
                }
            }
        }
    }

    private companion object {
        /** Width of a typical phone in dp. */
        val PHONE_WIDTH = 420.dp

        /**
         * Width of a resized desktop window, narrow enough that half of it cannot hold a clock
         * value: the fields have to stack whatever the length of their label in the current
         * translation.
         */
        val NARROW_WIDTH = 240.dp

        /** Screen padding of the detail content, on both sides. */
        val HORIZONTAL_INSET = 32.dp

        const val LARGE_FONT_SCALE = 2f
    }
}
