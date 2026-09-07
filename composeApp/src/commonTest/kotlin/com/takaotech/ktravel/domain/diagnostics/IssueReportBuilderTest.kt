package com.takaotech.ktravel.domain.diagnostics

import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * What a bug report says, and what it does not.
 *
 * The log is quoted only when the user refused to send diagnostics: with consent the maintainer
 * already has it, and putting it in the URL as well would send the same thing twice through a
 * channel the user did not pick. The cap is the other half — a URL past about eight kilobytes never
 * reaches GitHub at all.
 */
class IssueReportBuilderTest :
    BehaviorSpec({
        val log = (1..5_000).joinToString("\n") { "line $it" }

        given("a user who did not consent to sending diagnostics") {
            `when`("a report is built") {
                then("the newest log lines are quoted, within the cap") {
                    val report = IssueReportBuilder.build(
                        appVersion = "0.1.0",
                        platform = "MACOS",
                        consent = TelemetryConsent.Denied,
                        installationId = "install-1",
                        logTail = log,
                    )

                    (report.body.length <= MAX_BODY_CHARS) shouldBe true
                    report.body.contains("line 5000") shouldBe true
                    report.body.contains("line 1\n") shouldBe false
                    report.body.contains("0.1.0") shouldBe true
                    report.body.contains("install-1") shouldBe true
                }
            }
        }

        given("a user who consented") {
            `when`("a report is built") {
                then("the log is not quoted again, and the answer is stated") {
                    val report = IssueReportBuilder.build(
                        appVersion = "0.1.0",
                        platform = "ANDROID",
                        consent = TelemetryConsent.Granted,
                        installationId = "install-2",
                        logTail = log,
                    )

                    report.body.contains("line 5000") shouldBe false
                    report.body.contains("Diagnostics sent to the developer: yes") shouldBe true
                }
            }
        }

        given("a report") {
            `when`("its URL is built") {
                then("it points at this repository and carries the body encoded") {
                    val url = gitHubIssueUrl(IssueReport(title = "[MACOS] ", body = "a b\nc"))

                    url.startsWith("$KTRAVEL_ISSUES_URL?labels=bug") shouldBe true
                    url.contains("body=a%20b%0Ac") shouldBe true
                    url.contains(" ") shouldBe false
                }
            }
        }
    })
