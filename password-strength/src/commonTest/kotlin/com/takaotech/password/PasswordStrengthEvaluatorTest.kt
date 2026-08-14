package com.takaotech.password

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

/**
 * Reference values captured by running the same passwords through upstream nbvcxz 1.5.1 on the JVM.
 * The port reproduces every one of them to six decimal places, so they are asserted tightly: a drift
 * here means the port and upstream have diverged, which is exactly what this suite exists to catch.
 */
class PasswordStrengthEvaluatorTest :
    BehaviorSpec({

        val evaluator = NbvcxzPasswordStrengthEvaluator()
        val tolerance = 1e-6

        given("the second entry of the password dictionary") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("password")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (1.0 plusOrMinus tolerance)
                }
                then("it scores zero") {
                    strength.score shouldBe 0
                }
                then("its rank puts it in the top ten") {
                    strength.warning shouldBe PasswordWarning.TOP_10_PASSWORD
                }
                then("it suggests adding another word") {
                    strength.suggestions shouldContain PasswordSuggestion.ADD_ANOTHER_WORD
                }
            }
        }

        given("a dictionary word dressed up with leet substitutions") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("p4ssw0rd")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (10.044394 plusOrMinus tolerance)
                }
                then("the substitutions are seen through, so it barely scores above zero") {
                    strength.score shouldBe 1
                }
                then("it suggests dropping the predictable substitutions") {
                    strength.suggestions shouldContain
                        PasswordSuggestion.AVOID_PREDICTABLE_LETTER_SUBSTITUTIONS
                }
            }
        }

        given("a keyboard walk that is also a known password") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("qwertyuiop")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (4.392317 plusOrMinus tolerance)
                }
                then("the dictionary explanation wins over the spatial one, as upstream does") {
                    strength.warning shouldBe PasswordWarning.TOP_100_PASSWORD
                }
            }
        }

        given("a repeated single character") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("aaaaaaaaaa")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (8.022368 plusOrMinus tolerance)
                }
                then("the repetition is recognised") {
                    strength.warning shouldBe PasswordWarning.REPEATED_CHARACTER
                }
            }
        }

        given("a repeated block of characters") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("abcabcabcabc")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (8.285402 plusOrMinus tolerance)
                }
                then("the repeated block is told apart from a repeated character") {
                    strength.warning shouldBe PasswordWarning.REPEATED_PATTERN
                }
            }
        }

        given("a block whose repeating unit the greedy and lazy passes disagree on") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("aabaabaab")

                then("the longer unit wins, so it is priced as `aab` three times over") {
                    strength.entropy shouldBe (6.285402 plusOrMinus tolerance)
                }
                then("the repeated block is recognised") {
                    strength.warning shouldBe PasswordWarning.REPEATED_PATTERN
                }
            }
        }

        given("a digit sequence") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("1234567890")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (4.584963 plusOrMinus tolerance)
                }
                then("it scores zero") {
                    strength.score shouldBe 0
                }
            }
        }

        given("a date written with separators") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("12/03/1985")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (17.550386 plusOrMinus tolerance)
                }
                then("the date is recognised") {
                    strength.warning shouldBe PasswordWarning.DATES
                }
                then("it suggests avoiding dates") {
                    strength.suggestions shouldContain PasswordSuggestion.AVOID_DATES
                }
            }
        }

        given("the canonical four-word passphrase") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("correct horse battery staple")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (16.609669 plusOrMinus tolerance)
                }
                then("upstream rates it poorly, because edit distance matches the whole phrase") {
                    strength.score shouldBe 1
                }
            }
        }

        given("a random string of mixed classes") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("xK9#mVq2\$Lp7Zt4W")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (70.380898 plusOrMinus tolerance)
                }
                then("it reaches the top score") {
                    strength.score shouldBe 4
                }
                then("it carries no warning") {
                    strength.warning shouldBe null
                }
            }
        }

        given("a word, a stray letter and a year") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("Takaotech2024")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (37.584380 plusOrMinus tolerance)
                }
                then("it clears the minimum and scores at the top") {
                    strength.score shouldBe 4
                }
            }
        }

        given("a long sentence") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("hello world this is a long passphrase")

                then("it matches the upstream entropy") {
                    strength.entropy shouldBe (73.513752 plusOrMinus tolerance)
                }
                then("it reaches the top score") {
                    strength.score shouldBe 4
                }
            }
        }

        given("an empty password") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("")

                then("its entropy is zero") {
                    strength.entropy shouldBe 0.0
                }
                then("it scores zero without failing") {
                    strength.score shouldBe 0
                }
                then("it falls back to the generic suggestions") {
                    strength.suggestions shouldContain PasswordSuggestion.USE_FEWER_WORDS
                }
            }
        }

        given("a password built out of a value the attacker knows") {
            `when`("that value is supplied as a user input") {
                val known = evaluator.evaluate("Takaotech2024", userInputs = listOf("takaotech"))
                val unknown = evaluator.evaluate("Takaotech2024")

                then("knowing it does not make the password look any stronger") {
                    (known.entropy <= unknown.entropy) shouldBe true
                }
            }
        }

        given("a password longer than the configured maximum") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("a".repeat(300))

                then("it is truncated to the limit instead of failing") {
                    strength.entropy shouldBeGreaterThan 0.0
                }
            }
        }

        given("a password made of characters outside ASCII") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("日本語のパスワード")

                then("the unicode class is priced instead of failing") {
                    strength.entropy shouldBeGreaterThan 0.0
                }
            }
        }

        given("a password of a single character") {
            `when`("it is evaluated") {
                val strength = evaluator.evaluate("a")

                then("it is handled without failing on the case heuristics") {
                    strength.score shouldBe 0
                }
            }
        }
    })
