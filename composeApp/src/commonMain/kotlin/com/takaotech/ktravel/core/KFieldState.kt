package com.takaotech.ktravel.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Represents the state of a text field along with its validation status.
 *
 * @property value The current value of the text field, encapsulated within a `TextFieldValue` object.
 * @property validationState The current validation state of the text field, represented as an instance of `FieldValidationState`.
 */
@Stable
data class KFieldState(
    val value: TextFieldValue = TextFieldValue(""),
    val validationState: FieldValidationState = FieldValidationState.None
)

data class TextPayload(
    val text: StringResource,
    val args: List<Any> = emptyList()
)

fun StringResource.toTextPayload(): TextPayload = TextPayload(this)

/**
 * Represents the validation state of a field in the application.
 * This class encapsulates the validation state and provides methods to check if the field is valid or not.
 */
sealed class FieldValidationState(
    val supportText: String? = null,
    val errorText: TextPayload? = null,
    val isError: Boolean = false
) {
    /**
     * Represents a validation state indicating that the associated field is not yet validated.
     */
    object None : FieldValidationState()

    /**
     * Represents a validation state indicating that the associated field is valid.
     * This state implies that no errors are present, and the field meets the validation criteria.
     */
    object Valid : FieldValidationState()

    /**
     * Represents a validation state indicating that the associated field is not valid.
     * This state implies that one or more errors are present,
     * and the field does not meet the validation criteria.
     */
    open class BaseNotValid(errorText: TextPayload? = null) : FieldValidationState(errorText = errorText)

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
