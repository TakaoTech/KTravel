package com.takaotech.password

/**
 * The verdict on a password.
 *
 * @property score 0 to 4, with the same meaning as zxcvbn: 0 "too guessable", 1 "very guessable",
 *  2 "somewhat guessable", 3 "safely unguessable", 4 "very unguessable".
 * @property entropy estimated bits of entropy.
 * @property warning what is wrong with the password, if anything stood out.
 * @property suggestions what would make it better, most relevant first.
 */
public data class PasswordStrength(
    val score: Int,
    val entropy: Double,
    val warning: PasswordWarning?,
    val suggestions: List<PasswordSuggestion>,
)

/**
 * Why a password is weak.
 *
 * Upstream nbvcxz resolves these against a `ResourceBundle`. Here they stay symbolic and the caller
 * maps them onto its own translations, so this module carries no strings to localise.
 */
public enum class PasswordWarning {
    /** Contains a date. */
    DATES,

    /** Contains a recent year. */
    RECENT_YEARS,

    /** A single character repeated, like `aaa`. */
    REPEATED_CHARACTER,

    /** A block repeated, like `abcabcabc`. */
    REPEATED_PATTERN,

    /** A run along the alphabet or the digits, like `abcdef`. */
    SEQUENCE,

    /** A short keyboard pattern. */
    SHORT_KEYBOARD_PATTERN,

    /** A straight row of keys, like `qwerty`. */
    STRAIGHT_ROW_OF_KEYS,

    /** Present in a dictionary the caller marked as disallowed. */
    NOT_ALLOWED,

    /** Among the ten most common passwords. */
    TOP_10_PASSWORD,

    /** Among the hundred most common passwords. */
    TOP_100_PASSWORD,

    /** A very common password. */
    VERY_COMMON_PASSWORD,
}

/** What the user could do about it. */
public enum class PasswordSuggestion {
    ADD_ANOTHER_WORD,
    AVOID_DATES,
    AVOID_YEARS,
    AVOID_REPEATED_WORDS,
    AVOID_SEQUENCES,
    USE_LONGER_KEYBOARD_PATTERN,
    PASSWORD_NOT_ALLOWED,
    AVOID_REVERSED_WORDS,
    AVOID_PREDICTABLE_LETTER_SUBSTITUTIONS,
    AVOID_ALL_UPPERCASE,
    CAPITALIZATION_DOES_NOT_HELP,
    USE_FEWER_WORDS,
    NO_NEED_FOR_SYMBOLS,
}

/** Estimates how hard a password would be to guess. */
public interface PasswordStrengthEvaluator {

    /**
     * @param userInputs values the attacker plausibly knows — a name, an email, the app name —
     *  which are treated as dictionary entries of their own.
     */
    public fun evaluate(password: String, userInputs: List<String> = emptyList()): PasswordStrength
}
