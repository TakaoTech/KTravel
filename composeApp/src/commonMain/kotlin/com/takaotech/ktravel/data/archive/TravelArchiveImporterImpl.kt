@file:OptIn(ExperimentalUuidApi::class)

package com.takaotech.ktravel.data.archive

import com.takaotech.ktravel.core.io.deleteRecursively
import com.takaotech.ktravel.data.archive.migration.TravelPlanSchemaMigrator
import com.takaotech.ktravel.data.archive.zip.ZipArchiveFactory
import com.takaotech.ktravel.data.archive.zip.ZipFormatException
import com.takaotech.ktravel.data.archive.zip.ZipReader
import com.takaotech.ktravel.data.datasource.AttachmentDataSource
import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSource
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.mapper.TravelPlanEntityMapper.toSummary
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.archive.ImportConflictStrategy
import com.takaotech.ktravel.domain.archive.StagedPlanPayload
import com.takaotech.ktravel.domain.archive.StagedTravelArchive
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.archive.TravelArchiveException
import com.takaotech.ktravel.domain.archive.TravelArchiveImporter
import com.takaotech.ktravel.domain.archive.asTravelArchiveException
import com.takaotech.ktravel.domain.model.TravelPlanSummary
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.toKotlinxIoPath
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Piano deserializzato che viaggia dentro [StagedTravelArchive]. */
internal class TravelPlanPayload(val plan: TravelPlanEntity) : StagedPlanPayload

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class TravelArchiveImporterImpl private constructor(
    private val storage: TravelPlanStorageDataSource,
    private val attachments: AttachmentDataSource,
    private val zipFactory: ZipArchiveFactory,
    private val stagingRootProvider: () -> PlatformFile,
    private val newId: () -> String,
) : TravelArchiveImporter {

    @Inject
    constructor(
        storage: TravelPlanStorageDataSource,
        attachments: AttachmentDataSource,
        zipFactory: ZipArchiveFactory,
    ) : this(
        storage = storage,
        attachments = attachments,
        zipFactory = zipFactory,
        stagingRootProvider = { FileKit.cacheDir / TravelArchiveExporterImpl.STAGING_DIR },
        newId = { Uuid.random().toString() },
    )

    /** Costruttore per i test: staging esplicito e id deterministici. */
    internal constructor(
        storage: TravelPlanStorageDataSource,
        attachments: AttachmentDataSource,
        zipFactory: ZipArchiveFactory,
        stagingRoot: PlatformFile,
        newId: () -> String = { Uuid.random().toString() },
    ) : this(storage, attachments, zipFactory, { stagingRoot }, newId)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val migrator = TravelPlanSchemaMigrator()
    private val stagingArea = ArchiveStagingArea(stagingRootProvider)

    override suspend fun stage(source: PlatformFile): Result<StagedTravelArchive> = withContext(Dispatchers.IO) {
        val stagingDir = stagingArea.newSession()
        try {
            val stagedArchive = stagingDir / "import.${TravelArchiveFormat.FILE_EXTENSION}"
            // Unico punto in cui si legge il file scelto dall'utente, che su Android può essere
            // un content:// non convertibile in un path del filesystem.
            source.copyTo(stagedArchive)

            Result.success(readStagedArchive(stagedArchive, stagingDir))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            runCatching { stagingDir.deleteRecursively() }
            Result.failure(throwable.asTravelArchiveException())
        }
    }

    override suspend fun import(
        staged: StagedTravelArchive,
        strategy: ImportConflictStrategy,
        nameOverride: String?,
    ): Result<TravelPlanSummary> = withContext(Dispatchers.IO) {
        val sourcePlan = (staged.payload as TravelPlanPayload).plan
        val duplicating = strategy == ImportConflictStrategy.DUPLICATE &&
            staged.conflictingTravelName != null

        val remapped = if (duplicating) {
            TravelArchiveIdRemapper.remap(sourcePlan, newTravelId = newId(), newId = newId)
        } else {
            TravelArchiveIdRemapper.Remapped(
                plan = sourcePlan.copy(id = staged.travelId),
                attachmentPathMapping = emptyMap(),
            )
        }
        val plan = nameOverride?.let { remapped.plan.copy(name = it) } ?: remapped.plan

        try {
            if (!duplicating && staged.conflictingTravelName != null) {
                // Sostituzione: il documento e i file del viaggio esistente vanno rimossi prima,
                // altrimenti resterebbero allegati orfani non più referenziati dal piano.
                storage.deleteTravelPlan(plan.id)
            }

            extractAttachments(staged, sourcePlan, remapped.attachmentPathMapping)
            storage.insertTravelPlan(plan)

            Result.success(plan.toSummary())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            // Compensazione: senza questa, un fallimento a metà lascerebbe file senza piano.
            runCatching { attachments.deleteTravelAttachments(plan.id) }
            runCatching { storage.deleteTravelPlan(plan.id) }
            Result.failure(throwable.asTravelArchiveException())
        } finally {
            discard(staged)
        }
    }

    override suspend fun discard(staged: StagedTravelArchive) {
        runCatching { staged.stagingDir.deleteRecursively() }
    }

    private suspend fun readStagedArchive(archive: PlatformFile, stagingDir: PlatformFile): StagedTravelArchive =
        openReader(archive).use { reader ->
            checkArchiveLimits(reader)

            val manifest = readManifest(reader)
            val plan = readPlan(reader, manifest)
            validateAttachments(plan, reader)

            StagedTravelArchive(
                travelId = manifest.travelId,
                travelName = manifest.travelName.ifEmpty { plan.name },
                schemaVersion = manifest.schemaVersion,
                exportedAtEpochMillis = manifest.exportedAtEpochMillis,
                attachmentCount = plan.allAttachments().size,
                conflictingTravelName = conflictingName(manifest.travelId),
                archive = archive,
                stagingDir = stagingDir,
                payload = TravelPlanPayload(plan),
            )
        }

    private fun openReader(archive: PlatformFile): ZipReader = try {
        zipFactory.reader(archive.toKotlinxIoPath())
    } catch (formatException: ZipFormatException) {
        throw TravelArchiveException(
            TravelArchiveError.CorruptedArchive(formatException.message.orEmpty()),
        )
    }

    private fun checkArchiveLimits(reader: ZipReader) {
        val entryCount = reader.entryPaths().size
        if (entryCount > TravelArchiveFormat.MAX_ENTRIES) {
            throw TravelArchiveException(
                TravelArchiveError.CorruptedArchive("too many entries: $entryCount"),
            )
        }
        val size = reader.totalUncompressedSize()
        if (size > TravelArchiveFormat.MAX_UNCOMPRESSED_BYTES) {
            throw TravelArchiveException(
                TravelArchiveError.CorruptedArchive("uncompressed size too large: $size bytes"),
            )
        }
    }

    private fun readManifest(reader: ZipReader): TravelArchiveManifest {
        val bytes = reader.readBytes(TravelArchiveFormat.MANIFEST_ENTRY)
            ?: throw TravelArchiveException(
                TravelArchiveError.MissingEntry(TravelArchiveFormat.MANIFEST_ENTRY),
            )
        return runCatching {
            json.decodeFromString(TravelArchiveManifest.serializer(), bytes.decodeToString())
        }.getOrElse { throwable ->
            throw TravelArchiveException(
                TravelArchiveError.InvalidManifest(
                    throwable.message ?: throwable::class.simpleName.orEmpty(),
                ),
            )
        }
    }

    private fun readPlan(reader: ZipReader, manifest: TravelArchiveManifest): TravelPlanEntity {
        val bytes = reader.readBytes(manifest.planEntry)
            ?: throw TravelArchiveException(TravelArchiveError.MissingEntry(manifest.planEntry))

        val element = runCatching { json.parseToJsonElement(bytes.decodeToString()) as? JsonObject }
            .getOrNull()
            ?: throw TravelArchiveException(
                TravelArchiveError.MalformedPlanJson("plan entry is not a json object"),
            )

        val migrated = migrator.migrate(element, manifest.schemaVersion)

        return runCatching {
            json.decodeFromJsonElement(TravelPlanEntity.serializer(), migrated)
        }.getOrElse { throwable ->
            throw TravelArchiveException(
                TravelArchiveError.MalformedPlanJson(
                    throwable.message ?: throwable::class.simpleName.orEmpty(),
                ),
            )
        }
    }

    private fun validateAttachments(plan: TravelPlanEntity, reader: ZipReader) {
        val entries = reader.entryPaths()
        plan.allAttachments().forEach { attachment ->
            if (!TravelArchiveValidation.isSafeRelativePath(attachment.relativePath)) {
                throw TravelArchiveException(
                    TravelArchiveError.CorruptedArchive(
                        "unsafe attachment path: ${attachment.relativePath}",
                    ),
                )
            }
            if (TravelArchiveFormat.attachmentEntry(attachment.relativePath) !in entries) {
                throw TravelArchiveException(
                    TravelArchiveError.MissingAttachment(attachment.relativePath),
                )
            }
        }
    }

    private suspend fun conflictingName(travelId: String): String? = storage.getTravelPlanNameOrNull(travelId)

    private suspend fun extractAttachments(
        staged: StagedTravelArchive,
        sourcePlan: TravelPlanEntity,
        pathMapping: Map<String, String>,
    ) {
        val sourceAttachments = sourcePlan.allAttachments()
        if (sourceAttachments.isEmpty()) return

        openReader(staged.archive).use { reader ->
            sourceAttachments.forEach { attachment ->
                val oldPath = attachment.relativePath
                val newPath = pathMapping[oldPath] ?: oldPath
                val target = attachments.resolveFile(newPath)
                (attachments.resolveFile(newPath.substringBeforeLast('/'))).createDirectories()

                val extracted = reader.extractTo(
                    TravelArchiveFormat.attachmentEntry(oldPath),
                    target.toKotlinxIoPath(),
                )
                if (!extracted) {
                    throw TravelArchiveException(TravelArchiveError.MissingAttachment(oldPath))
                }
            }
        }
    }
}
