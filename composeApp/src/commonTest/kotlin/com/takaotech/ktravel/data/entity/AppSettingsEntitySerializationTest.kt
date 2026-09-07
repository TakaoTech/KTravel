package com.takaotech.ktravel.data.entity

import com.takaotech.ktravel.core.logging.DEFAULT_LOG_RETENTION_DAYS
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.data.datasource.appSettingsJson
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * How the settings document survives being read by a build that did not write it.
 *
 * Both directions matter here. A document written by an older build has none of the diagnostics
 * fields and has to read as the defaults; one written by a newer build may hold a consent this build
 * has never heard of, and the only safe reading of a decision it cannot understand is that there is
 * none — the notice is then shown again, and nothing is sent in the meantime.
 */
class AppSettingsEntitySerializationTest :
    BehaviorSpec({
        given("a settings document written before diagnostics existed") {
            `when`("it is read") {
                then("the new fields come back as their defaults") {
                    val stored = """{"type":"app_settings","navigator_remote_base_url":"https://nav.example.com"}"""

                    val entity = appSettingsJson.decodeFromString<AppSettingsEntity>(stored)

                    entity.navigatorRemoteBaseUrl shouldBe "https://nav.example.com"
                    entity.telemetryConsent shouldBe TelemetryConsent.Unknown
                    entity.acknowledgedConsentVersion shouldBe 0
                    entity.consentDecidedAtEpochMillis shouldBe 0
                    entity.logRetentionDays shouldBe DEFAULT_LOG_RETENTION_DAYS
                    entity.installationId shouldBe ""
                }
            }
        }

        given("a document holding a consent this build does not know") {
            `when`("it is read") {
                then("it counts as no decision rather than failing the read") {
                    val stored = """{"type":"app_settings","telemetry_consent":"granted_for_research"}"""

                    appSettingsJson.decodeFromString<AppSettingsEntity>(stored).telemetryConsent shouldBe
                        TelemetryConsent.Unknown
                }
            }
        }

        given("an answered consent") {
            `when`("it is written and read back") {
                then("it survives, under the name the document has always used") {
                    val entity = AppSettingsEntity(telemetryConsent = TelemetryConsent.Granted)

                    val written = appSettingsJson.encodeToString(entity)

                    written.contains("\"telemetry_consent\":\"granted\"") shouldBe true
                    appSettingsJson.decodeFromString<AppSettingsEntity>(written) shouldBe entity
                }
            }
        }
    })
