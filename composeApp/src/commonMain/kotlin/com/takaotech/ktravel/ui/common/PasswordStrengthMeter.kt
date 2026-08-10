package com.takaotech.ktravel.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.takaotech.password.PasswordStrength
import com.takaotech.password.PasswordSuggestion
import com.takaotech.password.PasswordWarning
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.password_strength_fair
import ktravel.composeapp.generated.resources.password_strength_label
import ktravel.composeapp.generated.resources.password_strength_strong
import ktravel.composeapp.generated.resources.password_strength_suggestion_add_another_word
import ktravel.composeapp.generated.resources.password_strength_suggestion_avoid_all_uppercase
import ktravel.composeapp.generated.resources.password_strength_suggestion_avoid_dates
import ktravel.composeapp.generated.resources.password_strength_suggestion_avoid_leet
import ktravel.composeapp.generated.resources.password_strength_suggestion_avoid_repeated_words
import ktravel.composeapp.generated.resources.password_strength_suggestion_avoid_reversed_words
import ktravel.composeapp.generated.resources.password_strength_suggestion_avoid_sequences
import ktravel.composeapp.generated.resources.password_strength_suggestion_avoid_years
import ktravel.composeapp.generated.resources.password_strength_suggestion_capitalization_does_not_help
import ktravel.composeapp.generated.resources.password_strength_suggestion_no_need_for_symbols
import ktravel.composeapp.generated.resources.password_strength_suggestion_password_not_allowed
import ktravel.composeapp.generated.resources.password_strength_suggestion_use_fewer_words
import ktravel.composeapp.generated.resources.password_strength_suggestion_use_longer_keyboard_pattern
import ktravel.composeapp.generated.resources.password_strength_very_strong
import ktravel.composeapp.generated.resources.password_strength_very_weak
import ktravel.composeapp.generated.resources.password_strength_warning_dates
import ktravel.composeapp.generated.resources.password_strength_warning_not_allowed
import ktravel.composeapp.generated.resources.password_strength_warning_recent_years
import ktravel.composeapp.generated.resources.password_strength_warning_repeated_character
import ktravel.composeapp.generated.resources.password_strength_warning_repeated_pattern
import ktravel.composeapp.generated.resources.password_strength_warning_sequence
import ktravel.composeapp.generated.resources.password_strength_warning_short_keyboard_pattern
import ktravel.composeapp.generated.resources.password_strength_warning_straight_row_of_keys
import ktravel.composeapp.generated.resources.password_strength_warning_top_10
import ktravel.composeapp.generated.resources.password_strength_warning_top_100
import ktravel.composeapp.generated.resources.password_strength_warning_very_common
import ktravel.composeapp.generated.resources.password_strength_weak
import org.jetbrains.compose.resources.stringResource

internal object PasswordStrengthMeterTestTags {
    const val METER = "password_strength_meter"
    const val LABEL = "password_strength_label"
}

/**
 * Shows the verdict of `:password-strength` on a password being typed.
 *
 * The module returns symbolic warnings and suggestions rather than text, so all of the wording lives
 * here in `strings.xml` — which is what keeps the module free of anything to translate.
 */
@Composable
internal fun PasswordStrengthMeter(strength: PasswordStrength?, modifier: Modifier = Modifier) {
    if (strength == null) return

    val progress by animateFloatAsState(
        targetValue = (strength.score + 1) / MAX_SCORE_STEPS,
        label = "password_strength_progress",
    )
    val color by animateColorAsState(strength.score.toColor(), label = "password_strength_color")

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PasswordStrengthMeterTestTags.METER),
        )

        Text(
            text = stringResource(Res.string.password_strength_label, strength.score.label()),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.testTag(PasswordStrengthMeterTestTags.LABEL),
        )

        // Only the first suggestion is shown: a list of five is noise while someone is typing.
        val advice = listOfNotNull(
            strength.warning?.message(),
            strength.suggestions.firstOrNull()?.message(),
        )
        advice.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Int.label(): String = stringResource(
    when (this) {
        0 -> Res.string.password_strength_very_weak
        1 -> Res.string.password_strength_weak
        2 -> Res.string.password_strength_fair
        3 -> Res.string.password_strength_strong
        else -> Res.string.password_strength_very_strong
    },
)

@Composable
private fun Int.toColor(): Color = when (this) {
    0, 1 -> MaterialTheme.colorScheme.error
    2 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}

/** Exhaustive on purpose: a new warning in the module fails the build until it is translated. */
@Composable
private fun PasswordWarning.message(): String = stringResource(
    when (this) {
        PasswordWarning.DATES -> Res.string.password_strength_warning_dates

        PasswordWarning.RECENT_YEARS -> Res.string.password_strength_warning_recent_years

        PasswordWarning.REPEATED_CHARACTER -> Res.string.password_strength_warning_repeated_character

        PasswordWarning.REPEATED_PATTERN -> Res.string.password_strength_warning_repeated_pattern

        PasswordWarning.SEQUENCE -> Res.string.password_strength_warning_sequence

        PasswordWarning.SHORT_KEYBOARD_PATTERN ->
            Res.string.password_strength_warning_short_keyboard_pattern

        PasswordWarning.STRAIGHT_ROW_OF_KEYS ->
            Res.string.password_strength_warning_straight_row_of_keys

        PasswordWarning.NOT_ALLOWED -> Res.string.password_strength_warning_not_allowed

        PasswordWarning.TOP_10_PASSWORD -> Res.string.password_strength_warning_top_10

        PasswordWarning.TOP_100_PASSWORD -> Res.string.password_strength_warning_top_100

        PasswordWarning.VERY_COMMON_PASSWORD -> Res.string.password_strength_warning_very_common
    },
)

@Composable
private fun PasswordSuggestion.message(): String = stringResource(
    when (this) {
        PasswordSuggestion.ADD_ANOTHER_WORD ->
            Res.string.password_strength_suggestion_add_another_word

        PasswordSuggestion.AVOID_DATES -> Res.string.password_strength_suggestion_avoid_dates

        PasswordSuggestion.AVOID_YEARS -> Res.string.password_strength_suggestion_avoid_years

        PasswordSuggestion.AVOID_REPEATED_WORDS ->
            Res.string.password_strength_suggestion_avoid_repeated_words

        PasswordSuggestion.AVOID_SEQUENCES -> Res.string.password_strength_suggestion_avoid_sequences

        PasswordSuggestion.USE_LONGER_KEYBOARD_PATTERN ->
            Res.string.password_strength_suggestion_use_longer_keyboard_pattern

        PasswordSuggestion.PASSWORD_NOT_ALLOWED ->
            Res.string.password_strength_suggestion_password_not_allowed

        PasswordSuggestion.AVOID_REVERSED_WORDS ->
            Res.string.password_strength_suggestion_avoid_reversed_words

        PasswordSuggestion.AVOID_PREDICTABLE_LETTER_SUBSTITUTIONS ->
            Res.string.password_strength_suggestion_avoid_leet

        PasswordSuggestion.AVOID_ALL_UPPERCASE ->
            Res.string.password_strength_suggestion_avoid_all_uppercase

        PasswordSuggestion.CAPITALIZATION_DOES_NOT_HELP ->
            Res.string.password_strength_suggestion_capitalization_does_not_help

        PasswordSuggestion.USE_FEWER_WORDS -> Res.string.password_strength_suggestion_use_fewer_words

        PasswordSuggestion.NO_NEED_FOR_SYMBOLS ->
            Res.string.password_strength_suggestion_no_need_for_symbols
    },
)

/** Scores run 0..4, so five filled steps. */
private const val MAX_SCORE_STEPS = 5f
