package com.takaotech.ktravel.data.archive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Stable contract of the archive: the one part that is **never** migrated, because it is what
 * decides whether and how to migrate the rest.
 *
 * Every field but [schemaVersion] and [travelId] has a default, so a manifest written by a future
 * version stays readable enough to produce a sensible error.
 */
@Serializable
data class TravelArchiveManifest(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("travel_id") val travelId: String,
    @SerialName("travel_name") val travelName: String = "",
    @SerialName("app_version") val appVersion: String = "",
    // Long epoch millis instead of an Instant: no dependency on the time serializers, and
    // consistent with the style the entities already use (`date_epoch_days`).
    @SerialName("exported_at_epoch_millis") val exportedAtEpochMillis: Long = 0L,
    // Explicit, so a future version can move the plan file without breaking old readers, which
    // read this field instead of the constant.
    @SerialName("plan_entry") val planEntry: String = TravelArchiveFormat.PLAN_ENTRY,
    @SerialName("attachments") val attachments: List<String> = emptyList(),
    // Signals the presence of `secrets.json`. Defaults to false: an archive written before this
    // feature existed stays readable without a migration, as the manifest rule requires.
    @SerialName("has_secrets") val hasSecrets: Boolean = false,
)
