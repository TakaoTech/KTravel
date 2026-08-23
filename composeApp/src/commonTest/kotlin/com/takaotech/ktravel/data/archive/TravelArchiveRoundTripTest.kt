package com.takaotech.ktravel.data.archive

import com.takaotech.ktravel.data.archive.zip.createZipArchiveFactory
import com.takaotech.ktravel.data.datasource.AttachmentDataSourceImpl
import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSourceImpl
import com.takaotech.ktravel.data.entity.StepEntity
import com.takaotech.ktravel.data.entity.TransitStepEntity
import com.takaotech.ktravel.data.entity.TransportAnswerEntity
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.storage.DatabaseProvider
import com.takaotech.ktravel.domain.archive.ImportConflictStrategy
import com.takaotech.ktravel.domain.model.AttachmentReference
import com.takaotech.ktravel.testutil.tempdir
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.toKotlinxIoPath
import io.github.vinceglb.filekit.write
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Esercita l'intero ciclo export -> import fra due installazioni simulate (due database e due
 * inventari file distinti), che è la ragione d'essere della feature.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TravelArchiveRoundTripTest :
    BehaviorSpec({

        val tempDir = tempdir("archive-round-trip")
        val zipFactory = createZipArchiveFactory()

        /** Una "installazione": database, inventario allegati e le due classi di archivio. */
        class Installation(root: PlatformFile, name: String) {
            val attachmentRoot = root / "attachments"
            val databaseProvider = DatabaseProvider(
                databaseName = name,
                directory = root.also { it.createDirectories() }.path,
                scope = TestScope(UnconfinedTestDispatcher()),
            )
            val attachments = AttachmentDataSourceImpl(attachmentRoot)
            val storage = TravelPlanStorageDataSourceImpl(databaseProvider, attachments)
            val exporter = TravelArchiveExporterImpl(
                storage = storage,
                attachments = attachments,
                zipFactory = zipFactory,
                stagingRoot = root / "staging",
            )
            var nextId = 0
            val importer = TravelArchiveImporterImpl(
                storage = storage,
                attachments = attachments,
                zipFactory = zipFactory,
                stagingRoot = root / "staging",
                newId = { "imported-${++nextId}" },
            )
        }

        suspend fun Installation.seedPlanWithFiles(plan: TravelPlanEntity) {
            storage.insertTravelPlan(plan)
            (attachmentRoot / plan.id / "step-1").createDirectories()
            attachments.resolveFile(ArchiveTestFixtures.PHOTO_PATH)
                .write(ArchiveTestFixtures.PHOTO_BYTES)
            attachments.resolveFile(ArchiveTestFixtures.DOC_PATH)
                .write(ArchiveTestFixtures.DOC_BYTES)
        }

        given("a travel plan with attachments exported from one installation") {
            val source = Installation(tempDir / "source", "round-trip-source")
            val target = Installation(tempDir / "target", "round-trip-target")
            val plan = ArchiveTestFixtures.plan()
            source.seedPlanWithFiles(plan)

            val archiveFile = tempDir / "tokyo.ktravel"
            val exportResult = source.exporter.export(ArchiveTestFixtures.TRAVEL_ID, archiveFile)

            `when`("the export completes") {
                then("it reports the exported travel and no skipped attachment") {
                    val result = exportResult.getOrThrow()
                    result.travelName shouldBe "Tokyo"
                    result.attachmentCount shouldBe 2
                    result.skippedAttachments shouldBe emptyList()
                }

                then("the archive contains the manifest, the plan and one entry per attachment") {
                    zipFactory.reader(archiveFile.toKotlinxIoPath()).use { reader ->
                        reader.entryPaths() shouldContainExactlyInAnyOrder listOf(
                            TravelArchiveFormat.MANIFEST_ENTRY,
                            TravelArchiveFormat.PLAN_ENTRY,
                            TravelArchiveFormat.attachmentEntry(ArchiveTestFixtures.PHOTO_PATH),
                            TravelArchiveFormat.attachmentEntry(ArchiveTestFixtures.DOC_PATH),
                        )
                    }
                }

                then("no staging leftover is kept") {
                    (tempDir / "source" / "staging").list().shouldBeEmpty()
                }
            }

            `when`("the archive is staged on another installation") {
                val staged = target.importer.stage(archiveFile).getOrThrow()

                then("the manifest metadata is exposed") {
                    staged.travelId shouldBe ArchiveTestFixtures.TRAVEL_ID
                    staged.travelName shouldBe "Tokyo"
                    staged.schemaVersion shouldBe TravelArchiveFormat.CURRENT_SCHEMA_VERSION
                    staged.attachmentCount shouldBe 2
                }

                then("no conflict is reported because the target has no such plan") {
                    staged.conflictingTravelName shouldBe null
                }

                then("discarding it releases the staging directory") {
                    target.importer.discard(staged)
                    (tempDir / "target" / "staging").list().shouldBeEmpty()
                }
            }

            `when`("the archive is imported into the other installation") {
                val staged = target.importer.stage(archiveFile).getOrThrow()
                val summary =
                    target.importer.import(staged, ImportConflictStrategy.DUPLICATE).getOrThrow()
                val imported = target.storage.getTravelPlan(summary.id).copy(id = summary.id)

                then("the plan is stored with the same content") {
                    imported.name shouldBe plan.name
                    imported.periodStart shouldBe plan.periodStart
                    imported.periodEnd shouldBe plan.periodEnd
                    imported.days.size shouldBe plan.days.size
                    imported.places.map { it.name } shouldBe plan.places.map { it.name }
                }

                then("the transport step survives with its journey, line and stops") {
                    val transport =
                        imported.days.first().steps.filterIsInstance<StepEntity.Transport>()
                    val journey = transport.first().answer
                        .shouldBeInstanceOf<TransportAnswerEntity.Transit>().journey
                    val ride = journey.steps.first().shouldBeInstanceOf<TransitStepEntity.Ride>()

                    ride.summary.durationSeconds shouldBe 1800
                    ride.line.name shouldBe "R12"
                    ride.line.color shouldBe "#00A03E"
                    ride.agency?.name shouldBe "Trenord"
                    ride.boarding?.name shouldBe "Milano Cadorna"
                    ride.alighting?.name shouldBe "Varese"
                }

                then("every attachment binary is restored byte for byte") {
                    target.attachments.resolveFile(ArchiveTestFixtures.PHOTO_PATH)
                        .readBytes() shouldBe ArchiveTestFixtures.PHOTO_BYTES
                    target.attachments.resolveFile(ArchiveTestFixtures.DOC_PATH)
                        .readBytes() shouldBe ArchiveTestFixtures.DOC_BYTES
                }

                then("no note reference is dangling") {
                    val step =
                        imported.days.first().steps.filterIsInstance<StepEntity.Place>().first()
                    AttachmentReference.missingReferences(
                        step.note,
                        step.attachments.map { it.relativePath },
                    ) shouldBe emptyList()
                }

                then("the staging directory is released") {
                    (tempDir / "target" / "staging").list().shouldBeEmpty()
                }
            }
        }

        given("an archive imported twice into the same installation") {
            val installation = Installation(tempDir / "twice", "round-trip-twice")
            val plan = ArchiveTestFixtures.plan()
            installation.seedPlanWithFiles(plan)

            val archiveFile = tempDir / "twice.ktravel"
            installation.exporter.export(ArchiveTestFixtures.TRAVEL_ID, archiveFile).getOrThrow()

            `when`("the archive is staged again where the plan already exists") {
                val staged = installation.importer.stage(archiveFile).getOrThrow()

                then("the conflicting plan name is reported") {
                    staged.conflictingTravelName shouldBe "Tokyo"
                }
            }

            `when`("the duplicate strategy is applied") {
                val staged = installation.importer.stage(archiveFile).getOrThrow()
                val summary = installation.importer
                    .import(staged, ImportConflictStrategy.DUPLICATE, nameOverride = "Tokyo (copy)")
                    .getOrThrow()

                then("a second plan is created with a new id and the overridden name") {
                    summary.id shouldNotBe ArchiveTestFixtures.TRAVEL_ID
                    summary.name shouldBe "Tokyo (copy)"
                    installation.storage.getAllTravelPlans()
                        .map { it.id } shouldContainExactlyInAnyOrder
                        listOf(ArchiveTestFixtures.TRAVEL_ID, summary.id)
                }

                then("the copy has its own attachment files, and the original still has its own") {
                    val copy = installation.storage.getTravelPlan(summary.id)
                    val copiedStep =
                        copy.days.first().steps.filterIsInstance<StepEntity.Place>().first()
                    copiedStep.attachments.forEach { attachment ->
                        installation.attachments.resolveFile(attachment.relativePath)
                            .exists() shouldBe true
                    }
                    installation.attachments.resolveFile(ArchiveTestFixtures.PHOTO_PATH)
                        .readBytes() shouldBe ArchiveTestFixtures.PHOTO_BYTES
                }
            }

            `when`("the replace strategy is applied") {
                val staged = installation.importer.stage(archiveFile).getOrThrow()
                val summary = installation.importer
                    .import(staged, ImportConflictStrategy.REPLACE)
                    .getOrThrow()

                then("the existing plan is overwritten instead of duplicated") {
                    summary.id shouldBe ArchiveTestFixtures.TRAVEL_ID
                    installation.storage.getAllTravelPlans()
                        .count { it.id == ArchiveTestFixtures.TRAVEL_ID } shouldBe 1
                }

                then("its attachments are restored") {
                    installation.attachments.resolveFile(ArchiveTestFixtures.PHOTO_PATH)
                        .readBytes() shouldBe ArchiveTestFixtures.PHOTO_BYTES
                }
            }
        }

        given("a plan referencing an attachment missing from disk") {
            val installation = Installation(tempDir / "missing-file", "round-trip-missing")
            val plan = ArchiveTestFixtures.plan()
            installation.storage.insertTravelPlan(plan)
            (installation.attachmentRoot / plan.id / "step-1").createDirectories()
            installation.attachments.resolveFile(ArchiveTestFixtures.PHOTO_PATH)
                .write(ArchiveTestFixtures.PHOTO_BYTES)

            `when`("the plan is exported") {
                val archiveFile = tempDir / "partial.ktravel"
                val result = installation.exporter
                    .export(ArchiveTestFixtures.TRAVEL_ID, archiveFile)
                    .getOrThrow()

                then("the export succeeds reporting the skipped attachment") {
                    result.attachmentCount shouldBe 1
                    result.skippedAttachments shouldBe listOf(ArchiveTestFixtures.DOC_PATH)
                }

                then("the archive stays self-consistent and can still be imported") {
                    val target = Installation(tempDir / "missing-file-target", "round-trip-partial")
                    val staged = target.importer.stage(archiveFile).getOrThrow()
                    staged.attachmentCount shouldBe 1

                    val summary = target.importer
                        .import(staged, ImportConflictStrategy.DUPLICATE)
                        .getOrThrow()
                    target.attachments.resolveFile(ArchiveTestFixtures.PHOTO_PATH)
                        .readBytes() shouldBe ArchiveTestFixtures.PHOTO_BYTES
                    target.storage.getTravelPlan(summary.id).name shouldBe "Tokyo"
                }
            }
        }
    })
