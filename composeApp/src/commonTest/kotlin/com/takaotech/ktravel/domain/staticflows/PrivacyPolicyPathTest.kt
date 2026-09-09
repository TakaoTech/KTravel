package com.takaotech.ktravel.domain.staticflows

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * Addressing a paragraph of the policy by its path.
 *
 * The path is what the introduction points at and what the document opens on, so it is a contract in
 * both directions. Against a fixture rather than the shipped text: what is being checked is how a
 * path is walked, not what the policy says.
 */
class PrivacyPolicyPathTest :
    BehaviorSpec({
        val policy = PrivacyPolicy(
            version = 1,
            language = "en",
            sections = listOf(
                PrivacyPolicySection(
                    id = "data",
                    title = "Data",
                    body = "What is handled.",
                    subsections = listOf(
                        PrivacyPolicySection(
                            id = "telemetry",
                            title = "Telemetry",
                            body = "Only if you say so.",
                            subsections = listOf(
                                PrivacyPolicySection(id = "retention", title = "Retention", body = "Three days."),
                            ),
                        ),
                    ),
                ),
                PrivacyPolicySection(id = "rights", title = "Rights", body = "Your choices."),
            ),
        )

        given("a path that names a section") {
            `when`("it is looked up") {
                then("the section is returned, at any depth") {
                    policy.section("data")?.title shouldBe "Data"
                    policy.section("data.telemetry")?.title shouldBe "Telemetry"
                    policy.section("data.telemetry.retention")?.title shouldBe "Retention"
                }
            }
        }

        given("a path that names nothing") {
            `when`("it is looked up") {
                then("nothing comes back, rather than a failure") {
                    policy.section("data.nowhere") shouldBe null
                    policy.section("nowhere") shouldBe null
                    policy.section("telemetry") shouldBe null
                    policy.section("data.telemetry.retention.deeper") shouldBe null
                }
            }
        }

        given("a path that is not a path") {
            `when`("it is looked up") {
                then("nothing comes back") {
                    policy.section("") shouldBe null
                    policy.section(".") shouldBe null
                    policy.section("data.") shouldBe null
                    policy.section(".telemetry") shouldBe null
                }
            }
        }

        given("the whole policy") {
            `when`("its paths are listed") {
                then("every section appears once, parents before their children") {
                    policy.paths() shouldBe listOf(
                        "data",
                        "data.telemetry",
                        "data.telemetry.retention",
                        "rights",
                    )
                }
            }
        }
    })
