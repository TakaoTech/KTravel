package com.takaotech.ktravel.domain.staticflows

import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.model.AppSettingsDomain
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * When an answer to the privacy policy stops counting, and what comes back when it does.
 *
 * The boundary is documented here on purpose: "a year" is a promise made to the user in the policy
 * itself, and a test is the only place that keeps it from drifting. What is shown when it lapses is
 * the other half of the promise — the privacy page again, not the whole introduction.
 */
class ConsentValidityTest :
    BehaviorSpec({
        val answeredAt = Instant.parse("2025-09-06T10:00:00Z")

        given("an answer given a year ago minus a day") {
            `when`("it is judged") {
                then("it still stands") {
                    isConsentExpired(answeredAt, answeredAt + 364.days) shouldBe false
                }
            }
        }

        given("an answer given more than a year ago") {
            `when`("it is judged") {
                then("it has expired") {
                    isConsentExpired(answeredAt, answeredAt + 366.days) shouldBe true
                }
            }
        }

        given("settings holding an expired consent") {
            `when`("the effective consent is asked for") {
                then("it is Unknown, and the privacy half is due again") {
                    val settings = AppSettingsDomain(
                        telemetryConsent = TelemetryConsent.Granted,
                        acknowledgedIntroVersion = 1,
                        acknowledgedPrivacyVersion = 1,
                        consentDecidedAt = answeredAt,
                    )
                    val now = answeredAt + 400.days

                    settings.effectiveConsent(now) shouldBe TelemetryConsent.Unknown
                    settings.introRequirement(introVersion = 1, policyVersion = 1, now = now) shouldBe
                        IntroRequirement.PrivacyOnly
                }
            }
        }

        given("settings answered to an older version of the policy") {
            `when`("a newer policy ships") {
                then("the privacy half is due again even though the answer is fresh") {
                    val settings = AppSettingsDomain(
                        telemetryConsent = TelemetryConsent.Denied,
                        acknowledgedIntroVersion = 1,
                        acknowledgedPrivacyVersion = 1,
                        consentDecidedAt = answeredAt,
                    )

                    settings.introRequirement(introVersion = 1, policyVersion = 2, now = answeredAt) shouldBe
                        IntroRequirement.PrivacyOnly
                    settings.introRequirement(introVersion = 1, policyVersion = 1, now = answeredAt) shouldBe
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
                        consentDecidedAt = answeredAt,
                    )

                    settings.introRequirement(introVersion = 2, policyVersion = 1, now = answeredAt) shouldBe
                        IntroRequirement.Full
                }
            }
        }

        given("an installation that has never answered") {
            `when`("what is due is asked for") {
                then("everything is, and nothing may be sent in the meantime") {
                    val settings = AppSettingsDomain()

                    settings.effectiveConsent(answeredAt) shouldBe TelemetryConsent.Unknown
                    settings.introRequirement(introVersion = 1, policyVersion = 1, now = answeredAt) shouldBe
                        IntroRequirement.Full
                }
            }
        }
    })
