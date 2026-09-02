package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.core.data.mime.MimeType
import com.takaotech.ktravel.testutil.tempdir
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * Verifica lo schema cartelle `<root>/<travelId>/<stepId>/<uuid>.<ext>` e le operazioni di
 * salvataggio/risoluzione/cancellazione dell'inventario file, usando una root temporanea.
 */
class AttachmentDataSourceImplTest :
    BehaviorSpec({

        val tempDir = tempdir("attachments-test")

        given("an attachment data source rooted in a temp directory") {
            val root = tempDir / "root"
            val dataSource = AttachmentDataSourceImpl(root)

            val sourceBytes = byteArrayOf(1, 2, 3, 4, 5)

            suspend fun sampleSource(name: String) = (tempDir / name).also { it.write(sourceBytes) }

            `when`("saveAttachment is called") {
                val source = sampleSource("photo.jpg")
                val entity = dataSource.saveAttachment("t1", "s1", source)

                then("it returns a relative path under travelId/stepId") {
                    entity.relativePath.startsWith("t1/s1/") shouldBe true
                    entity.relativePath.endsWith(".jpg") shouldBe true
                }

                then("it keeps the original name and derived mime type") {
                    entity.originalName shouldBe "photo.jpg"
                    entity.mimeType shouldBe MimeType("image/jpeg")
                }

                then("the file is copied on disk following the folder scheme") {
                    (root / "t1" / "s1").isDirectory() shouldBe true
                    val resolved = dataSource.resolveFile(entity.relativePath)
                    resolved.exists() shouldBe true
                    resolved.readBytes() shouldBe sourceBytes
                }
            }

            `when`("deleteAttachment is called") {
                val entity = dataSource.saveAttachment("t2", "s2", sampleSource("doc.pdf"))
                dataSource.deleteAttachment(entity.relativePath)

                then("the file no longer exists") {
                    dataSource.resolveFile(entity.relativePath).exists() shouldBe false
                }
            }

            `when`("deleteTravelAttachments is called") {
                dataSource.saveAttachment("t3", "s1", sampleSource("a.png"))
                dataSource.saveAttachment("t3", "s2", sampleSource("b.png"))
                dataSource.deleteTravelAttachments("t3")

                then("the whole travel folder is removed") {
                    (root / "t3").exists() shouldBe false
                }
            }
        }
    })
