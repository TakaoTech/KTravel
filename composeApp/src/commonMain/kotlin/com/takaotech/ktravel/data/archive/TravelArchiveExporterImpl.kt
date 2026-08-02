@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.takaotech.ktravel.data.archive

import com.takaotech.ktravel.core.KTravelBuildInfo
import com.takaotech.ktravel.core.io.deleteRecursively
import com.takaotech.ktravel.data.archive.zip.ZipArchiveFactory
import com.takaotech.ktravel.data.datasource.AttachmentDataSource
import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSource
import com.takaotech.ktravel.data.entity.AttachmentEntity
import com.takaotech.ktravel.data.entity.StepEntity
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

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class TravelArchiveExporterImpl private constructor(
    private val storage: TravelPlanStorageDataSource,
    private val attachments: AttachmentDataSource,
    private val zipFactory: ZipArchiveFactory,
    private val appVersion: String,
    private val clock: Clock,
    // Staging iniettabile: in produzione la cache dir dell'app, nei test una tempdir.
    private val stagingRootProvider: () -> PlatformFile
) : TravelArchiveExporter {

    @Inject
    constructor(
        storage: TravelPlanStorageDataSource,
        attachments: AttachmentDataSource,
        zipFactory: ZipArchiveFactory
    ) : this(
        storage = storage,
        attachments = attachments,
        zipFactory = zipFactory,
        appVersion = KTravelBuildInfo.VERSION,
        clock = Clock.System,
        stagingRootProvider = { FileKit.cacheDir / STAGING_DIR }
    )

    /** Costruttore per i test: staging esplicito e clock deterministico. */
    internal constructor(
        storage: TravelPlanStorageDataSource,
        attachments: AttachmentDataSource,
        zipFactory: ZipArchiveFactory,
        stagingRoot: PlatformFile,
        appVersion: String = "test",
        clock: Clock = Clock.System
    ) : this(storage, attachments, zipFactory, appVersion, clock, { stagingRoot })

    private val json = Json { prettyPrint = false; encodeDefaults = true }
    private val stagingArea = ArchiveStagingArea(stagingRootProvider)

    override suspend fun export(
        travelId: String,
        destination: PlatformFile
    ): Result<TravelArchiveExportResult> = withContext(Dispatchers.IO) {
        val stagingDir = stagingArea.newSession()
        try {
            val plan = readPlan(travelId)
            val (present, skipped) = plan.allAttachments().partition { attachment ->
                attachments.resolveFile(attachment.relativePath).exists()
            }

            val stagingArchive = stagingDir / "archive.${TravelArchiveFormat.FILE_EXTENSION}"

            // Il piano scritto nell'archivio elenca solo gli allegati effettivamente inclusi:
            // altrimenti l'archivio sarebbe auto-incoerente e l'import lo rifiuterebbe.
            writeArchive(stagingArchive, travelId, plan.retainingOnly(present), present)
            // Unico punto in cui si esce dal filesystem reale: `destination` può essere un
            // content:// Android, che FileKit gestisce in streaming.
            stagingArchive.copyTo(destination)

            Result.success(
                TravelArchiveExportResult(
                    travelId = travelId,
                    travelName = plan.name,
                    attachmentCount = present.size,
                    skippedAttachments = skipped.map { it.relativePath }
                )
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Result.failure(throwable.asTravelArchiveException())
        } finally {
            runCatching { stagingDir.deleteRecursively() }
        }
    }

    private fun readPlan(travelId: String): TravelPlanEntity =
        runCatching { storage.getTravelPlan(travelId).copy(id = travelId) }
            .getOrElse { throwable ->
                throw TravelArchiveException(
                    TravelArchiveError.Io("Travel plan $travelId is not readable: ${throwable.message}")
                )
            }

    private fun writeArchive(
        archive: PlatformFile,
        travelId: String,
        plan: TravelPlanEntity,
        attachmentsToWrite: List<AttachmentEntity>
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
            }
        )

        zipFactory.writer(archive.toKotlinxIoPath()).use { writer ->
            writer.writeEntry(
                TravelArchiveFormat.MANIFEST_ENTRY,
                json.encodeToString(TravelArchiveManifest.serializer(), manifest)
                    .encodeToByteArray()
            )
            writer.writeEntry(
                TravelArchiveFormat.PLAN_ENTRY,
                json.encodeToString(TravelPlanEntity.serializer(), plan).encodeToByteArray()
            )
            attachmentsToWrite.forEach { attachment ->
                writer.writeEntry(
                    TravelArchiveFormat.attachmentEntry(attachment.relativePath),
                    attachments.resolveFile(attachment.relativePath).toKotlinxIoPath()
                )
            }
        }
    }

    companion object {
        const val STAGING_DIR: String = "archive-staging"
    }
}

/** Tutti gli allegati referenziati dal piano, in ordine di comparsa. */
internal fun TravelPlanEntity.allAttachments(): List<AttachmentEntity> =
    days.flatMap { day -> day.steps }
        .filterIsInstance<StepEntity.Place>()
        .flatMap { step -> step.attachments }

/**
 * Copia del piano il cui inventario contiene solo [retained]. I riferimenti nelle note non vengono
 * toccati: restano dangling esattamente come lo erano già prima dell'export.
 */
private fun TravelPlanEntity.retainingOnly(retained: List<AttachmentEntity>): TravelPlanEntity {
    val keep = retained.map { it.relativePath }.toSet()
    return copy(
        days = days.map { day ->
            day.copy(
                steps = day.steps.map { step ->
                    when (step) {
                        is StepEntity.Transport -> step
                        is StepEntity.Place -> step.copy(
                            attachments = step.attachments.filter { it.relativePath in keep }
                        )
                    }
                }
            )
        }
    )
}
