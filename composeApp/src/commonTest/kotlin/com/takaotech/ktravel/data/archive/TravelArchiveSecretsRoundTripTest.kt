package com.takaotech.ktravel.data.archive

import com.takaotech.ktravel.data.archive.zip.createZipArchiveFactory
import com.takaotech.ktravel.data.datasource.AttachmentDataSourceImpl
import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSourceImpl
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.entity.TravelSettingsEntity
import com.takaotech.ktravel.data.storage.DatabaseProvider
import com.takaotech.ktravel.domain.archive.ImportConflictStrategy
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.archive.TravelArchiveException
import com.takaotech.ktravel.testutil.tempdir
import com.takaotech.ktravel.testutil.testAppLogger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.toKotlinxIoPath
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Covers the API key crossing between two installations.
 *
 * The invariant that matters is that the key never appears in `travel.json`: whether or not the
 * user opts in, the plan entry is written with an empty key, and the only copy that leaves the
 * device is the encrypted one in `secrets.json`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TravelArchiveSecretsRoundTripTest :
    BehaviorSpec({

        val tempDir = tempdir("archive-secrets")
        val zipFactory = createZipArchiveFactory()
        val apiKey = "HERE-secret-key-0123456789"
        val password = "a fairly long export password"
        val navigatorUrl = "https://nav.example.com"

        class Installation(root: PlatformFile, name: String) {
            val attachmentRoot = root / "attachments"
            val databaseProvider = DatabaseProvider(
                databaseName = name,
                directory = root.also { it.createDirectories() }.path,
                scope = TestScope(UnconfinedTestDispatcher()),
            )
            val attachments = AttachmentDataSourceImpl(attachmentRoot, testAppLogger())
            val storage = TravelPlanStorageDataSourceImpl(databaseProvider, attachments)
            val exporter = TravelArchiveExporterImpl(
                storage = storage,
                attachments = attachments,
                zipFactory = zipFactory,
                stagingRoot = root / "staging",
                appLogger = testAppLogger(),
            )
            var nextId = 0
            val importer = TravelArchiveImporterImpl(
                storage = storage,
                attachments = attachments,
                zipFactory = zipFactory,
                stagingRoot = root / "staging",
                appLogger = testAppLogger(),
                newId = { "imported-${++nextId}" },
            )
        }

        /** A plan with no attachments: this suite is about the secrets entry, not the files. */
        fun planWithKey(): TravelPlanEntity = TravelPlanEntity(
            id = ArchiveTestFixtures.TRAVEL_ID,
            name = "Tokyo",
            periodStart = ArchiveTestFixtures.plan().periodStart,
            periodEnd = ArchiveTestFixtures.plan().periodEnd,
            days = emptyList(),
            places = emptyList(),
            settings = TravelSettingsEntity(hereApiKey = apiKey),
        )

        fun PlatformFile.entryText(entry: String): String? =
            zipFactory.reader(toKotlinxIoPath()).use { it.readBytes(entry)?.decodeToString() }

        fun PlatformFile.entryPaths(): Set<String> = zipFactory.reader(toKotlinxIoPath()).use { it.entryPaths() }

        given("a plan with an API key exported without a password") {
            val source = Installation(tempDir / "plain-source", "secrets-plain-source")
            source.storage.insertTravelPlan(planWithKey())

            val archive = tempDir / "plain.ktravel"
            source.exporter.export(ArchiveTestFixtures.TRAVEL_ID, archive).getOrThrow()

            `when`("the archive is inspected") {
                then("it carries no secrets entry") {
                    archive.entryPaths() shouldNotContain TravelArchiveFormat.SECRETS_ENTRY
                }
                then("the manifest says so") {
                    archive.entryText(TravelArchiveFormat.MANIFEST_ENTRY)
                        .orEmpty() shouldNotContain "\"has_secrets\":true"
                }
                then("the plan entry does not leak the key") {
                    archive.entryText(TravelArchiveFormat.PLAN_ENTRY)
                        .orEmpty() shouldNotContain apiKey
                }
            }

            `when`("it is imported") {
                val target = Installation(tempDir / "plain-target", "secrets-plain-target")
                val staged = target.importer.stage(archive).getOrThrow()

                then("no password is asked for") {
                    staged.hasSecrets shouldBe false
                }
                then("the trip arrives without a key") {
                    val summary = target.importer
                        .import(staged, ImportConflictStrategy.DUPLICATE)
                        .getOrThrow()
                    target.storage.getTravelPlan(summary.id).settings.hereApiKey shouldBe ""
                }
            }
        }

        given("a plan that names a remote navigator of its own") {
            val source = Installation(tempDir / "navigator-source", "secrets-navigator-source")
            source.storage.insertTravelPlan(
                planWithKey().let {
                    it.copy(
                        settings = it.settings.copy(
                            navigatorPreference = "REMOTE",
                            navigatorRemoteBaseUrl = navigatorUrl,
                        ),
                    )
                },
            )

            val archive = tempDir / "navigator.ktravel"
            source.exporter
                .export(ArchiveTestFixtures.TRAVEL_ID, archive, secretsPassword = password)
                .getOrThrow()

            `when`("the archive is inspected") {
                then("the address and the preference travel with the trip, because they are not secrets") {
                    val plan = archive.entryText(TravelArchiveFormat.PLAN_ENTRY).orEmpty()

                    plan shouldContain navigatorUrl
                    plan shouldContain "\"navigator_preference\":\"REMOTE\""
                }

                then("the only credential stripped is still the HERE key") {
                    archive.entryText(TravelArchiveFormat.PLAN_ENTRY).orEmpty() shouldNotContain apiKey
                }
            }

            `when`("it is imported with the right password") {
                val target = Installation(tempDir / "navigator-target", "secrets-navigator-target")
                val staged = target.importer.stage(archive).getOrThrow()
                val summary = target.importer
                    .import(staged, ImportConflictStrategy.DUPLICATE, secretsPassword = password)
                    .getOrThrow()
                val settings = target.storage.getTravelPlan(summary.id).settings

                then("the HERE key is restored") {
                    settings.hereApiKey shouldBe apiKey
                }

                then("the trip still points at the navigator it was planned against") {
                    settings.navigatorRemoteBaseUrl shouldBe navigatorUrl
                    settings.navigatorPreference shouldBe "REMOTE"
                }
            }
        }

        given("a plan with an API key exported under a password") {
            val source = Installation(tempDir / "sealed-source", "secrets-sealed-source")
            source.storage.insertTravelPlan(planWithKey())

            val archive = tempDir / "sealed.ktravel"
            source.exporter
                .export(ArchiveTestFixtures.TRAVEL_ID, archive, secretsPassword = password)
                .getOrThrow()

            `when`("the archive is inspected") {
                then("the encrypted entry is there") {
                    archive.entryPaths() shouldContain TravelArchiveFormat.SECRETS_ENTRY
                }
                then("the plan entry still does not carry the key") {
                    archive.entryText(TravelArchiveFormat.PLAN_ENTRY)
                        .orEmpty() shouldNotContain apiKey
                }
                then("the secrets entry does not carry it in the clear either") {
                    archive.entryText(TravelArchiveFormat.SECRETS_ENTRY)
                        .orEmpty() shouldNotContain apiKey
                }
            }

            `when`("it is imported with the right password") {
                val target = Installation(tempDir / "sealed-target", "secrets-sealed-target")
                val staged = target.importer.stage(archive).getOrThrow()

                then("the archive announces it has secrets") {
                    staged.hasSecrets shouldBe true
                }
                then("the key arrives intact") {
                    val summary = target.importer
                        .import(staged, ImportConflictStrategy.DUPLICATE, secretsPassword = password)
                        .getOrThrow()
                    target.storage.getTravelPlan(summary.id).settings.hereApiKey shouldBe apiKey
                }
            }

            `when`("it is imported with the wrong password") {
                val target = Installation(tempDir / "wrong-target", "secrets-wrong-target")
                val staged = target.importer.stage(archive).getOrThrow()
                val failure = target.importer
                    .import(staged, ImportConflictStrategy.DUPLICATE, secretsPassword = "nope")
                    .exceptionOrNull()

                then("it fails as a wrong password") {
                    (failure as TravelArchiveException).error shouldBe
                        TravelArchiveError.WrongPassword
                }
                then("nothing was written, so the user can try again") {
                    target.storage.getTravelPlanNameOrNull(ArchiveTestFixtures.TRAVEL_ID) shouldBe null
                }
            }

            `when`("it is imported while declining the key") {
                val target = Installation(tempDir / "declined-target", "secrets-declined-target")
                val staged = target.importer.stage(archive).getOrThrow()

                then("the trip arrives without a key and without complaint") {
                    val summary = target.importer
                        .import(staged, ImportConflictStrategy.DUPLICATE, secretsPassword = null)
                        .getOrThrow()
                    target.storage.getTravelPlan(summary.id).settings.hereApiKey shouldBe ""
                }
            }
        }

        given("a plan with no API key exported with a password anyway") {
            val source = Installation(tempDir / "nokey-source", "secrets-nokey-source")
            source.storage.insertTravelPlan(planWithKey().copy(settings = TravelSettingsEntity()))

            val archive = tempDir / "nokey.ktravel"
            source.exporter
                .export(ArchiveTestFixtures.TRAVEL_ID, archive, secretsPassword = password)
                .getOrThrow()

            `when`("the archive is inspected") {
                then("no secrets entry is written, because there was no secret") {
                    archive.entryPaths() shouldNotContain TravelArchiveFormat.SECRETS_ENTRY
                }
            }
        }
    })

private infix fun Set<String>.shouldContain(value: String) {
    contains(value) shouldBe true
}

private infix fun Set<String>.shouldNotContain(value: String) {
    contains(value) shouldBe false
}
