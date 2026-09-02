package com.takaotech.ktravel.core.data.mime

import com.takaotech.ktravel.data.entity.AttachmentEntity
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

/**
 * Checks the media type derived from a file extension and, above all, that [MimeType] stays a plain
 * string on the wire: the archive and the database hold documents written before the type existed.
 */
class MimeTypeTest :
    BehaviorSpec({

        val json = Json { encodeDefaults = true }

        given("a file extension") {
            `when`("it is known to the catalog") {
                then("the media type is resolved with or without the leading dot, in any case") {
                    MimeTypes.fromExtension("jpg") shouldBe MimeType("image/jpeg")
                    MimeTypes.fromExtension(".JPEG") shouldBe MimeType("image/jpeg")
                    MimeTypes.fromExtension("PDF") shouldBe MimeType("application/pdf")
                    MimeTypes.fromExtension("md") shouldBe MimeType("text/markdown")
                }

                then("an extension claimed by two groups resolves to the one with precedence") {
                    MimeTypes.fromExtension("mp4") shouldBe MimeType("video/mp4")
                    MimeTypes.fromExtension("m4a") shouldBe MimeType("audio/mp4")
                }
            }

            `when`("it is unknown or empty") {
                then("it falls back to the default binary type") {
                    MimeTypes.fromExtension("nope") shouldBe MimeTypes.DEFAULT
                    MimeTypes.fromExtension("") shouldBe MimeTypes.DEFAULT
                    MimeTypes.DEFAULT shouldBe MimeType("application/octet-stream")
                }
            }
        }

        given("an attachment entity") {
            val entity = AttachmentEntity(
                id = "a1",
                relativePath = "t1/s1/abc.jpg",
                originalName = "photo.jpg",
                mimeType = MimeType("image/jpeg"),
                sizeBytes = 1234,
            )

            `when`("it is encoded") {
                val encoded = json.encodeToString(AttachmentEntity.serializer(), entity)

                then("the media type is written as the plain string it has always been") {
                    encoded shouldBe """{"id":"a1","relative_path":"t1/s1/abc.jpg",""" +
                        """"original_name":"photo.jpg","mime_type":"image/jpeg","size_bytes":1234}"""
                }
            }

            `when`("a document holds a media type outside the catalog") {
                val decoded = json.decodeFromString(
                    AttachmentEntity.serializer(),
                    """{"id":"a1","relative_path":"t1/s1/abc.bin","original_name":"photo.bin",""" +
                        """"mime_type":"application/x-custom","size_bytes":1}""",
                )

                then("it is decoded verbatim instead of being rejected") {
                    decoded.mimeType shouldBe MimeType("application/x-custom")
                    decoded.mimeType.isImage shouldBe false
                }
            }
        }

        given("a media type") {
            `when`("it belongs to the image tree") {
                then("it is previewable") {
                    MimeType("image/png").isImage shouldBe true
                    MimeType("IMAGE/PNG").isImage shouldBe true
                    MimeType("application/pdf").isImage shouldBe false
                }
            }
        }
    })
