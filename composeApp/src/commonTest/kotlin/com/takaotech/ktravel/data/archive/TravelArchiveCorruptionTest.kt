package com.takaotech.ktravel.data.archive

import com.takaotech.ktravel.data.archive.ArchiveTestFixtures.withAttachmentPath
import com.takaotech.ktravel.data.archive.ArchiveTestFixtures.withoutAttachments
import com.takaotech.ktravel.data.archive.zip.createZipArchiveFactory
import com.takaotech.ktravel.data.datasource.AttachmentDataSourceImpl
import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSourceImpl
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.storage.DatabaseProvider
import com.takaotech.ktravel.domain.archive.ImportConflictStrategy
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.archive.TravelArchiveException
import com.takaotech.ktravel.testutil.tempdir
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.toKotlinxIoPath
import io.github.vinceglb.filekit.write
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json

/**
 * Every malformed archive must produce a typed error and leave no trace on disk: these cases are
 * the reason the import is split into `stage` and `import`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TravelArchiveCorruptionTest :
    BehaviorSpec({

        val tempDir = tempdir("archive-corruption")
        val zipFactory = createZipArchiveFactory()
        val json = Json { encodeDefaults = true }

        val root = (tempDir / "installation").also { it.createDirectories() }
        val stagingRoot = root / "staging"
        val storage = TravelPlanStorageDataSourceImpl(
            DatabaseProvider(
                databaseName = "corruption-test",
                directory = root.path,
                scope = TestScope(UnconfinedTestDispatcher()),
            ),
            AttachmentDataSourceImpl(root / "attachments"),
        )
        val importer = TravelArchiveImporterImpl(
            storage = storage,
            attachments = AttachmentDataSourceImpl(root / "attachments"),
            zipFactory = zipFactory,
            stagingRoot = stagingRoot,
        )

        /** Hand-builds an archive with an arbitrary manifest and plan. */
        fun buildArchive(
            name: String,
            manifest: String?,
            plan: String?,
            extraEntries: Map<String, ByteArray> = emptyMap(),
        ): PlatformFile {
            val file = tempDir / name
            zipFactory.writer(file.toKotlinxIoPath()).use { writer ->
                manifest?.let {
                    writer.writeEntry(TravelArchiveFormat.MANIFEST_ENTRY, it.encodeToByteArray())
                }
                plan?.let {
                    writer.writeEntry(
                        TravelArchiveFormat.PLAN_ENTRY,
                        it.encodeToByteArray(),
                    )
                }
                extraEntries.forEach { (path, bytes) -> writer.writeEntry(path, bytes) }
            }
            return file
        }

        fun manifestJson(
            schemaVersion: Int = TravelArchiveFormat.CURRENT_SCHEMA_VERSION,
            travelId: String = "t1",
            hasSecrets: Boolean = false,
        ): String = json.encodeToString(
            TravelArchiveManifest.serializer(),
            TravelArchiveManifest(
                schemaVersion = schemaVersion,
                travelId = travelId,
                travelName = "Tokyo",
                hasSecrets = hasSecrets,
            ),
        )

        fun planJson(plan: TravelPlanEntity = ArchiveTestFixtures.plan().withoutAttachments()): String =
            json.encodeToString(TravelPlanEntity.serializer(), plan)

        suspend fun stageError(file: PlatformFile): TravelArchiveError {
            val exception = importer.stage(file).exceptionOrNull()
            exception.shouldBeInstanceOf<TravelArchiveException>()
            return exception.error
        }

        given("a file that is not a zip archive") {
            val file =
                (tempDir / "garbage.ktravel").also { it.write(ByteArray(256) { i -> i.toByte() }) }

            `when`("it is staged") {
                then("it is reported as a corrupted archive") {
                    stageError(file).shouldBeInstanceOf<TravelArchiveError.CorruptedArchive>()
                }

                then("no staging leftover is kept") {
                    importer.stage(file)
                    stagingRoot.list().shouldBeEmpty()
                }
            }
        }

        given("an archive without a manifest") {
            val file = buildArchive("no-manifest.ktravel", manifest = null, plan = planJson())

            `when`("it is staged") {
                then("the missing manifest entry is reported") {
                    stageError(file) shouldBe
                        TravelArchiveError.MissingEntry(TravelArchiveFormat.MANIFEST_ENTRY)
                }
            }
        }

        given("an archive without the plan entry") {
            val file = buildArchive("no-plan.ktravel", manifest = manifestJson(), plan = null)

            `when`("it is staged") {
                then("the missing plan entry is reported") {
                    stageError(file) shouldBe
                        TravelArchiveError.MissingEntry(TravelArchiveFormat.PLAN_ENTRY)
                }
            }
        }

        given("an archive whose manifest is not valid json") {
            val file =
                buildArchive("bad-manifest.ktravel", manifest = "not json", plan = planJson())

            `when`("it is staged") {
                then("the manifest is reported as invalid") {
                    stageError(file).shouldBeInstanceOf<TravelArchiveError.InvalidManifest>()
                }
            }
        }

        given("an archive whose manifest has no schema version") {
            val file = buildArchive(
                "no-version.ktravel",
                manifest = """{"travel_id":"t1","travel_name":"Tokyo"}""",
                plan = planJson(),
            )

            `when`("it is staged") {
                then("the manifest is reported as invalid") {
                    stageError(file).shouldBeInstanceOf<TravelArchiveError.InvalidManifest>()
                }
            }
        }

        given("an archive produced by an older, unsupported app version") {
            val file = buildArchive(
                "too-old.ktravel",
                manifest = manifestJson(schemaVersion = 0),
                plan = planJson(),
            )

            `when`("it is staged") {
                then("it is rejected as unsupported") {
                    stageError(file) shouldBe TravelArchiveError.UnsupportedSchemaVersion(
                        found = 0,
                        minSupported = TravelArchiveFormat.MIN_SUPPORTED_SCHEMA_VERSION,
                    )
                }
            }
        }

        given("an archive produced by a newer app version") {
            val file = buildArchive(
                "too-new.ktravel",
                manifest = manifestJson(schemaVersion = 99),
                plan = planJson(),
            )

            `when`("it is staged") {
                then("it is rejected as a future version") {
                    stageError(file) shouldBe TravelArchiveError.FutureSchemaVersion(
                        found = 99,
                        current = TravelArchiveFormat.CURRENT_SCHEMA_VERSION,
                    )
                }
            }
        }

        given("an archive whose plan does not match the current schema") {
            val file = buildArchive(
                "bad-plan.ktravel",
                manifest = manifestJson(),
                plan = """{"name":"Tokyo"}""",
            )

            `when`("it is staged") {
                then("the plan is reported as malformed") {
                    stageError(file).shouldBeInstanceOf<TravelArchiveError.MalformedPlanJson>()
                }
            }
        }

        given("an archive whose plan entry is not a json object") {
            val file =
                buildArchive("array-plan.ktravel", manifest = manifestJson(), plan = "[1,2,3]")

            `when`("it is staged") {
                then("the plan is reported as malformed") {
                    stageError(file).shouldBeInstanceOf<TravelArchiveError.MalformedPlanJson>()
                }
            }
        }

        given("an archive referencing an attachment it does not contain") {
            val file = buildArchive(
                "missing-attachment.ktravel",
                manifest = manifestJson(),
                plan = planJson(ArchiveTestFixtures.plan()),
            )

            `when`("it is staged") {
                then("the missing attachment is reported") {
                    stageError(file) shouldBe
                        TravelArchiveError.MissingAttachment(ArchiveTestFixtures.PHOTO_PATH)
                }
            }
        }

        given("an archive whose attachment path escapes the attachments root") {
            val evilPath = "../../evil.txt"
            val file = buildArchive(
                "zip-slip.ktravel",
                manifest = manifestJson(),
                plan = planJson(ArchiveTestFixtures.plan().withAttachmentPath(evilPath)),
                extraEntries = mapOf(
                    TravelArchiveFormat.attachmentEntry(evilPath) to byteArrayOf(1, 2, 3),
                ),
            )

            `when`("it is staged") {
                then("it is rejected before anything is written to disk") {
                    val error = stageError(file)
                    error.shouldBeInstanceOf<TravelArchiveError.CorruptedArchive>()
                    error.reason.contains("unsafe attachment path") shouldBe true
                }
            }
        }

        given("an archive claiming secrets it does not carry") {
            val file = buildArchive(
                "missing-secrets.ktravel",
                manifest = manifestJson(hasSecrets = true),
                plan = planJson(),
            )

            `when`("it is staged") {
                then("the missing entry is reported instead of prompting for a password") {
                    stageError(file) shouldBe
                        TravelArchiveError.MissingEntry(TravelArchiveFormat.SECRETS_ENTRY)
                }
            }
        }

        given("an archive whose secrets entry is not valid json") {
            val file = buildArchive(
                "bad-secrets.ktravel",
                manifest = manifestJson(hasSecrets = true),
                plan = planJson(),
                extraEntries = mapOf(
                    TravelArchiveFormat.SECRETS_ENTRY to "not json".encodeToByteArray(),
                ),
            )

            `when`("it is staged and a password is supplied") {
                then("the archive is reported as corrupted, not as a wrong password") {
                    val staged = importer.stage(file).getOrThrow()
                    staged.hasSecrets shouldBe true

                    val failure = importer
                        .import(staged, ImportConflictStrategy.DUPLICATE, secretsPassword = "pw")
                        .exceptionOrNull()
                    failure.shouldBeInstanceOf<TravelArchiveException>()
                    failure.error.shouldBeInstanceOf<TravelArchiveError.CorruptedArchive>()
                }
            }
        }
    })
