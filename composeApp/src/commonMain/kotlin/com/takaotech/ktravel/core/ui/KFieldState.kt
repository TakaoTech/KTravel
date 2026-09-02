package com.takaotech.ktravel.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * What a text field holds and what validation has to say about it, hoisted out of the field itself.
 *
 * @property value What is typed, with the selection and the composition the field needs to keep the
 * caret where the user left it.
 * @property validationState What validation made of [value], which is what decides whether the
 * field is drawn as an error and what is written under it.
 */
@Stable
data class KFieldState(
    val value: TextFieldValue = TextFieldValue(""),
    val validationState: FieldValidationState = FieldValidationState.None,
)

/**
 * A message to show, kept unresolved until it is drawn.
 *
 * Validation runs in a view model, where there is no composition to resolve a resource against, so
 * what travels is the resource and the values to fill it with, not a finished string. It is also
 * what makes the message follow a change of language without the state being computed again.
 *
 * @property text The string resource to show.
 * @property args Values to fill its placeholders with, in the order the resource declares them.
 */
data class TextPayload(val text: StringResource, val args: List<Any> = emptyList())

/** The message this resource makes on its own, with no placeholder to fill. */
fun StringResource.toTextPayload(): TextPayload = TextPayload(this)

/**
 * What validation made of a field: nothing yet, a pass, or a failure with the reason to show.
 *
 * Equality is by content and not by identity, which is what lets an unchanged state leave a
 * `StateFlow` unchanged and the field unrecomposed.
 *
 * @property supportText Hint written under the field while it is not in error.
 * @property errorText Why the field is in error, unresolved until it is drawn.
 * @property isError Whether the state itself declares an error. [BaseNotValid] leaves it `false`
 * and is recognised by its type instead, so a screen that reads this rather than testing for
 * [BaseNotValid] never sees a failed field.
 */
sealed class FieldValidationState(
    val supportText: String? = null,
    val errorText: TextPayload? = null,
    val isError: Boolean = false,
) {
    /** The field has not been validated yet, which is where every field starts. */
    object None : FieldValidationState()

    /** The field passed validation. */
    object Valid : FieldValidationState()

    /**
     * The field failed validation, with the reason to show under it.
     *
     * Open, though nothing subclasses it today: it is built as it is, with the message to show.
     * The type is what a screen tests for, since [isError] stays `false` on it.
     */
    open class BaseNotValid(errorText: TextPayload? = null) : FieldValidationState(errorText = errorText)

    /** The error resolved against the composition's language, or null when there is no error. */
    @Suppress("SpreadOperator")
    @Composable
    fun kErrorStringResource(): String? {
        if (errorText == null) return null
        return stringResource(errorText.text, *errorText.args.toTypedArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FieldValidationState

        if (isError != other.isError) return false
        if (supportText != other.supportText) return false
        if (errorText != other.errorText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isError.hashCode()
        result = 31 * result + (supportText?.hashCode() ?: 0)
        result = 31 * result + (errorText?.hashCode() ?: 0)
        return result
    }
}
