package com.takaotech.ktravel.domain.staticflows

import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.model.AppSettingsDomain
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * What brings the introduction back, and how much of it comes.
 *
 * The versions are the only thing that can: diagnostics rest on a legitimate interest, so where the
 * user left them stands until they move it and nothing expires on a timer. What comes back when the
 * policy is rewritten is the privacy half, never the welcome cards.
 */
class IntroRequirementTest :
    BehaviorSpec({
        given("settings that were shown an older version of the policy") {
            `when`("a newer policy ships") {
                then("the privacy half is due again, and only that") {
                    val settings = AppSettingsDomain(
                        telemetryConsent = TelemetryConsent.Denied,
                        acknowledgedIntroVersion = 1,
                        acknowledgedPrivacyVersion = 1,
                    )

                    settings.introRequirement(introVersion = 1, policyVersion = 2) shouldBe
                        IntroRequirement.PrivacyOnly
                    settings.introRequirement(introVersion = 1, policyVersion = 1) shouldBe
                        IntroRequirement.None
                }
            }
        }

        given("settings that have seen an older introduction") {
            `when`("a newer introduction ships") {
                then("the whole thing is shown again, not only its privacy half") {
                    val settings = AppSettingsDomain(
                        telemetryConsent = TelemetryConsent.Granted,
                        acknowledgedIntroVersion = 1,
                        acknowledgedPrivacyVersion = 1,
                    )

                    settings.introRequirement(introVersion = 2, policyVersion = 1) shouldBe
                        IntroRequirement.Full
                }
            }
        }

        given("settings whose stored value this build cannot read") {
            `when`("what is due is asked for") {
                then("the privacy half comes back, because nobody has been told anything") {
                    val settings = AppSettingsDomain(
                        telemetryConsent = TelemetryConsent.Unknown,
                        acknowledgedIntroVersion = 1,
                        acknowledgedPrivacyVersion = 1,
                    )

                    settings.introRequirement(introVersion = 1, policyVersion = 1) shouldBe
                        IntroRequirement.PrivacyOnly
                }
            }
        }

        given("an installation that has never been through the introduction") {
            `when`("what is due is asked for") {
                then("everything is, and nothing may be sent in the meantime") {
                    val settings = AppSettingsDomain()

                    settings.telemetryConsent shouldBe TelemetryConsent.Unknown
                    settings.introRequirement(introVersion = 1, policyVersion = 1) shouldBe
                        IntroRequirement.Full
                }
            }
        }
    })
