package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.data.entity.AppSettingsEntity
import com.takaotech.ktravel.data.storage.DatabaseProvider
import com.takaotech.ktravel.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotbase.MutableDocument
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * How the settings document is read and written.
 *
 * `coerceInputValues` is what makes an unreadable value fall back to the property's default instead
 * of failing the whole read: a `telemetry.consent` written by a newer build comes back as
 * [com.takaotech.ktravel.core.telemetry.TelemetryConsent.Unknown], which means the privacy page is shown
 * again rather than the preferences being lost.
 *
 * Internal rather than private so a test can check that fallback without opening a database.
 */
internal val appSettingsJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

/**
 * The installation's preferences, in their own collection.
 *
 * Separate from `travel_plans` rather than a document inside it: the export path walks that
 * collection, and a credential belonging to the machine has no business being reachable from
 * something the user hands to someone else.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AppSettingsStorageDataSourceImpl(private val databaseProvider: DatabaseProvider) :
    AppSettingsStorageDataSource {

    private val json = appSettingsJson

    private val settingsCollection = databaseProvider.database.createCollection("app_settings")

    /** Defaults when the document is absent, which is what a first launch looks like. */
    override fun getAppSettings(): AppSettingsEntity = settingsCollection.getDocument(AppSettingsEntity.DOCUMENT_ID)
        ?.let { json.decodeFromString<AppSettingsEntity>(it.toJSON()) }
        ?: AppSettingsEntity()

    /**
     * Awaited, unlike the fire and forget plan save.
     *
     * A plan is written on every edit and the screen already shows the new state; these are written
     * when the user presses save and then immediately read back to say whether the navigator answers,
     * so the write has to have happened by the time that call is made.
     */
    override suspend fun saveAppSettings(entity: AppSettingsEntity) {
        withContext(databaseProvider.writeContext) {
            val jsonString = json.encodeToString(AppSettingsEntity.serializer(), entity)
            val document = settingsCollection.getDocument(AppSettingsEntity.DOCUMENT_ID)
                ?.toMutable()
                ?.also { it.setJSON(jsonString) }
                ?: MutableDocument(AppSettingsEntity.DOCUMENT_ID, jsonString)

            settingsCollection.save(document)
        }
    }
}
