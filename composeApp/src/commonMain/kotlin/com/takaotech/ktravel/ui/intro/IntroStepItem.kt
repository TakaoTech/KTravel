package com.takaotech.ktravel.ui.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.takaotech.ktravel.domain.staticflows.IntroMedia
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.domain.staticflows.PrivacyDetail
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.allDrawableResources
import ktravel.composeapp.generated.resources.intro_cd_open_detail
import ktravel.composeapp.generated.resources.open_in_new
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** How tall the illustration is allowed to be, relative to its own width. */
private const val MEDIA_ASPECT_RATIO = 16f / 9f

/** The drawable illustrations are icons, drawn at the size an icon is legible at. */
private val STATIC_MEDIA_SIZE = 96.dp

/**
 * One step of the introduction: a page to read, the privacy points, or the question.
 *
 * The bodies are Markdown and not plain strings because they have to breathe — emphasis and bullet
 * lists — and because the renderer is already here, drawing the trip notes.
 *
 * @param step What to draw.
 * @param onPolicyOpened Called with the path of the policy section a privacy point asks to open.
 * @param modifier The modifier applied to the step.
 */
@Composable
internal fun IntroStepItem(step: IntroStep, onPolicyOpened: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag(IntroTestTags.step(step.id)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (step) {
            is IntroStep.Card -> step.media?.let { IntroStepMedia(it) }
            is IntroStep.Decision -> step.media?.let { IntroStepMedia(it) }
            is IntroStep.Privacy -> Unit
        }

        Text(text = step.title, style = MaterialTheme.typography.headlineSmall)

        when (step) {
            is IntroStep.Card -> Markdown(content = step.body, modifier = Modifier.fillMaxWidth())

            is IntroStep.Decision -> Markdown(content = step.body, modifier = Modifier.fillMaxWidth())

            is IntroStep.Privacy -> {
                step.body?.let { Markdown(content = it, modifier = Modifier.fillMaxWidth()) }

                step.details.forEach { detail ->
                    PrivacyDetailRow(detail = detail, onClick = { onPolicyOpened(detail.policyRef) })
                }
            }
        }
    }
}

/**
 * One privacy point: the summary, and the whole row as the way into what the policy says about it.
 *
 * The row is the target rather than an icon button beside it — a point is one thing to read and one
 * thing to open, and a full width target is what a thumb finds. Touching it opens the policy itself,
 * scrolled to the section this point summarises, so what is read is the document and not a second
 * copy of it.
 */
@Composable
private fun PrivacyDetailRow(detail: PrivacyDetail, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag(IntroTestTags.detail(detail.id)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = detail.title, style = MaterialTheme.typography.titleMedium)

                Markdown(content = detail.body)
            }

            Icon(
                painter = painterResource(Res.drawable.open_in_new),
                contentDescription = stringResource(Res.string.intro_cd_open_detail),
            )
        }
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
                modifier = modifier.fillMaxWidth().aspectRatio(MEDIA_ASPECT_RATIO),
            )
        }

        // A name that no longer matches a drawable draws nothing rather than crashing: the
        // introduction is content, and content must not be able to take the first screen of the app
        // down.
        is IntroMedia.Static -> Res.allDrawableResources[media.name]?.let { drawable ->
            Image(
                painter = painterResource(drawable),
                contentDescription = null,
                modifier = modifier.size(STATIC_MEDIA_SIZE),
            )
        }
    }
}
