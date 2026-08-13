package com.takaotech.navigator.api.common

import kotlinx.serialization.Serializable

/**
 * Units of measurement a provider should use for the values it localizes, such as the text of a
 * navigation instruction.
 *
 * The numeric fields of the response are always metric: distances are meters and durations are
 * seconds, whatever is requested here. Converting them is a presentation concern, and a contract
 * whose units depend on a request field cannot be read without also reading the request.
 */
@Serializable
enum class Units {
    /** Meters and kilometers. */
    METRIC,

    /** Feet and miles. */
    IMPERIAL,
}
