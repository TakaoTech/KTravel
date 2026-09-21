package com.takaotech.ktravel.core.telemetry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Whether this installation may send diagnostics away from the device.
 *
 * Three states and not a boolean, because "has not been told yet" is not "has said no": diagnostics
 * run on the developer's legitimate interest, which does not excuse sending them before the user has
 * been shown what leaves. Under [Unknown] the introduction is still due and nothing may leave the
 * device in the meantime; from there on the user opposes, or does not.
 *
 * Serializable, with the stored names spelled out: they are written into the settings document and
 * outlive any renaming of the entries. A value this build does not know falls back to [Unknown] —
 * the settings document is read with `coerceInputValues`, so a decision that cannot be understood
 * counts as no decision, which is the safe reading.
 */
@Serializable
enum class TelemetryConsent {
    /** The user has not been shown the privacy page yet. Nothing is sent. */
    @SerialName("unknown")
    Unknown,

    /** The user left diagnostics on, which is how the introduction leaves them. */
    @SerialName("granted")
    Granted,

    /** The user objected. Diagnostics stay on the device, where the log screen still shows them. */
    @SerialName("denied")
    Denied,
}
