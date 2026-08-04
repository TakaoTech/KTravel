package com.takaotech.ktravel.presentation.planning.detail

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * Verifica la validazione Markdown usata per il salvataggio automatico delle note.
 */
class MarkdownValidationTest :
    BehaviorSpec({

        given("isValidMarkdown") {
            `when`("the content is well-formed Markdown") {
                then("it should be considered valid") {
                    isValidMarkdown("# Title\n- **bold** item\n[link](https://example.com)") shouldBe true
                }
            }

            `when`("the content is empty") {
                then("it should be considered valid") {
                    isValidMarkdown("") shouldBe true
                }
            }

            `when`("the content is plain text") {
                then("it should be considered valid") {
                    isValidMarkdown("just some plain notes") shouldBe true
                }
            }
        }
    })
