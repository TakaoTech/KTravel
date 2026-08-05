package com.takaotech.ktravel.domain.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

/**
 * Verifica lo schema custom `ktravel://attachment/<rel>`: costruzione snippet, estrazione e
 * validazione di coerenza dei riferimenti rispetto all'inventario.
 */
class AttachmentReferenceTest :
    BehaviorSpec({

        given("a relative path") {
            val rel = "t1/s1/abc.jpg"

            `when`("building an image snippet") {
                val md = AttachmentReference.imageMarkdown(rel, altText = "Foto")

                then("it should be an inline image with the attachment uri") {
                    md shouldBe "![Foto](ktravel://attachment/t1/s1/abc.jpg)"
                }
            }

            `when`("building a file link snippet") {
                val md = AttachmentReference.fileMarkdown(rel, label = "doc.pdf")

                then("it should be a link with the attachment uri") {
                    md shouldBe "[doc.pdf](ktravel://attachment/t1/s1/abc.jpg)"
                }
            }
        }

        given("a uri") {
            `when`("it is an attachment uri") {
                then("relativePathOf returns the relative path") {
                    AttachmentReference.relativePathOf("ktravel://attachment/t1/s1/x.png") shouldBe "t1/s1/x.png"
                }
            }

            `when`("it is an external uri") {
                then("it is not recognized as an attachment") {
                    AttachmentReference.isAttachmentUri("https://example.com") shouldBe false
                    AttachmentReference.relativePathOf("https://example.com") shouldBe null
                }
            }
        }

        given("a markdown with attachment references") {
            val markdown = """
            # Notes
            ![](ktravel://attachment/t1/s1/img.jpg)
            See [doc](ktravel://attachment/t1/s1/doc.pdf) and [external](https://example.com).
            """.trimIndent()

            `when`("extracting relative paths") {
                then("only attachment references are returned") {
                    AttachmentReference.extractRelativePaths(markdown) shouldContainExactly listOf(
                        "t1/s1/img.jpg",
                        "t1/s1/doc.pdf",
                    )
                }
            }

            `when`("all references exist in the inventory") {
                val missing = AttachmentReference.missingReferences(
                    markdown,
                    inventoryRelativePaths = listOf("t1/s1/img.jpg", "t1/s1/doc.pdf"),
                )

                then("there are no missing references") {
                    missing.shouldBeEmpty()
                }
            }

            `when`("a reference is not present in the inventory") {
                val missing = AttachmentReference.missingReferences(
                    markdown,
                    inventoryRelativePaths = listOf("t1/s1/img.jpg"),
                )

                then("the dangling reference is reported") {
                    missing shouldContainExactly listOf("t1/s1/doc.pdf")
                }
            }

            `when`("references are rewritten with a full mapping") {
                val rewritten = AttachmentReference.rewriteReferences(
                    markdown,
                    mapping = mapOf(
                        "t1/s1/img.jpg" to "t9/s9/img.jpg",
                        "t1/s1/doc.pdf" to "t9/s9/doc.pdf",
                    ),
                )

                then("every reference points to the new path") {
                    AttachmentReference.extractRelativePaths(rewritten) shouldContainExactly listOf(
                        "t9/s9/img.jpg",
                        "t9/s9/doc.pdf",
                    )
                }

                then("the surrounding markdown is preserved") {
                    rewritten.contains("[external](https://example.com)") shouldBe true
                    rewritten.startsWith("# Notes") shouldBe true
                }
            }

            `when`("references are rewritten with a partial mapping") {
                val rewritten = AttachmentReference.rewriteReferences(
                    markdown,
                    mapping = mapOf("t1/s1/img.jpg" to "t9/s9/img.jpg"),
                )

                then("unmapped references are left untouched") {
                    AttachmentReference.extractRelativePaths(rewritten) shouldContainExactly listOf(
                        "t9/s9/img.jpg",
                        "t1/s1/doc.pdf",
                    )
                }
            }

            `when`("references are rewritten with an empty mapping") {
                then("the markdown is returned unchanged") {
                    AttachmentReference.rewriteReferences(
                        markdown,
                        mapping = emptyMap(),
                    ) shouldBe markdown
                }
            }
        }

        given("a markdown whose paths contain regex replacement metacharacters") {
            val markdown = "![](ktravel://attachment/t1/s1/a\$b.jpg)"

            `when`("the reference is rewritten") {
                val rewritten = AttachmentReference.rewriteReferences(
                    markdown,
                    mapping = mapOf("t1/s1/a\$b.jpg" to "t2/s2/c\$d.jpg"),
                )

                then("the metacharacters are not interpreted as group references") {
                    rewritten shouldBe "![](ktravel://attachment/t2/s2/c\$d.jpg)"
                }
            }
        }
    })
