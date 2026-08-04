package com.takaotech.ktravel.data.archive.zip

import com.takaotech.ktravel.testutil.tempdir
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.toKotlinxIoPath
import io.github.vinceglb.filekit.write
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Verifica il contratto dell'astrazione zip su ogni piattaforma: lo stesso test gira su JVM e su
 * iOS, quindi copre entrambe le varianti dell'artefatto kzip.
 */
class ZipArchiveTest :
    BehaviorSpec({

        val tempDir = tempdir("zip-archive-test")
        val factory = createZipArchiveFactory()

        given("a zip archive factory") {

            `when`("entries are written and the archive is reopened") {
                val archive = tempDir / "round-trip.zip"
                val payload = ByteArray(1024) { (it % 251).toByte() }
                val sourceFile = (tempDir / "payload.bin").also { it.write(payload) }

                factory.writer(archive.toKotlinxIoPath()).use { writer ->
                    writer.writeEntry(
                        "manifest.json",
                        """{"schema_version":1}""".encodeToByteArray()
                    )
                    writer.writeEntry("attachments/t1/s1/photo.bin", sourceFile.toKotlinxIoPath())
                }

                then("every written entry is listed") {
                    factory.reader(archive.toKotlinxIoPath()).use { reader ->
                        reader.entryPaths() shouldBe setOf(
                            "manifest.json",
                            "attachments/t1/s1/photo.bin",
                        )
                    }
                }

                then("byte entries round-trip unchanged") {
                    factory.reader(archive.toKotlinxIoPath()).use { reader ->
                        reader.readBytes("manifest.json")
                            ?.decodeToString() shouldBe """{"schema_version":1}"""
                    }
                }

                then("file entries round-trip unchanged") {
                    factory.reader(archive.toKotlinxIoPath()).use { reader ->
                        reader.readBytes("attachments/t1/s1/photo.bin") shouldBe payload
                    }
                }

                then("the total uncompressed size covers every entry") {
                    factory.reader(archive.toKotlinxIoPath()).use { reader ->
                        reader.totalUncompressedSize() shouldBe payload.size + """{"schema_version":1}""".length
                    }
                }

                then("an entry can be extracted to a file") {
                    val target = tempDir / "extracted.bin"
                    factory.reader(archive.toKotlinxIoPath()).use { reader ->
                        reader.extractTo(
                            "attachments/t1/s1/photo.bin",
                            target.toKotlinxIoPath(),
                        ) shouldBe true
                    }
                    target.readBytes() shouldBe payload
                }
            }

            `when`("a missing entry is requested") {
                val archive = tempDir / "missing-entry.zip"
                factory.writer(archive.toKotlinxIoPath())
                    .use { it.writeEntry("a.txt", byteArrayOf(1)) }

                then("readBytes returns null and extractTo returns false") {
                    factory.reader(archive.toKotlinxIoPath()).use { reader ->
                        reader.readBytes("nope.txt") shouldBe null
                        reader.extractTo(
                            "nope.txt",
                            (tempDir / "nope.bin").toKotlinxIoPath(),
                        ) shouldBe false
                    }
                }
            }

            `when`("the file is not a zip archive") {
                val notAZip =
                    (tempDir / "garbage.ktravel").also { it.write(ByteArray(512) { i -> i.toByte() }) }

                then("opening it fails with ZipFormatException") {
                    val exception = shouldThrow<ZipFormatException> {
                        factory.reader(notAZip.toKotlinxIoPath()).close()
                    }
                    exception.cause shouldNotBe null
                }
            }
        }
    })
