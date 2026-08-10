package com.takaotech.password.resources

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Guards the generated dictionary sources.
 *
 * The word lists go through a Gradle task that chunks them into string constants and escapes
 * backslashes, quotes and dollar signs. A mistake there would silently drop or corrupt entries and
 * every entropy figure would shift with it, so the line counts and the awkward entries are asserted
 * against the upstream files directly.
 */
class DictionaryTest :
    BehaviorSpec({

        given("the generated word lists") {
            `when`("their entries are counted") {
                // None of the upstream files ends in a newline, so these are one more than `wc -l`
                // reports — the last word is a line too, and upstream's readLine() loop reads it.
                then("each holds exactly as many entries as the upstream file has lines") {
                    PasswordsDictionary.lines().size shouldBe 100_002
                    SurnamesDictionary.lines().size shouldBe 40_583
                    EnglishDictionary.lines().size shouldBe 18_091
                    EffLargeDictionary.lines().size shouldBe 7_776
                    FemaleNamesDictionary.lines().size shouldBe 3_815
                    MaleNamesDictionary.lines().size shouldBe 1_004
                }
            }

            `when`("the entries needing escaping are looked up") {
                then("backslashes, dollars and non-ASCII survived the round trip") {
                    // These are every line in passwords.txt that the generator had to escape.
                    PasswordsDictionary.lines() shouldContainAll listOf(
                        """pic\'s""",
                        "P030710P\$E4O",
                        "12345\$",
                        "g00dPa\$\$w0rD",
                        "\$andmann",
                        "aª»",
                    )
                }
            }

            `when`("the order is inspected") {
                then("the most common passwords come first, which is what ranks them") {
                    PasswordsDictionary.lines().take(3) shouldBe
                        listOf("123456", "password", "12345678")
                }
            }

            `when`("no chunk boundary is visible") {
                then("no entry contains a line break") {
                    PasswordsDictionary.lines().none { it.contains('\n') } shouldBe true
                }
            }
        }

        given("the ranked dictionaries built from those lists") {
            `when`("they are assembled") {
                val passwords = Dictionaries.defaults.first { it.dictionaryName == "passwords" }

                then("rank one is the single most common password") {
                    passwords.dictionary["123456"] shouldBe 1
                }
                then("ranks follow the order of the file") {
                    passwords.dictionary["password"] shouldBe 2
                }
                then("the length lookup covers every length up to the longest entry") {
                    (0..passwords.maxLength).all { it in passwords.sortedDictionaryLengthLookup } shouldBe true
                }
            }
        }

        given("user supplied values") {
            `when`("a dictionary is built from them") {
                val dictionary = Dictionaries.userInputs(listOf("Takaotech", "KTravel", "", "Takaotech"))

                then("they are lower cased so matching can find them") {
                    dictionary.dictionary["takaotech"] shouldNotBe null
                }
                then("blanks and duplicates are dropped") {
                    dictionary.dictionary.size shouldBe 2
                }
            }
        }
    })
