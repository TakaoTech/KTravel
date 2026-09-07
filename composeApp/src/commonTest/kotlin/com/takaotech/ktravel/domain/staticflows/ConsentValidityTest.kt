package com.takaotech.ktravel.domain.staticflows

import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.model.AppSettingsDomain
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * When an answer to the privacy notice stops counting.
 *
 * The boundary is documented here on purpose: "a year" is a promise made to the user in the notice
 * itself, and a test is the only place that keeps it from drifting.
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
                then("it is Unknown, and the notice is due again") {
                    val settings = AppSettingsDomain(
                        telemetryConsent = TelemetryConsent.Granted,
                        acknowledgedConsentVersion = 1,
                        consentDecidedAt = answeredAt,
                    )
                    val now = answeredAt + 400.days

                    settings.effectiveConsent(now) shouldBe TelemetryConsent.Unknown
                    settings.needsConsentFlow(flowVersion = 1, now = now) shouldBe true
                }
            }
        }

        given("settings answered to an older version of the notice") {
            `when`("a newer notice ships") {
                then("it is due again even though the answer is fresh") {
                    val settings = AppSettingsDomain(
                        telemetryConsent = TelemetryConsent.Denied,
                        acknowledgedConsentVersion = 1,
                        consentDecidedAt = answeredAt,
                    )

                    settings.needsConsentFlow(flowVersion = 2, now = answeredAt) shouldBe true
                    settings.needsConsentFlow(flowVersion = 1, now = answeredAt) shouldBe false
                }
            }
        }

        given("an installation that has never answered") {
            `when`("the notice is due") {
                then("it is, and nothing may be sent in the meantime") {
                    val settings = AppSettingsDomain()

                    settings.effectiveConsent(answeredAt) shouldBe TelemetryConsent.Unknown
                    settings.needsConsentFlow(flowVersion = 1, now = answeredAt) shouldBe true
                }
            }
        }
    })
