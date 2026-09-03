package com.takaotech.ktravel.data.archive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Stable contract of the archive: the one part that is **never** migrated, because it is what
 * decides whether and how to migrate the rest.
 *
 * Every field but [schemaVersion] and [travelId] has a default, so a manifest written by a future
 * version stays readable enough to produce a sensible error.
 *
 * @property schemaVersion Version the plan entry is written in, which is what decides whether the
 * archive is migrated, refused as too old, or refused as written by a newer build.
 * @property travelId Identity the plan had where it was exported. The import compares it with the
 * plans already stored to find out whether it is about to overwrite one.
 * @property travelName Name of the plan, repeated here so the import can name it before the plan
 * entry is read.
 * @property appVersion Build that wrote the archive. Diagnostic only: nothing is ever accepted or
 * refused on it.
 * @property exportedAtEpochMillis When the archive was written, in epoch milliseconds. A plain
 * `Long` rather than an `Instant`: no dependency on the time serializers, and consistent with the
 * style the entities already use (`date_epoch_days`).
 * @property planEntry Where the plan sits inside the archive. Explicit, so a future version can
 * move it without breaking old readers, which read this field instead of the constant.
 * @property attachments Entry paths of the files carried, `attachments/` prefix included.
 * @property hasSecrets Whether `secrets.json` is there. Defaults to false, so an archive written
 * before the feature existed stays readable without a migration, as the manifest rule requires.
 */
@Serializable
data class TravelArchiveManifest(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("travel_id") val travelId: String,
    @SerialName("travel_name") val travelName: String = "",
    @SerialName("app_version") val appVersion: String = "",
    @SerialName("exported_at_epoch_millis") val exportedAtEpochMillis: Long = 0L,
    @SerialName("plan_entry") val planEntry: String = TravelArchiveFormat.PLAN_ENTRY,
    @SerialName("attachments") val attachments: List<String> = emptyList(),
    @SerialName("has_secrets") val hasSecrets: Boolean = false,
)
