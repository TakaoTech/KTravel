package com.takaotech.ktravel.core.telemetry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Whether this installation may send diagnostics away from the device.
 *
 * Three states and not a boolean, because "has not decided yet" is not "has said no": under
 * [Unknown] the application has to ask, and nothing may leave the device in the meantime.
 *
 * Serializable, with the stored names spelled out: they are written into the settings document and
 * outlive any renaming of the entries. A value this build does not know falls back to [Unknown] —
 * the settings document is read with `coerceInputValues`, so a decision that cannot be understood
 * counts as no decision, which is the safe reading.
 */
@Serializable
enum class TelemetryConsent {
    /** The user has not answered, or their answer has expired. Nothing is sent. */
    @SerialName("unknown")
    Unknown,

    /** The user agreed to send diagnostics. */
    @SerialName("granted")
    Granted,

    /** The user refused. Diagnostics stay on the device, where the log screen still shows them. */
    @SerialName("denied")
    Denied,
}
