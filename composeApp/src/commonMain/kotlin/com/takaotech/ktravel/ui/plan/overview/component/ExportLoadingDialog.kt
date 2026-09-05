package com.takaotech.ktravel.ui.plan.overview.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_trip_cd_export_animation
import ktravel.composeapp.generated.resources.planning_trip_cd_export_completed_animation
import ktravel.composeapp.generated.resources.planning_trip_export_in_progress
import ktravel.composeapp.generated.resources.planning_trip_export_success
import org.jetbrains.compose.resources.stringResource

/**
 * Blocking feedback while the archive is being written: the export cannot be cancelled, so the
 * dialog ignores back press and outside taps.
 *
 * When [isCompleted] turns true the running train slides to the right and is replaced by the train
 * arriving at the station; [onCompletionAnimationEnd] fires once that arrival has played through,
 * so the caller can dismiss the dialog.
 */
@Composable
internal fun ExportLoadingDialog(isCompleted: Boolean, onCompletionAnimationEnd: () -> Unit) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            // Both compositions are loaded upfront: the arrival must be ready when the transition
            // starts, otherwise the slide would carry an empty frame.
            val inProgressComposition by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/lottie_train_export.json").decodeToString(),
                )
            }
            val completedComposition by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/lottie_train_station_export.json").decodeToString(),
                )
            }

            AnimatedContent(
                targetState = isCompleted,
                transitionSpec = {
                    // The train keeps travelling to the right: the arrival enters from the left
                    // while the running train leaves through the right edge.
                    slideInHorizontally { width -> -width } togetherWith
                        slideOutHorizontally { width -> width }
                },
            ) { completed ->
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (completed) {
                        ExportTrainAnimation(
                            composition = completedComposition,
                            iterations = 1,
                            contentDescription = stringResource(
                                Res.string.planning_trip_cd_export_completed_animation,
                            ),
                            onAnimationEnd = onCompletionAnimationEnd,
                        )

                        Text(
                            text = stringResource(Res.string.planning_trip_export_success),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    } else {
                        ExportTrainAnimation(
                            composition = inProgressComposition,
                            iterations = Compottie.IterateForever,
                            contentDescription = stringResource(
                                Res.string.planning_trip_cd_export_animation,
                            ),
                        )

                        Text(
                            text = stringResource(Res.string.planning_trip_export_in_progress),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}
