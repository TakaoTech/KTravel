package com.takaotech.ktravel.ui.plan.overview.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottiePainter

/**
 * Native size of `files/lottie_train_export.json` and `files/lottie_train_station_export.json`, so
 * the animations render without empty margins.
 */
internal const val EXPORT_ANIMATION_ASPECT_RATIO = 1920f / 651f

/**
 * Plays [composition] for [iterations] and notifies [onAnimationEnd] when the playback is over.
 * The callback never fires for [Compottie.IterateForever] nor when the animation is cancelled by
 * leaving the composition.
 */
@Composable
internal fun ExportTrainAnimation(
    composition: LottieComposition?,
    iterations: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = { },
) {
    val animatable = rememberLottieAnimatable()
    val currentOnAnimationEnd by rememberUpdatedState(onAnimationEnd)

    LaunchedEffect(composition, iterations) {
        animatable.animate(
            composition = composition ?: return@LaunchedEffect,
            iterations = iterations,
        )
        currentOnAnimationEnd()
    }

    Image(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(EXPORT_ANIMATION_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.large),
        painter = rememberLottiePainter(
            composition = composition,
            progress = animatable::value,
        ),
        contentDescription = contentDescription,
    )
}
