package com.takaotech.ktravel.ui.consent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.takaotech.ktravel.domain.staticflows.ConsentCard
import com.takaotech.ktravel.domain.staticflows.ConsentMedia
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

/**
 * One card of the privacy notice: an illustration, a heading and a body in Markdown.
 *
 * The body is Markdown and not a plain string because it is a legal text that has to breathe —
 * emphasis and bullet lists — and because the renderer is already here, drawing the trip notes.
 *
 * @param card What to draw.
 * @param modifier The modifier applied to the card.
 */
@Composable
internal fun ConsentCardItem(card: ConsentCard, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag(ConsentTestTags.card(card.id)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        card.media?.let { ConsentCardMedia(it) }

        Text(
            text = card.title,
            style = MaterialTheme.typography.headlineSmall,
        )

        Markdown(content = card.message, modifier = Modifier.fillMaxWidth())
    }
}

/** The illustration of a card: a Lottie animation, or a drawable already in the application. */
@Composable
private fun ConsentCardMedia(media: ConsentMedia, modifier: Modifier = Modifier) {
    when (media) {
        is ConsentMedia.Lottie -> {
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

        // A name that no longer matches a drawable draws nothing rather than crashing: the notice
        // is content, and content must not be able to take the first screen of the app down.
        is ConsentMedia.Static -> Res.allDrawableResources[media.name]?.let { drawable ->
            Image(
                painter = painterResource(drawable),
                contentDescription = null,
                modifier = modifier.size(STATIC_MEDIA_SIZE),
            )
        }
    }
}
