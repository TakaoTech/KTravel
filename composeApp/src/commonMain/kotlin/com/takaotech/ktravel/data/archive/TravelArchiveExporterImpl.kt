@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.takaotech.ktravel.data.archive

import co.touchlab.kermit.Logger
import com.takaotech.ktravel.core.KTravelBuildInfo
import com.takaotech.ktravel.core.io.deleteRecursively
import com.takaotech.ktravel.data.archive.crypto.ArchiveSecretsCipher
import com.takaotech.ktravel.data.archive.crypto.ArchiveSecretsEnvelope
import com.takaotech.ktravel.data.archive.crypto.ArchiveSecretsPayload
import com.takaotech.ktravel.data.archive.zip.ZipArchiveFactory
import com.takaotech.ktravel.data.datasource.AttachmentDataSource
import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSource
import com.takaotech.ktravel.data.entity.AttachmentEntity
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.archive.TravelArchiveException
import com.takaotech.ktravel.domain.archive.TravelArchiveExportResult
import com.takaotech.ktravel.domain.archive.TravelArchiveExporter
import com.takaotech.ktravel.domain.archive.asTravelArchiveException
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.toKotlinxIoPath
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * Writes a plan and the files it references into a `.ktravel` archive.
 *
 * The archive is built in a staging directory and copied to the destination only once it is
 * complete, so a failure halfway through leaves the file the user picked untouched. That copy is
 * also the one point where the real filesystem is left behind: on Android the destination may be a
 * `content://` uri that only FileKit knows how to write to.
 *
 * What lands in the archive is not quite the stored plan. It lists only the attachments whose file
 * was actually found, so the archive is never self-inconsistent with itself, and its HERE key is
 * stripped unconditionally: the key leaves the device only inside `secrets.json`, encrypted under a
 * password the user chose, and only when they asked for it.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class TravelArchiveExporterImpl private constructor(
    private val storage: TravelPlanStorageDataSource,
    private val attachments: AttachmentDataSource,
    private val zipFactory: ZipArchiveFactory,
    private val appVersion: String,
    private val clock: Clock,
    // Injectable staging: the app cache dir in production, a tempdir in tests.
    private val stagingRootProvider: () -> PlatformFile,
) : TravelArchiveExporter {

    private val logger = Logger.withTag("TravelArchiveExporter")

    @Inject
    constructor(
        storage: TravelPlanStorageDataSource,
        attachments: AttachmentDataSource,
        zipFactory: ZipArchiveFactory,
    ) : this(
        storage = storage,
        attachments = attachments,
        zipFactory = zipFactory,
        appVersion = KTravelBuildInfo.VERSION,
        clock = Clock.System,
        stagingRootProvider = { FileKit.cacheDir / STAGING_DIR },
    )

    /** Constructor for tests: explicit staging and a deterministic clock. */
    internal constructor(
        storage: TravelPlanStorageDataSource,
        attachments: AttachmentDataSource,
        zipFactory: ZipArchiveFactory,
        stagingRoot: PlatformFile,
        appVersion: String = "test",
        clock: Clock = Clock.System,
    ) : this(storage, attachments, zipFactory, appVersion, clock, { stagingRoot })

    private val json = Json {
        prettyPrint = false
        encodeDefaults = true
    }
    private val stagingArea = ArchiveStagingArea(stagingRootProvider)

    override suspend fun export(
        travelId: String,
        destination: PlatformFile,
        secretsPassword: String?,
    ): Result<TravelArchiveExportResult> = withContext(Dispatchers.IO) {
        val stagingDir = stagingArea.newSession()
        try {
            val plan = readPlan(travelId)
            val (present, skipped) = plan.allAttachments().partition { attachment ->
                attachments.resolveFile(attachment.relativePath).exists()
            }

            val stagingArchive = stagingDir / "archive.${TravelArchiveFormat.FILE_EXTENSION}"

            // Sealing happens before the zip is opened so a failure here leaves nothing behind.
            val secrets = sealSecrets(plan, secretsPassword)

            // The plan written into the archive lists only the attachments actually included:
            // otherwise the archive would be self-inconsistent and the import would reject it.
            writeArchive(stagingArchive, travelId, plan.retainingOnly(present), present, secrets)
            // The one point where we leave the real filesystem: `destination` may be an Android
            // content://, which FileKit handles by streaming.
            stagingArchive.copyTo(destination)

            Result.success(
                TravelArchiveExportResult(
                    travelId = travelId,
                    travelName = plan.name,
                    attachmentCount = present.size,
                    skippedAttachments = skipped.map { it.relativePath },
                ),
            )
        } catch (cancellation: CancellationException) {
            logger.i { "Export cancelled" }
            throw cancellation
        } catch (throwable: Throwable) {
            Result.failure(throwable.asTravelArchiveException())
        } finally {
            logger.i { "Start cancel staging area" }
            runCatching { stagingDir.deleteRecursively() }
                .onFailure {
                    logger.e(it) { "Failed to delete staging area" }
                }
        }
    }

    private fun readPlan(travelId: String): TravelPlanEntity =
        runCatching { storage.getTravelPlan(travelId).copy(id = travelId) }
            .getOrElse { throwable ->
                throw TravelArchiveException(
                    TravelArchiveError.Io("Travel plan $travelId is not readable: ${throwable.message}"),
                )
            }

    /**
     * Encrypts the plan's API key, or returns null when there is nothing to protect or the user did
     * not ask for it. A password with no key configured is not an error: there is simply no secret.
     */
    private suspend fun sealSecrets(plan: TravelPlanEntity, password: String?): ArchiveSecretsEnvelope? {
        if (password.isNullOrEmpty() || plan.settings.hereApiKey.isEmpty()) return null
        return ArchiveSecretsCipher.seal(ArchiveSecretsPayload(plan.settings.hereApiKey), password)
    }

    private fun writeArchive(
        archive: PlatformFile,
        travelId: String,
        plan: TravelPlanEntity,
        attachmentsToWrite: List<AttachmentEntity>,
        secrets: ArchiveSecretsEnvelope?,
    ) {
        val manifest = TravelArchiveManifest(
            schemaVersion = TravelArchiveFormat.CURRENT_SCHEMA_VERSION,
            travelId = travelId,
            travelName = plan.name,
            appVersion = appVersion,
            exportedAtEpochMillis = clock.now().toEpochMilliseconds(),
            planEntry = TravelArchiveFormat.PLAN_ENTRY,
            attachments = attachmentsToWrite.map {
                TravelArchiveFormat.attachmentEntry(it.relativePath)
            },
            hasSecrets = secrets != null,
        )

        zipFactory.writer(archive.toKotlinxIoPath()).use { writer ->
            writer.writeEntry(
                TravelArchiveFormat.MANIFEST_ENTRY,
                json.encodeToString(TravelArchiveManifest.serializer(), manifest)
                    .encodeToByteArray(),
            )
            writer.writeEntry(
                TravelArchiveFormat.PLAN_ENTRY,
                // Only the secret field is stripped, not the whole settings object: the plan's
                // other preferences belong to the plan and travel with it. The key is stripped
                // unconditionally, because it only ever leaves the device encrypted.
                json.encodeToString(
                    TravelPlanEntity.serializer(),
                    plan.copy(settings = plan.settings.copy(hereApiKey = "")),
                ).encodeToByteArray(),
            )
            secrets?.let { envelope ->
                writer.writeEntry(
                    TravelArchiveFormat.SECRETS_ENTRY,
                    json.encodeToString(ArchiveSecretsEnvelope.serializer(), envelope)
                        .encodeToByteArray(),
                )
            }
            attachmentsToWrite.forEach { attachment ->
                writer.writeEntry(
                    TravelArchiveFormat.attachmentEntry(attachment.relativePath),
                    attachments.resolveFile(attachment.relativePath).toKotlinxIoPath(),
                )
            }
        }
    }

    /** Shared with the importer, which stages under the same root. */
    companion object {
        /** Directory under the cache root that export and import both work in. */
        const val STAGING_DIR: String = "archive-staging"
    }
}
