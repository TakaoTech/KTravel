package com.takaotech.ktravel.ui.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.staticflows.IntroMedia
import com.takaotech.ktravel.domain.staticflows.IntroStep
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.allDrawableResources
import org.jetbrains.compose.resources.painterResource

/** How tall the illustration is allowed to be, relative to its own width. */
private const val MEDIA_ASPECT_RATIO = 16f / 9f

/** The drawable illustrations are icons, drawn at the size an icon is legible at. */
private val STATIC_MEDIA_SIZE = 96.dp

/** The margin every step keeps from the edges of the screen. */
private val STEP_HORIZONTAL_PADDING = 24.dp
private val STEP_VERTICAL_PADDING = 16.dp

/** How far apart an illustrated step keeps its picture, its heading and its text. */
private val ILLUSTRATED_SPACING = 16.dp

/**
 * One step of the introduction: a page to read, the privacy points, or the question.
 *
 * The bodies are Markdown and not plain strings because they have to breathe — emphasis and bullet
 * lists — and because the renderer is already here, drawing the trip notes.
 *
 * The three shapes are drawn by three composables rather than by one with branches inside it: a
 * page that illustrates something is centred around its picture, and the privacy page is a column
 * of text and boxes that has to line up on its left edge. Only the scrolling frame is shared.
 *
 * @param step What to draw.
 * @param isAcknowledged Whether the reader has ticked the privacy acknowledgement. Only the privacy
 *   step draws it; the others ignore it.
 * @param onAcknowledgedChange Called with the new value when that tick is toggled.
 * @param selectedConsent Which answer the question is currently resting on. Only the step that asks
 *   it draws this; the others ignore it.
 * @param onConsentChange Called with the answer the reader picked.
 * @param onPolicyOpen Called with the path of the policy section a privacy point asks to open,
 *   or with null when the document itself is asked for.
 * @param modifier The modifier applied to the step.
 */
@Composable
internal fun IntroStepItem(
    step: IntroStep,
    isAcknowledged: Boolean,
    onAcknowledgedChange: (Boolean) -> Unit,
    selectedConsent: TelemetryConsent,
    onConsentChange: (TelemetryConsent) -> Unit,
    onPolicyOpen: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val page = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = STEP_HORIZONTAL_PADDING, vertical = STEP_VERTICAL_PADDING)
        .testTag(IntroTestTags.step(step.id))

    when (step) {
        is IntroStep.Card -> IntroIllustratedStep(
            media = step.media,
            title = step.title,
            body = step.body,
            modifier = page,
        )

        is IntroStep.Decision -> IntroDecisionStep(
            step = step,
            selectedConsent = selectedConsent,
            onConsentChange = onConsentChange,
            modifier = page,
        )

        is IntroStep.Privacy -> IntroPrivacyStep(
            step = step,
            isAcknowledged = isAcknowledged,
            onAcknowledgedChange = onAcknowledgedChange,
            onPolicyOpened = onPolicyOpen,
            modifier = page,
        )
    }
}

/** A page that is only read: the picture it is built around, then what it has to say. */
@Composable
private fun IntroIllustratedStep(media: IntroMedia?, title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ILLUSTRATED_SPACING),
    ) {
        media?.let { IntroStepMedia(it) }

        Text(text = title, style = MaterialTheme.typography.headlineSmall)

        Markdown(content = body, modifier = Modifier.fillMaxWidth())
    }
}

/** The illustration of a step: a Lottie animation, or a drawable already in the application. */
@Composable
private fun IntroStepMedia(media: IntroMedia, modifier: Modifier = Modifier) {
    when (media) {
        is IntroMedia.Lottie -> {
            val composition by rememberLottieComposition {
                LottieCompositionSpec.JsonString(Res.readBytes(media.path).decodeToString())
            }
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = Compottie.IterateForever,
            )

            Image(
                painter = rememberLottiePainter(composition = composition, progress = { progress }),
                contentDescription = null,
                modifier = modifier
                    .height(100.dp)
                    .aspectRatio(MEDIA_ASPECT_RATIO),
            )
        }

        is IntroMedia.Static -> Res.allDrawableResources[media.name]?.let { drawable ->
            // TODO Add support for a boolean for apply dark/light theme
            Image(
                painter = painterResource(drawable),
                contentDescription = null,
                modifier = modifier.size(STATIC_MEDIA_SIZE),
            )
        }
    }
}
